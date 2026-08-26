package com.zayprojetcs.weeksk8.core.room.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zayprojetcs.weeksk8.utils.DATE_FORMAT_dd_MM_yyyy_HH_mm_ss
import com.zayprojetcs.weeksk8.utils.dateTimeFormat
import kotlinx.serialization.Serializable


enum class StatusDay(
    val status: String,
    val statusText: String,
    val statusColor: Color
) {
    CREATED("0", "CREADO", Color.DarkGray),
    STARTED("1", "INICIADO", Color.Green),
    CLOSED("2", "CERRADO", Color.Blue)
}

enum class TypeActivityDay(
    val numDay: Int,
    val statusText: String
) {
    REVIEWING_FLOOR_TRICKS(1, "REPASANDO TRUCOS DE PISO"),
    UNLOCKED_FLOOR_TRICKS(2, "DESBLOQUEANDO TRUCOS DE PISO"),
    GRIND_AND_SLIDE_TRICKS(3, "REPASANDO Y DESBLOQUEANDO TRUCOS GRIND AND SLIDE"),
    SWITCH_TRICKS(4, "PRACTICANDO SWITCH"),
}

@Entity(tableName = "R_Day")
@Serializable
data class RoomDay(
    @PrimaryKey(autoGenerate = true)
    var idDay: Long = 0,

    val status: String = "",
    val createDate: Long = 0,
    val startDate: Long = 0,
    val closeDate: Long = 0,
    val typeActivityDay: Int = 0,
    val countRound: Int = 0
) {
    fun getStatusDay(): StatusDay {
        return when (status) {
            "1" -> StatusDay.STARTED
            "2" -> StatusDay.CLOSED
            else -> StatusDay.CREATED
        }
    }

    fun getTypeActivityDay(): TypeActivityDay {
        return when (typeActivityDay) {
            2 -> TypeActivityDay.UNLOCKED_FLOOR_TRICKS
            3 -> TypeActivityDay.GRIND_AND_SLIDE_TRICKS
            4 -> TypeActivityDay.SWITCH_TRICKS
            else -> TypeActivityDay.REVIEWING_FLOOR_TRICKS
        }
    }

    fun getNextTypeDay(): Int {
        return when (getTypeActivityDay().numDay) {
            TypeActivityDay.SWITCH_TRICKS.numDay -> TypeActivityDay.REVIEWING_FLOOR_TRICKS.numDay
            else -> getTypeActivityDay().numDay.plus(1)
        }
    }

    fun getFormatCreateDate() = DATE_FORMAT_dd_MM_yyyy_HH_mm_ss.dateTimeFormat(createDate)
    fun getFormatStartDate() = DATE_FORMAT_dd_MM_yyyy_HH_mm_ss.dateTimeFormat(startDate)
    fun getFormatCloseDate() = DATE_FORMAT_dd_MM_yyyy_HH_mm_ss.dateTimeFormat(closeDate)

}
