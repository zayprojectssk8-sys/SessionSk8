package com.zayprojetcs.weeksk8.core.room.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickDistributionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickOrderMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickTrackingMode
import com.zaysk8.core.utils.getFormatDurationSession
import kotlinx.serialization.Serializable


enum class StatusSession(
    val status: String,
    val statusText: String,
    val statusColor: Color
) {
    CREATED("0", "CREADO", Color.DarkGray),
    STARTED("1", "INICIADO", Color.Green),
    CLOSED("2", "CERRADO", Color.Blue)
}

@Entity(tableName = "R_Session")
@Serializable
data class RoomSession(
    @PrimaryKey(autoGenerate = true)
    var idSession: Long = 0,

    val durationSessionMinutes: Int? = null,
    val warmupMinutes: Int = 0,
    val cooldownMinutes: Int = 0,
    val calculatedRounds: Int = 0,
    val totalSkateTime: Int = 0,
    val totalRestTime: Int = 0,
    val marginMinutes: Int? = null,
    val selectedCustomTricksCount: Int = 0,
    val unlockedTricksCount: Int = 0,
    val trickTrackingMode: Int = 0,
    val trickOrderMode: Int = 0,
    val trickDistributionMode: Int = 0,

    val status: String = "",
    val createDate: Long = 0,
    val startDate: Long = 0,
    val closeDate: Long = 0,
) {
    fun getDurationTimeSession(): String {
        return if (durationSessionMinutes == null) "Abierta (Sin límite)"
        else getFormatDurationSession(durationSessionMinutes)
    }
    fun getStatusSession(): StatusSession {
        return when (status) {
            "1" -> StatusSession.STARTED
            "2" -> StatusSession.CLOSED
            else -> StatusSession.CREATED
        }
    }

    fun getTrickTrackingMode(): TrickTrackingMode {
        return when (trickTrackingMode) {
            1 -> TrickTrackingMode.REST_CHECKING
            else -> TrickTrackingMode.POST_SESSION
        }
    }

    fun getTrickOrderMode(): TrickOrderMode {
        return when (trickOrderMode) {
            1 -> TrickOrderMode.SEQUENTIAL
            else -> TrickOrderMode.RANDOM
        }
    }

    fun getTrickDistributionMode(): TrickDistributionMode {
        return when (trickDistributionMode) {
            1 -> TrickDistributionMode.ROUNDS_PER_TRICK
            else -> TrickDistributionMode.ALL_TRICKS_PER_ROUND
        }
    }
}