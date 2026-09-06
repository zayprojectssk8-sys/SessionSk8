package com.zayprojetcs.weeksk8.core.room.model.combine

data class CombinedMinuteMetrics(
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,

    // Acelerómetro
    val minImpactG: Float?,
    val maxImpactG: Float?,
    val minBodyAccel: Float?,
    val maxBodyAccel: Float?,
    val minVibration: Float?,
    val maxVibration: Float?,

    // Activity Recognition
    val detectedActivity: String?,
    val maxActivityConfidence: Int?,

    // Barómetro
    val minPressureHpa: Float?,
    val maxPressureHpa: Float?,
    val minRelativeAltitudeM: Float?,
    val maxRelativeAltitudeM: Float?,

    // GPS
    val minSpeedKmh: Float?,
    val maxSpeedKmh: Float?,
    val totalDistanceMeters: Float?,
    val gpsAccuracyMeters: Float?,

    // Giroscopio
    val minRotationDps: Float?,
    val maxRotationDps: Float?,
    val avgRotationDps: Float?,

    // Magnetómetro
    val minFieldMicroTesla: Float?,
    val maxFieldMicroTesla: Float?,
    val maxHeadingDegrees: Float?,

    // Step Detector
    val stepCount: Int?,
    val maxCadenceSpm: Float?
)