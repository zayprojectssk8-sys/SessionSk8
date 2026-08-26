package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zayprojetcs.weeksk8.core.room.model.RoomDay
import com.zayprojetcs.weeksk8.core.room.model_relation.DayWithRoundsTrick
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAppDao {

    @Insert
    fun insertDay(roomDay: RoomDay): Long?

    @Update
    fun updateDay(roomDay: RoomDay)

    @Query("SELECT * FROM R_Day ORDER BY closeDate DESC LIMIT 1")
    suspend fun getLastDaySkateByDate(): RoomDay?

    @Query("SELECT * FROM R_Day")
    fun getDaysSkateFlow(): Flow<List<RoomDay>>

    @Query("SELECT * FROM R_Day WHERE status='0' OR status='1'")
    fun getStartDatSkateFlow(): Flow<DayWithRoundsTrick?>
}