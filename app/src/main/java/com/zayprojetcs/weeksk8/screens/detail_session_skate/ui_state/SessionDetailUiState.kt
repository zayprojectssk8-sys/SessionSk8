package com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state

import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zaysk8.core.model.SessionPhase
/**
 * Estado UI principal para la pantalla de detalle de sesión.
 */
data class SessionDetailUiState(
    val session: RoomSession? = null,
    val activeState: ActiveSessionUiState = ActiveSessionUiState(),
    val formattedTotalTimer: String = "00:00:00",
    val formattedPhaseTimer: String = "00:00",
    val warmupStatus: PropertyStatus = PropertyStatus.PENDING,
    val skateRoundsStatus: PropertyStatus = PropertyStatus.PENDING,
    val cooldownStatus: PropertyStatus = PropertyStatus.PENDING,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Estado en tiempo real sincronizado con SkateSessionService.
 */
data class ActiveSessionUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isSessionStarted: Boolean = false,
    val isWaitingUserAction: Boolean = false,
    val phase: SessionPhase = SessionPhase.WARMUP,
    val currentRound: Int = 1,
    val totalRounds: Int = 0,
    val generalElapsedTimeSec: Long = 0L,
    val phaseTimeRemainingSec: Long = 0L
)

/**
 * Estado visual de cada módulo dentro del LazyColumn.
 */
enum class PropertyStatus(val label: String) {
    PENDING("PENDIENTE"),
    IN_PROGRESS("EN PROGRESO"),
    COMPLETED("COMPLETADO"),
    DISABLED("NO CONFIGURADO")
}