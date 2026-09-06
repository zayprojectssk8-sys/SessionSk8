package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomMagMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.MagMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertMagMinute(magMinuteEntity: MagMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomMagMinuteDb().insert(magMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}
