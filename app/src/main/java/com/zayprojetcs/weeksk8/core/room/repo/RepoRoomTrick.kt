package com.zayprojetcs.weeksk8.core.room.repo

import android.app.Application
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomTrickDb
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zaysk8.core.utils.OperationResult

fun Application.repoRoomGetListTrickFlow() = roomTrickDb().getListTrickFlow()
fun Application.repoRoomGetListTrickFloor() = roomTrickDb().getListTrickFloor()
fun Application.repoRoomGetListTrickUnlock() = roomTrickDb().getListTrickUnlock()

fun Application.repoRoomGetListTrickTriedUnlock() = roomTrickDb().getListTrickTriedUnlock()
fun Application.repoRoomGetListTrickGrindSlideUnlock() =
    roomTrickDb().getListTrickGlideGrindUnlock()

fun Application.repoRoomGetListTrickSwitchNollieUnlock() =
    roomTrickDb().getListTrickSwitchFlowUnlock()

suspend fun Application.repoRoomInsertListTrick(listRoomTrick: List<RoomTrick>): OperationResult<Boolean> {
    return try {
        val ids = roomTrickDb().insertListTrick(listRoomTrick)
        if (ids.size == listRoomTrick.size && ids.none { it == -1L }) {
            OperationResult.Success
        } else {
            OperationResult.Error("No se pudieron guardar todos los trucos correctamente.")
        }
    } catch (e: Exception) {
        OperationResult.Error(
            message = "Error al guardar la lista de trucos en la base de datos",
            throwable = e
        )
    }
}

fun Application.repoRoomUpdateTrick(roomTrick: RoomTrick) = roomTrickDb().updateTrick(roomTrick)