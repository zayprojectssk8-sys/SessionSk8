package com.zayprojetcs.weeksk8.core.room.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


enum class StatusRoundTrick(
    val status: String,
    val statusText: String,
    val statusColor: Color
) {
    STARTED("1", "INICIADO", Color.Green),
    CLOSED("2", "CERRADO", Color.Blue)
}


@Entity(
    tableName = "R_RoundTrick",
    foreignKeys = [
        ForeignKey(
            entity = RoomDay::class,
            parentColumns = ["idDay"],
            childColumns = ["idOwnerDay"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomTrick::class,
            parentColumns = ["idTrick"],
            childColumns = ["idOwnerTrick"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@Serializable
data class RoomRoundTrick(
    @PrimaryKey(autoGenerate = true)
    var idRoundTrick: Long = 0,

    var idOwnerDay: Long,
    var idOwnerTrick: Long?,

    val status: String = "",
    val attempts: Int = 5,
    val round: Int = 0,
    val countTriedSuccess: Int = 0,
    val countTriedFail: Int = 0,
) {
    fun getStatusRoundTrick(): StatusRoundTrick {
        return when (status) {
            "2" -> StatusRoundTrick.CLOSED
            else -> StatusRoundTrick.STARTED
        }
    }
}
