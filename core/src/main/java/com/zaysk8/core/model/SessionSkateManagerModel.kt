package com.zaysk8.core.model

/**
 * Representa las distintas fases por las que transita una sesión de skate.
 */
enum class SessionPhase(val displayName: String) {
    WARMUP("Calentamiento"),
    SKATE_RUNNING("Patinaje"),
    REST_RUNNING("Descanso"),
    EXTRA_TIME_RUNNING("Tiempo Extra"),
    COOL_DOWN("Estiramiento Final"),
    FINISHED("Sesión Finalizada");

    /**
     * Indica si la fase actual pertenece a las rondas repetitivas de skate/descanso.
     */
    val isRoundPhase: Boolean
        get() = this == SKATE_RUNNING || this == REST_RUNNING
}


