package com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.model

import com.zayprojetcs.weeksk8.core.model.TFMTrick
import com.zayprojetcs.weeksk8.core.room.model.RoomDay
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.CreateDaySkateUiState

data class CreateDaySkateUiStateModel(
    val currentRoomDay: RoomDay? = null,
    val trickEnableList: List<RoomTrick> = arrayListOf(),
    val unlockTrickList: List<RoomTrick> = arrayListOf(),
    val unlockNewTrickList: List<RoomTrick> = arrayListOf(),
    val unlockTriedTrickList: List<RoomTrick> = arrayListOf(),
    val unlockNewTriedTrickList: List<RoomTrick> = arrayListOf(),
    val unlockTriedGrindSlideTrickList: List<RoomTrick> = arrayListOf(),
    val unlockNewTriedGrindSlideTrickList: List<RoomTrick> = arrayListOf(),
    val unlockTriedSwitchNollieTrickList: List<RoomTrick> = arrayListOf(),
    val unlockNewTriedSwitchNollieTrickList: List<RoomTrick> = arrayListOf(),
    val showViewAddUnlockTrick: Boolean = false,
)