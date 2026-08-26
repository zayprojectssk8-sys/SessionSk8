package com.sk8.appwatch.presentation.screen.skate_session.ui_state

data class SkateSessionUiStateModel(
    val isTracking: Boolean = false,
    val bpm: Int = 0,
    val calories: Double = 0.0,
    val currentSpeedKmH: Float = 0f,
    val maxSpeedKmH: Float = 0f,
    val avgSpeedKmH: Float = 0f,
    val distanceMeters: Double = 0.0,
    val distanceKm: Double = 0.0,
    val activeTimeSeconds: Long = 0,
    val restTimeSeconds: Long = 0,
    val isCurrentlyActive: Boolean = false,
    val fallCount: Int = 0,
)