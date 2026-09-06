package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ---  MAGNETÓMETRO ---
@Entity(
    tableName = "mag_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class MagMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val minFieldMicroTesla: Float,
    val maxFieldMicroTesla: Float,
    val minHeadingDegrees: Float,
    val maxHeadingDegrees: Float
)