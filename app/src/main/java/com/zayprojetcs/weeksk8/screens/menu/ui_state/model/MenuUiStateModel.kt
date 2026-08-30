package com.zayprojetcs.weeksk8.screens.menu.ui_state.model

import com.zayprojetcs.weeksk8.core.room.model_relation.SessionWithRoundsTrick
import com.zayprojetcs.weeksk8.utils.DetectedWearable

data class MenuUiStateModel(
    val isLoading: Boolean = false,
    val historyCountSession: Int = 0,
    val connectedDevice: DetectedWearable? = null,
    val sessionWithRoundsTrick: SessionWithRoundsTrick? = null,
)