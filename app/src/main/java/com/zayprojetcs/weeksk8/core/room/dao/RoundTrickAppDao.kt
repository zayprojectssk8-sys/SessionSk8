package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick

@Dao
interface RoundTrickAppDao {

    @Insert
    fun insertListRoundTrick(listRoomRoundTrick: List<RoomRoundTrick>): List<Long>

    @Update
    fun updateRoundTrick(roomRoundTrick: RoomRoundTrick)
}