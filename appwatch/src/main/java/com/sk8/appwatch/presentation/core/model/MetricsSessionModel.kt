package com.sk8.appwatch.presentation.core.model


data class MetricsSessionModel(
    val bpm: Int = 0,
    val calories: Double = 0.0,
    val currentSpeedKmH: Float = 0f,
    val maxSpeedKmH: Float = 0f,
    val avgSpeedKmH: Float = 0f,
    val distanceMeters: Double = 0.0, // Metros exactos
    val distanceKm: Double = 0.0,       // Kilómetros formateados
    val activeTimeSeconds: Long = 0,
    val restTimeSeconds: Long = 0,
    val isCurrentlyActive: Boolean = false,
    val fallCount: Int = 0
)