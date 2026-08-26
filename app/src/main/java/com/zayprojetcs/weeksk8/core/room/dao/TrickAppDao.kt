package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import kotlinx.coroutines.flow.Flow

@Dao
interface TrickAppDao {

    @Insert
    fun insertListTrick(listRoomTrick: List<RoomTrick>)

    @Update
    fun updateTrick(roomTrick: RoomTrick)

    @Query("SELECT * FROM R_Trick WHERE status IN ('1', '2')")
    fun getListTrickFlow(): Flow<List<RoomTrick>>

    @Query("SELECT * FROM R_Trick WHERE status IN ('1', '2') AND typeTrick='Floor'")
    fun getListTrickFloor(): List<RoomTrick>

    @Query("SELECT * FROM R_Trick WHERE status = '1' AND typeTrick='Floor'")
    fun getListTrickTriedUnlock(): List<RoomTrick>
    @Query("SELECT * FROM R_Trick WHERE status = '2' AND typeTrick='Floor'")
    fun getListTrickUnlock(): List<RoomTrick>

    @Query("SELECT * FROM R_Trick WHERE status IN ('1', '2') AND typeTrick IN ('Grind','Slide')")
    fun getListTrickGlideGrindUnlock(): List<RoomTrick>

    @Query("SELECT * FROM R_Trick WHERE status IN ('1', '2') AND typeTrick IN ('Grind','Slide','Floor')")
    fun getListTrickSwitchFlowUnlock(): List<RoomTrick>
}