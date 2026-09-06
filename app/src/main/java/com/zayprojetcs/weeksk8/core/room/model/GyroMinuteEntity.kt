package com.zayprojetcs.weeksk8.core.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// --- GIROSCÓPIO ---
@Entity(
    tableName = "gyro_minute_metrics",
    indices = [Index(value = ["sessionId", "phaseId", "minuteIndex"])]
)
data class GyroMinuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val phaseId: String,
    val minuteIndex: Int,
    val minRotationDps: Float,
    val maxRotationDps: Float,
    val avgRotationDps: Float
)