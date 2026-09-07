package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomAccelMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.AccelMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertAccelMinute(accelMinuteEntity: AccelMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomAccelMinuteDb().insert(accelMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success(true)
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}
