package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.model_relation.SessionWithRoundsTrick
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(roomDay: RoomSession): Long

    @Update
    fun updateSession(roomDay: RoomSession)

    @Query("SELECT * FROM R_Session ORDER BY closeDate DESC LIMIT 1")
    suspend fun getLastSessionSkateByDate(): RoomSession?

    @Query("SELECT * FROM R_Session WHERE idSession = :idSession")
    suspend fun getSessionSkateId(idSession: Long): RoomSession?

    @Query("SELECT * FROM R_Session WHERE idSession = :idSession")
    fun getSessionSkateIdFlow(idSession: Long): Flow<RoomSession?>

    @Query("SELECT * FROM R_Session")
    fun getSessionSkateFlow(): Flow<List<RoomSession>>

    @Query("SELECT * FROM R_Session WHERE status='0' OR status='1'")
    fun getStartSessionSkateFlow(): Flow<SessionWithRoundsTrick?>

    @Query("SELECT * FROM R_Session WHERE status='2'")
    fun getHistorySessionSkateFlow(): Flow<SessionWithRoundsTrick?>

    @Query("SELECT COUNT(*) FROM R_Session WHERE status='2'")
    fun getHistoryCountSessionSkateFlow(): Flow<Int>
}