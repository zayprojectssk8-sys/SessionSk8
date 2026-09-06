package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ---  ACELERÓMETRO ---
@Entity(
    tableName = "accel_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class AccelMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String, // "WARMUP" o "SKATE"
    val minuteIndex: Int,
    val minImpactG: Float,
    val maxImpactG: Float,
    val minBodyAccel: Float,
    val maxBodyAccel: Float,
    val minVibration: Float,
    val maxVibration: Float
)