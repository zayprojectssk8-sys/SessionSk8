package com.zayprojetcs.weeksk8.core.room.model_relation

import androidx.room.Embedded
import androidx.room.Relation
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick

data class SessionWithRoundsTrick(
    @Embedded
    val roomSession: RoomSession,
    @Relation(
        entity = RoomRoundTrick::class,
        parentColumn = "idSession",
        entityColumn = "idOwnerSession"
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