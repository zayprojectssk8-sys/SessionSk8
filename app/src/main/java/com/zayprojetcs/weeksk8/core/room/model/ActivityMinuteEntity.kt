package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ---  ACTIVITY RECOGNITION ---
@Entity(
    tableName = "activity_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class ActivityMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val detectedActivity: String,
    val minConfidence: Int,
    val maxConfidence: Int
)