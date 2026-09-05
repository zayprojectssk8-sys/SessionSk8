package com.sk8.appwatch.presentation.screen.session_skate

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.concurrent.futures.await
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.helper.SessionSyncManager
import com.zaysk8.core.helper.SyncTimestamps
import com.zaysk8.core.model.SkateSessionPhase
import com.zaysk8.core.model.SkateSessionStateModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

class WearSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionSyncManager by lazy { SessionSyncManager(application.applicationContext) }
    private val remoteActivityHelper by lazy { RemoteActivityHelper(application.applicationContext) }

    private val _uiState = MutableStateFlow(SkateSessionStateModel())
    val uiState: StateFlow<SkateSessionStateModel> = _uiState.asStateFlow()

    private var currentTimestamps = SyncTimestamps()

    init {
        observeRemoteState()
        startLocalTicker()
    }

    /**
     * Valida si el estado actual contiene una sesión válida creada por el celular
     * o iniciada previamente en la app.
     */
    fun isSessionConfigured(state: SkateSessionStateModel = _uiState.value): Boolean {
        return state.isSessionStarted ||
                state.totalRounds > 0 ||
                state.phaseTotalDurationSec > 0 ||
                state.currentPhase != SkateSessionPhase.NOT_STARTED
    }

    /**
     * Escucha las actualizaciones provenientes de DataClient (Celular o Persistencia)
     */
    private fun observeRemoteState() {
        sessionSyncManager.observeSessionState()
            .onEach { (model, timestamps) ->
                _uiState.value = model
                currentTimestamps = timestamps
            }
            .launchIn(viewModelScope)
    }

    /**
     * Ticker de 1 segundo optimizado para Wear OS.
     * Solo realiza cálculos cuando la sesión está activa y no pausada.
     */
    private fun startLocalTicker() {
        viewModelScope.launch {
            while (true) {
                val state = _uiState.value

                if (state.isRunning && !state.isPaused && state.isSessionStarted) {
                    val now = System.currentTimeMillis()

                    // 1. Cálculo de tiempo transcurrido general
                    val elapsedSec = if (currentTimestamps.overallStartTimeMs > 0) {
                        ((now - currentTimestamps.overallStartTimeMs) / 1000).coerceAtLeast(0L)
                    } else {
                        state.generalElapsedTimeSec + 1
                    }

                    // 2. Cálculo de tiempo restante de la fase
                    val remainingSec =
                        if (currentTimestamps.phaseStartTimeMs > 0 && state.phaseTotalDurationSec > 0) {
                            val elapsedPhaseSec = (now - currentTimestamps.phaseStartTimeMs) / 1000
                            (state.phaseTotalDurationSec - elapsedPhaseSec).coerceAtLeast(0L)
                        } else {
                            (state.phaseTimeRemainingSec - 1).coerceAtLeast(0L)
                        }

                    _uiState.update { currentState ->
                        currentState.copy(
                            generalElapsedTimeSec = elapsedSec,
                            phaseTimeRemainingSec = remainingSec
                        )
                    }
                }

                delay(1000L)
            }
        }
    }

    // =========================================================================
    // ACCIONES DE CONTROL (Invocadas desde Compose Wear OS)
    // =========================================================================

    /**
     * Inicia la sesión o avanza a la siguiente fase manualmente.
     */
    fun onNextPhaseOrStartClicked() {
        val currentState = _uiState.value

        // GUARDIA DE SEGURIDAD: Evita iniciar una sesión fantasma si no hay configuración
        if (!isSessionConfigured(currentState)) {
            Log.w("WearSessionViewModel", "Intento de inicio bloqueado: No existe sesión creada.")
            return
        }

        val now = System.currentTimeMillis()

        // Primera vez: Iniciar la sesión creada desde la fase de Calentamiento
        if (!currentState.isSessionStarted) {
            val warmupDuration =
                if (currentState.phaseTotalDurationSec > 0) currentState.phaseTotalDurationSec else 300L

            val updatedState = currentState.copy(
                isSessionStarted = true,
                isRunning = true,
                isPaused = false,
                isWaitingManualStart = false,
                currentPhase = SkateSessionPhase.WARMUP,
                phaseTotalDurationSec = warmupDuration,
                phaseTimeRemainingSec = warmupDuration
            )
            val newTimestamps = SyncTimestamps(overallStartTimeMs = now, phaseStartTimeMs = now)
            publishState(updatedState, newTimestamps)
            return
        }

        // Transición de fases
        val nextPhase = when (currentState.currentPhase) {
            SkateSessionPhase.WARMUP -> SkateSessionPhase.SKATE
            SkateSessionPhase.SKATE -> {
                if (currentState.currentRound >= currentState.totalRounds && currentState.totalRounds > 0) {
                    SkateSessionPhase.STRETCHING
                } else {
                    SkateSessionPhase.REST
                }
            }

            SkateSessionPhase.REST -> SkateSessionPhase.SKATE
            SkateSessionPhase.EXTRA_TIME -> SkateSessionPhase.STRETCHING
            SkateSessionPhase.STRETCHING -> SkateSessionPhase.COMPLETED
            SkateSessionPhase.COMPLETED -> SkateSessionPhase.COMPLETED
            SkateSessionPhase.NOT_STARTED -> SkateSessionPhase.WARMUP
        }

        val nextRound = if (currentState.currentPhase == SkateSessionPhase.REST) {
            currentState.currentRound + 1
        } else {
            currentState.currentRound
        }

        val newPhaseDurationSec = getDurationForPhase(nextPhase, currentState)

        val updatedState = currentState.copy(
            currentPhase = nextPhase,
            currentRound = nextRound,
            isRunning = nextPhase != SkateSessionPhase.COMPLETED,
            isPaused = false,
            isWaitingManualStart = false,
            phaseTotalDurationSec = newPhaseDurationSec,
            phaseTimeRemainingSec = newPhaseDurationSec
        )

        val newTimestamps = currentTimestamps.copy(phaseStartTimeMs = now)
        publishState(updatedState, newTimestamps)
    }

    /**
     * Pausa o reanuda la sesión ajustando los timestamps para evitar saltos.
     */
    fun onTogglePauseClicked() {
        val currentState = _uiState.value
        val willBePaused = !currentState.isPaused
        val now = System.currentTimeMillis()

        val updatedTimestamps = if (!willBePaused) {
            val elapsedGeneralMs = currentState.generalElapsedTimeSec * 1000L
            val elapsedPhaseMs =
                (currentState.phaseTotalDurationSec - currentState.phaseTimeRemainingSec) * 1000L

            SyncTimestamps(
                overallStartTimeMs = now - elapsedGeneralMs,
                phaseStartTimeMs = now - elapsedPhaseMs
            )
        } else {
            currentTimestamps
        }

        val updatedState = currentState.copy(isPaused = willBePaused)
        publishState(updatedState, updatedTimestamps)
    }

    /**
     * Finaliza la sesión manualmente.
     */
    fun onFinishSessionClicked() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(
            currentPhase = SkateSessionPhase.COMPLETED,
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = false
        )
        publishState(updatedState, currentTimestamps)
    }

    /**
     * Solicita abrir la app en el celular para configurar o crear una nueva sesión de Skate.
     */
    fun onRequestCreateSessionFromPhone() {
        viewModelScope.launch {
            try {
                val nodeClient = Wearable.getNodeClient(getApplication<Application>())
                val nodes = nodeClient.connectedNodes.await()
                val phoneNode = nodes.firstOrNull()

                if (phoneNode != null) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        data = "projectssk8://create_session".toUri()

                        // OBLIGATORIO: Especifica el nombre de paquete de la app del CELULAR
                        setPackage("com.zaysk8.projectssk8")

                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        remoteActivityHelper.startRemoteActivity(intent, phoneNode.id).await()
                        Log.d(
                            "RemoteActivity",
                            "Lanzamiento enviado exitosamente a ${phoneNode.displayName}"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "RemoteActivity",
                            "Error al abrir la app en el teléfono: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "WearSessionViewModel",
                    "Error al enviar evento de apertura al teléfono: ${e.message}"
                )
            }
        }
    }

    /**
     * Permite crear e iniciar una sesión predeterminada (3 rondas, 5 min calentamiento, 10 min skate)
     * en caso de que el usuario quiera patinar únicamente con el reloj.
     */
    fun startQuickLocalSession() {
        val now = System.currentTimeMillis()
        val defaultWarmup = 300L // 5 min
        val quickState = SkateSessionStateModel(
            isSessionStarted = true,
            isRunning = true,
            isPaused = false,
            isWaitingManualStart = false,
            currentPhase = SkateSessionPhase.WARMUP,
            currentRound = 1,
            totalRounds = 3,
            phaseTotalDurationSec = defaultWarmup,
            phaseTimeRemainingSec = defaultWarmup
        )
        val timestamps = SyncTimestamps(overallStartTimeMs = now, phaseStartTimeMs = now)
        publishState(quickState, timestamps)
    }

    private fun publishState(state: SkateSessionStateModel, timestamps: SyncTimestamps) {
        _uiState.value = state
        currentTimestamps = timestamps
        viewModelScope.launch {
            sessionSyncManager.updateSessionState(state, timestamps)
        }
    }

    private fun getDurationForPhase(
        phase: SkateSessionPhase,
        currentState: SkateSessionStateModel
    ): Long {
        return when (phase) {
            SkateSessionPhase.WARMUP -> 300L      // 5 min
            SkateSessionPhase.SKATE -> if (currentState.phaseTotalDurationSec > 0) currentState.phaseTotalDurationSec else 600L
            SkateSessionPhase.REST -> 180L        // 3 min
            SkateSessionPhase.EXTRA_TIME -> 300L  // 5 min
            SkateSessionPhase.STRETCHING -> 300L  // 5 min
            SkateSessionPhase.COMPLETED, SkateSessionPhase.NOT_STARTED -> 0L
        }
    }
}