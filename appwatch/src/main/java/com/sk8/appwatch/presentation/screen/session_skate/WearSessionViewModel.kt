package com.sk8.appwatch.presentation.screen.session_skate


import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import kotlin.time.Duration.Companion.milliseconds
import androidx.concurrent.futures.await as futureAwait
import kotlinx.coroutines.tasks.await as gmsAwait

class WearSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionSyncManager by lazy { SessionSyncManager(application.applicationContext) }
    private val remoteActivityHelper by lazy { RemoteActivityHelper(application.applicationContext) }

    private val _uiState = MutableStateFlow(SkateSessionStateModel())
    val uiState: StateFlow<SkateSessionStateModel> = _uiState.asStateFlow()

    private var currentTimestamps = SyncTimestamps()

    init {
        // 1. ESCUCHAR CAMBIOS Y RECUPERAR ESTADO INICIAL (SessionSyncManager lo hace internamente)
        observeRemoteState()

        // 2. INICIAR EL CRONÓMETRO LOCAL
        startLocalTicker()
    }

    /**
     * Valida si existe una sesión lista para ser iniciada o en progreso.
     * Incluye `hasActiveSessionConfig` para mostrar el botón en cuanto se sincroniza desde el celular.
     */
    fun isSessionConfigured(state: SkateSessionStateModel = _uiState.value): Boolean {
        return state.hasActiveSessionConfig ||
                state.isSessionStarted ||
                state.totalRounds > 0 ||
                state.phaseTotalDurationSec > 0 ||
                state.currentPhase != SkateSessionPhase.NOT_STARTED
    }

    private fun observeRemoteState() {
        sessionSyncManager.observeSessionState()
            .onEach { (model, timestamps) ->
                Log.d("WearSessionVM", "Estado recibido desde celular: hasConfig=${model.hasActiveSessionConfig}, phase=${model.currentPhase}, rounds=${model.totalRounds}")
                _uiState.value = model
                currentTimestamps = timestamps
            }
            .launchIn(viewModelScope)
    }

    private fun startLocalTicker() {
        viewModelScope.launch {
            while (true) {
                val state = _uiState.value

                if (state.isRunning && !state.isPaused && state.isSessionStarted) {
                    val now = System.currentTimeMillis()

                    val elapsedSec = if (currentTimestamps.overallStartTimeMs > 0) {
                        ((now - currentTimestamps.overallStartTimeMs) / 1000).coerceAtLeast(0L)
                    } else {
                        state.generalElapsedTimeSec + 1
                    }

                    val remainingSec = if (currentTimestamps.phaseStartTimeMs > 0 && state.phaseTotalDurationSec > 0) {
                        val elapsedPhaseSec = (now - currentTimestamps.phaseStartTimeMs) / 1000
                        (state.phaseTotalDurationSec - elapsedPhaseSec).coerceAtLeast(0L)
                    } else {
                        (state.phaseTimeRemainingSec - 1).coerceAtLeast(0L)
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            generalElapsedTimeSec = elapsedSec,
                            phaseTimeRemainingSec = remainingSec,
                            isWaitingManualStart = remainingSec == 0L && currentState.phaseTotalDurationSec > 0
                        )
                    }
                }

                delay(1000L.milliseconds)
            }
        }
    }

    // =========================================================================
    // ACCIONES DE CONTROL
    // =========================================================================

    /**
     * Acción del botón "Iniciar Sesión" / "Siguiente Fase" en el Reloj.
     */
    fun onNextPhaseOrStartClicked() {
        val currentState = _uiState.value

        if (!isSessionConfigured(currentState)) {
            Log.w("WearSessionVM", "Intento de inicio bloqueado: No existe sesión configurada.")
            return
        }

        val now = System.currentTimeMillis()

        // 1. PRIMER INICIO DE LA SESIÓN
        if (!currentState.isSessionStarted) {
            val initialPhase = if (currentState.currentPhase != SkateSessionPhase.NOT_STARTED) {
                currentState.currentPhase
            } else {
                SkateSessionPhase.WARMUP
            }

            val phaseDuration = if (currentState.phaseTotalDurationSec > 0) {
                currentState.phaseTotalDurationSec
            } else {
                getDurationForPhase(initialPhase, currentState)
            }

            val updatedState = currentState.copy(
                isSessionStarted = true,
                isRunning = true,
                isPaused = false,
                isWaitingManualStart = false,
                currentPhase = initialPhase,
                phaseTotalDurationSec = phaseDuration,
                phaseTimeRemainingSec = phaseDuration
            )
            val newTimestamps = SyncTimestamps(overallStartTimeMs = now, phaseStartTimeMs = now)

            startSensorTrackingService()
            publishState(updatedState, newTimestamps)
            return
        }

        // 2. TRANSICIÓN DE FASES
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

        if (nextPhase == SkateSessionPhase.COMPLETED) {
            stopSensorTrackingService()
        }

        val newTimestamps = currentTimestamps.copy(phaseStartTimeMs = now)
        publishState(updatedState, newTimestamps)
    }

    fun onTogglePauseClicked() {
        val currentState = _uiState.value
        val willBePaused = !currentState.isPaused
        val now = System.currentTimeMillis()

        val updatedTimestamps = if (!willBePaused) {
            val elapsedGeneralMs = currentState.generalElapsedTimeSec * 1000L
            val elapsedPhaseMs = (currentState.phaseTotalDurationSec - currentState.phaseTimeRemainingSec) * 1000L

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

    fun onFinishSessionClicked() {
        val currentState = _uiState.value
        val updatedState = currentState.copy(
            currentPhase = SkateSessionPhase.COMPLETED,
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = false
        )
        stopSensorTrackingService()
        publishState(updatedState, currentTimestamps)
    }

    fun onRequestCreateSessionFromPhone() {
        viewModelScope.launch {
            try {
                val nodeClient = Wearable.getNodeClient(getApplication<Application>())
                val nodes = nodeClient.connectedNodes.gmsAwait()
                val phoneNode = nodes.firstOrNull()

                if (phoneNode != null) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        data = "projectssk8://create_session".toUri()
                        setPackage("com.zaysk8.projectssk8")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    remoteActivityHelper.startRemoteActivity(intent, phoneNode.id).futureAwait()
                }
            } catch (e: Exception) {
                Log.e("WearSessionVM", "Error al solicitar apertura en teléfono: ${e.message}")
            }
        }
    }

    private fun startSensorTrackingService() {
        try {
            val context = getApplication<Application>().applicationContext
            val intent = Intent().apply {
                setClassName(context, "com.sk8.appwatch.service.SkateTrackingService")
                action = "ACTION_START_TRACKING"
            }
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e("WearSessionVM", "Error iniciando servicio de sensores: ${e.message}")
        }
    }

    private fun stopSensorTrackingService() {
        try {
            val context = getApplication<Application>().applicationContext
            val intent = Intent().apply {
                setClassName(context, "com.sk8.appwatch.service.SkateTrackingService")
                action = "ACTION_STOP_TRACKING"
            }
            context.stopService(intent)
        } catch (e: Exception) {
            Log.e("WearSessionVM", "Error deteniendo servicio de sensores: ${e.message}")
        }
    }

    private fun publishState(state: SkateSessionStateModel, timestamps: SyncTimestamps) {
        _uiState.value = state
        currentTimestamps = timestamps
        viewModelScope.launch {
            sessionSyncManager.updateSessionState(state, timestamps)
        }
    }

    /**
     * Utiliza las duraciones recibidas desde el celular (warmupDurationSec, skateDurationSec, etc.)
     * y recurre a valores por defecto solo si no fueron especificadas.
     */
    private fun getDurationForPhase(
        phase: SkateSessionPhase,
        currentState: SkateSessionStateModel
    ): Long {
        return when (phase) {
            SkateSessionPhase.WARMUP -> if (currentState.warmupDurationSec > 0) currentState.warmupDurationSec else 300L
            SkateSessionPhase.SKATE -> if (currentState.skateDurationSec > 0) currentState.skateDurationSec else 600L
            SkateSessionPhase.REST -> if (currentState.restDurationSec > 0) currentState.restDurationSec else 180L
            SkateSessionPhase.EXTRA_TIME -> 300L
            SkateSessionPhase.STRETCHING -> if (currentState.stretchingDurationSec > 0) currentState.stretchingDurationSec else 300L
            SkateSessionPhase.COMPLETED, SkateSessionPhase.NOT_STARTED -> 0L
        }
    }
}