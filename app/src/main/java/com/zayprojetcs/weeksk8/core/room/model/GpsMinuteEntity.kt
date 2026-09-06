package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- GPS ---
@Entity(
    tableName = "gps_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class GpsMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val minSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val minAltitudeMeters: Double,
    val maxAltitudeMeters: Double,
    val minAccuracyMeters: Float,
    val maxAccuracyMeters: Float,
    val totalDistanceMeters: Float
)