package com.zayprojetcs.weeksk8.screens.start_day_skate.ui_state

import com.zayprojetcs.weeksk8.core.room.model_relation.RoundTrick

sealed class StartDaySkateUiState {
    object GetTricksDay : StartDaySkateUiState()
    object StartDay : StartDaySkateUiState()
    object StopDay : StartDaySkateUiState()
    object CreateRound : StartDaySkateUiState()
    object CreateNextRound : StartDaySkateUiState()
    data class SendRoundTrick(val roundTrick: RoundTrick, val numTried: Int) : StartDaySkateUiState()
}