package com.zayprojetcs.weeksk8.core.room.model_relation

import androidx.room.Embedded
import androidx.room.Relation
import com.zayprojetcs.weeksk8.core.room.model.RoomDay
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick

data class DayWithRoundsTrick(
    @Embedded
    val roomDay: RoomDay,
    @Relation(
        entity = RoomRoundTrick::class,
        parentColumn = "idDay",
        entityColumn = "idOwnerDay"
    )
    val roomRoundTrickList: List<RoundTrick> = arrayListOf()
)

data class RoundTrick(
    @Embedded
    val roomRoundTrick: RoomRoundTrick,

    @Relation(
        parentColumn = "idOwnerTrick",
        entityColumn = "idTrick"
    )
    val roomTrick: RoomTrick?
)