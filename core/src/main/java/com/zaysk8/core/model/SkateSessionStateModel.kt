package com.zaysk8.core.model

enum class SkateSessionPhase(val displayName: String) {
    NOT_STARTED("Sin iniciar"),
    WARMUP("Calentamiento"),
    SKATE("Patinaje"),
    REST("Descanso"),
    EXTRA_TIME("Tiempo Extra"),
    STRETCHING("Estiramiento"),
    COMPLETED("Finalizado")
}

data class SkateSessionStateModel(
    // Flag de presencia de configuración
    val hasActiveSessionConfig: Boolean = false,

    // Estados de ejecución
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isSessionStarted: Boolean = false,
    val isWaitingManualStart: Boolean = true,

    // Información del progreso actual
    val currentPhase: SkateSessionPhase = SkateSessionPhase.NOT_STARTED,
    val currentRound: Int = 1,
    val totalRounds: Int = 0,

    // Cronómetros dinámicos (calculados/mostrados en UI)
    val generalElapsedTimeSec: Long = 0L,
    val phaseTimeRemainingSec: Long = 0L,
    val phaseTotalDurationSec: Long = 0L,

    // =========================================================================
    // CONFIGURACIÓN DE FASES (Permite al reloj saber las duraciones al avanzar)
    // =========================================================================
    val warmupDurationSec: Long = 300L,     // 5 min por defecto
    val skateDurationSec: Long = 600L,      // 10 min por defecto
    val restDurationSec: Long = 180L,       // 3 min por defecto
    val stretchingDurationSec: Long = 300L, // 5 min por defecto

    // =========================================================================
    // TIMESTAMPS PARA PRECISIÓN Y SINCRONIZACIÓN BIDIRECCIONAL
    // =========================================================================
    val overallStartTimeMs: Long = 0L,
    val phaseStartTimeMs: Long = 0L,
    val lastUpdatedMs: Long = 0L            // Para prevenir sobreescritura por paquetes viejos
)