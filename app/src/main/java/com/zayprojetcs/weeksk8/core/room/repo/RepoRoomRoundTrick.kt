package com.zayprojetcs.weeksk8.core.room.repo

import android.app.Application
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomRoundTrickDb
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick

fun Application.repoRoomInsertListRoundTrick(listRoomRoundTrick: List<RoomRoundTrick>) =
    roomRoundTrickDb().insertListRoundTrick(listRoomRoundTrick = listRoomRoundTrick)

fun Application.repoRoomInsertRoundTrick(roomRoundTrick: RoomRoundTrick) =
    roomRoundTrickDb().updateRoundTrick(roomRoundTrick)
