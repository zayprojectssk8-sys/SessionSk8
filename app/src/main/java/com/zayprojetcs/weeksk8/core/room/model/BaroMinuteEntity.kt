package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- BARÓMETRO ---
@Entity(
    tableName = "baro_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class BaroMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val minPressureHpa: Float,
    val maxPressureHpa: Float,
    val minRelativeAltitudeM: Float,
    val maxRelativeAltitudeM: Float
)