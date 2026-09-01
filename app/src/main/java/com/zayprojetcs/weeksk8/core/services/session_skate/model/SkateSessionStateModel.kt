package com.zayprojetcs.weeksk8.core.services.session_skate.model

enum class SkateSessionPhase(val displayName: String) {
    WARMUP("Calentamiento"),
    SKATE("Patinaje"),
    REST("Descanso"),
    EXTRA_TIME("Tiempo Extra"),
    STRETCHING("Estiramiento"),
    COMPLETED("Finalizado")
}

data class SkateSessionStateModel(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isSessionStarted: Boolean = false,
    val isWaitingManualStart: Boolean = true, // TRUE cuando una fase termina y espera inicio manual del usuario
    val currentPhase: SkateSessionPhase = SkateSessionPhase.WARMUP,
    val currentRound: Int = 1,
    val totalRounds: Int = 0,
    val generalElapsedTimeSec: Long = 0L,     // Cronómetro general activo
    val phaseTimeRemainingSec: Long = 0L,     // Tiempo restante de la ronda/fase actual
    val phaseTotalDurationSec: Long = 0L
)