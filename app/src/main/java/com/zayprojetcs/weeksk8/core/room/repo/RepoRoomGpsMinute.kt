package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomGpsMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.GpsMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertGpsMinute(gpsMinuteEntity: GpsMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomGpsMinuteDb().insert(gpsMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}
