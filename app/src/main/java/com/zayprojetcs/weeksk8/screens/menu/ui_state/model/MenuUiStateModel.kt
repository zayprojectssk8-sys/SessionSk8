package com.zayprojetcs.weeksk8.screens.menu.ui_state.model

import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.core.room.model_relation.SessionWithRoundsTrick

data class MenuUiStateModel(
    val isLoading: Boolean = false,
    val historyCountSession: Int = 0,
    val connectedDevice: DeviceWearable? = null,
    val sessionWithRoundsTrick: SessionWithRoundsTrick? = null,
)