package com.zayprojetcs.weeksk8.core.room.repo

import android.app.Application
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomDayDb
import com.zayprojetcs.weeksk8.core.room.model.RoomDay

suspend fun Application.repoRoomGetLastDaySkateByDate() = roomDayDb().getLastDaySkateByDate()

fun Application.repoRoomGetDaysSkateFlow() = roomDayDb().getDaysSkateFlow()
fun Application.repoRoomGetStartDayFlow() = roomDayDb().getStartDatSkateFlow()

fun Application.repoRoomInsertDay(roomDay: RoomDay) = roomDayDb().insertDay(roomDay)

fun Application.repoRoomUpdateDay(roomDay: RoomDay) = roomDayDb().updateDay(roomDay)
