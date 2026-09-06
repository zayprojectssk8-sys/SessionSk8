package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ---  STEP DETECTOR ---

@Entity(
    tableName = "step_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class StepMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val stepCount: Int,
    val minCadenceSpm: Float,
    val maxCadenceSpm: Float
)