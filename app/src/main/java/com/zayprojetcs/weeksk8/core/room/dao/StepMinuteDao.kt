package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.zayprojetcs.weeksk8.core.room.model.StepMinuteEntity

@Dao
interface StepMinuteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepMinuteEntity): Long
}