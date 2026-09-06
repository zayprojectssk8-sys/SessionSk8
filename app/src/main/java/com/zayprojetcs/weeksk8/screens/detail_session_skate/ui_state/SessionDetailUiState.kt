package com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state

import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zaysk8.core.model.SkateSessionPhase
import com.zaysk8.core.model.SkateSessionStateModel

data class SensorDataPoint(
    val timestamp: Long,
    val value: Float // Ejemplo: Magnitud de Aceleración o Giroscopio
)

data class PhaseSensorSummary(
    val phase: SkateSessionPhase,
    val roundNumber: Int = 1,
    val totalSteps: Int = 0,
    val maxGForce: Float = 0f,        // Pico máximo de impacto / aceleración
    val avgGyroscope: Float = 0f,     // Rotación promedio
    val elevationGainMeters: Float = 0f, // Cambio de altitud (Barómetro)
    val movementPoints: List<SensorDataPoint> = emptyList() // Datos para la gráfica
)

/**
 * Estado UI principal para la pantalla de detalle de sesión.
 */
data class SessionDetailUiState(
    val session: RoomSession? = null,
    val skateSessionStateModel: SkateSessionStateModel? = null,
    val deviceWearable: DeviceWearable? = null,
    val activeState: ActiveSessionUiState = ActiveSessionUiState(),
    val formattedTotalTimer: String = "00:00:00",
    val formattedPhaseTimer: String = "00:00",
    val warmupStatus: PropertyStatus = PropertyStatus.PENDING,
    val skateRoundsStatus: PropertyStatus = PropertyStatus.PENDING,
    val cooldownStatus: PropertyStatus = PropertyStatus.PENDING,
    val phaseSummaries: Map<String, PhaseSensorSummary> = emptyMap(),
    val isLoading: Boolean = false,
    val permissionSessionGranted: Boolean = false,
    val wearOsUserConnected: Boolean = true,
    val errorMessage: String? = null,
    val userMessage: String? = null
)

/**
 * Estado en tiempo real sincronizado con SkateSessionService.
 */
data class ActiveSessionUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isSessionStarted: Boolean = false,
    val isWaitingUserAction: Boolean = false,
    val phase: SkateSessionPhase = SkateSessionPhase.WARMUP,
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