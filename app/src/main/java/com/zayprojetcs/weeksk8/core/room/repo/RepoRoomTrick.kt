package com.zayprojetcs.weeksk8.core.room.repo

import android.app.Application
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomTrickDb
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick

fun Application.repoRoomGetListTrickFlow() = roomTrickDb().getListTrickFlow()
fun Application.repoRoomGetListTrickFloor() = roomTrickDb().getListTrickFloor()
fun Application.repoRoomGetListTrickUnlock() = roomTrickDb().getListTrickUnlock()

fun Application.repoRoomGetListTrickTriedUnlock() = roomTrickDb().getListTrickTriedUnlock()
fun Application.repoRoomGetListTrickGrindSlideUnlock() = roomTrickDb().getListTrickGlideGrindUnlock()
fun Application.repoRoomGetListTrickSwitchNollieUnlock() = roomTrickDb().getListTrickSwitchFlowUnlock()

fun Application.repoRoomInsertListTrick(listRoomTrick: List<RoomTrick>) = roomTrickDb().insertListTrick(listRoomTrick)
fun Application.repoRoomUpdateTrick(roomTrick: RoomTrick) = roomTrickDb().updateTrick(roomTrick)