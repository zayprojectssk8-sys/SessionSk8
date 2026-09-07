package com.zayprojetcs.weeksk8.core.room.repo


import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomStepMinuteDb
import com.zayprojetcs.weeksk8.core.room.model.StepMinuteEntity
import com.zaysk8.core.utils.OperationResult

suspend fun Context.repoRoomInsertStepMinute(stepMinuteEntity: StepMinuteEntity): OperationResult<Boolean> {
    return try {
        val id = roomStepMinuteDb().insert(stepMinuteEntity)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success(true)
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}
