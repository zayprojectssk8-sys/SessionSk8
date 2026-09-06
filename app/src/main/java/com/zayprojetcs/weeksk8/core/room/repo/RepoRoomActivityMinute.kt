package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomActivityMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.ActivityMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertActivityMinute(activityMinuteEntity: ActivityMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomActivityMinuteDb().insert(activityMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}