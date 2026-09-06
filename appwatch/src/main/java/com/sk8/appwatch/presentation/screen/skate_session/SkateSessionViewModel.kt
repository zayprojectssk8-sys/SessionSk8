package com.sk8.appwatch.presentation.screen.skate_session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

class SkateSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionSyncManager by lazy { SessionSyncManager(application.applicationContext) }

    private val _uiState = MutableStateFlow(SkateSessionStateModel())
    val uiState: StateFlow<SkateSessionStateModel> = _uiState.asStateFlow()

    private var currentTimestamps = SyncTimestamps()

    init {
        observeRemoteState()
        startLocalTicker()
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
                    val remainingSec = if (currentTimestamps.phaseStartTimeMs > 0 && state.phaseTotalDurationSec > 0) {
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

                delay(1000L.milliseconds)
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
        val now = System.currentTimeMillis()

        // Primera vez: Iniciar la sesión desde la fase de Calentamiento
        if (!currentState.isSessionStarted) {
            val warmupDuration = if (currentState.phaseTotalDurationSec > 0) currentState.phaseTotalDurationSec else 300L

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
            // Reanudando: Reajustamos los timestamps absolutos restando el tiempo ya transcurrido
            // para que la diferencia (now - startTime) no sume el tiempo que estuvo pausado.
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

    private fun publishState(state: SkateSessionStateModel, timestamps: SyncTimestamps) {
        _uiState.value = state
        currentTimestamps = timestamps
        viewModelScope.launch {
            sessionSyncManager.updateSessionState(state, timestamps)
        }
    }

    /**
     * Obtiene la duración de la fase respetando valores previos configurados desde el celular.
     */
    private fun getDurationForPhase(phase: SkateSessionPhase, currentState: SkateSessionStateModel): Long {
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