package com.zayprojetcs.weeksk8.core.room.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


enum class StatusTrick(
    val status: String,
    val statusText: String,
    val statusColor: Color
) {
    LOCK("0", "BLOQUEADO", Color.LightGray),
    TRYING("1", "INTENTANDO", Color.Blue),
    UNLOCK("2", "DESBLOQUEADO", Color.Green)
}

enum class RoomTypeTrick(
    var typeTrickName: String
) {
    FLOOR(typeTrickName = "Floor"),
    GRIND(typeTrickName = "Grind"),
    SLIDE(typeTrickName = "Slide"),
    GRABS(typeTrickName = "Grabs"),
    BALANCE(typeTrickName = "Balance"),
}

@Entity(tableName = "R_Trick")
@Serializable
data class RoomTrick(
    @PrimaryKey(autoGenerate = true)
    var idTrick: Long = 0,

    val realName: String = "",
    val akaName: String = "",
    val difficulty: Float = 0.0f,
    val typeTrick: String = "",
    val status: String = "",

    val startDateTrying: Long = 0,
    val dateUnlock: Long = 0,

    val countTriedAttempts: Int = 0,

    val countUnlockTriedSuccess: Int = 0,
    val countUnlockTriedFail: Int = 0,
) {
    fun getStatusTrick(): StatusTrick {
        return when (status) {
            "1" -> StatusTrick.TRYING
            "2" -> StatusTrick.UNLOCK
            else -> StatusTrick.LOCK
        }
    }
}