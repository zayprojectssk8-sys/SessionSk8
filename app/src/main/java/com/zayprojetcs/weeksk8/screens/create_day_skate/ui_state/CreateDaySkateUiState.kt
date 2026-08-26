package com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state

import com.zayprojetcs.weeksk8.core.model.TFMTrick

sealed class CreateDaySkateUiState {

    object GetLastDaySkate : CreateDaySkateUiState()
    object GetUnlockTrickList : CreateDaySkateUiState()
    object GetTriedUnlockTrickList : CreateDaySkateUiState()
    object GetTriedGrindSlideUnlockTrickList : CreateDaySkateUiState()
    object GetTriedSwitchNollieUnlockTrickList : CreateDaySkateUiState()
    object RegisterDay : CreateDaySkateUiState()
    data class AddUnlockTrick(val showView: Boolean) : CreateDaySkateUiState()
    data class SetUnlockTrickSelectionList(val unlockTrickList: List<TFMTrick>) : CreateDaySkateUiState()
    data class SetTriedUnlockTrickSelectionList(val unlockTriedTrickList: List<TFMTrick>) : CreateDaySkateUiState()
    data class SetTriedUnlockGrindSlideTrickSelectionList(val unlockGrindSlideTrickList: List<TFMTrick>) : CreateDaySkateUiState()
    data class SetTriedUnlockSwitchNollieTrickSelectionList(val unlockSwitchNollieTrickList: List<TFMTrick>) : CreateDaySkateUiState()
}