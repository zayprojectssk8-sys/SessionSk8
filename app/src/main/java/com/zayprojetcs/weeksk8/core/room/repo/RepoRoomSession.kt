package com.zayprojetcs.weeksk8.core.room.repo

import android.app.Application
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomSessionDb
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zaysk8.core.utils.OperationResult

suspend fun Application.repoRoomGetLastSessionSkateByDate() = roomSessionDb().getLastSessionSkateByDate()
 fun Application.repoRoomGetHistorySessionSkate() = roomSessionDb().getHistorySessionSkateFlow()
 fun Application.repoRoomCountHistorySessionSkate() = roomSessionDb().getHistoryCountSessionSkateFlow()

fun Application.repoRoomGetSessionsSkateFlow() = roomSessionDb().getSessionSkateFlow()
fun Application.repoRoomGetStartSessionFlow() = roomSessionDb().getStartSessionSkateFlow()

suspend fun Application.repoRoomGetIdSession(idSession: Long) = roomSessionDb().getSessionSkateId(idSession)
suspend fun Application.repoRoomGetIdSessionFlow(idSession: Long) = roomSessionDb().getSessionSkateIdFlow(idSession)
suspend fun Application.repoRoomInsertSession(roomSession: RoomSession): OperationResult<Boolean> {
    return try {
        val id = roomSessionDb().insertSession(roomSession)
        if (id == -1L) OperationResult.Error("No se pudo guardar la sesión correctamente.")
        else OperationResult.Success
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la sesión en la base de datos",
            throwable = e
        )
    }
}

fun Application.repoRoomUpdateSession(roomSession: RoomSession) =
    roomSessionDb().updateSession(roomSession)
