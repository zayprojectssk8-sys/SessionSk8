package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomGyroMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.GyroMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertGyroMinute(gyroMinuteEntity: GyroMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomGyroMinuteDb().insert(gyroMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}
