package com.zayprojetcs.weeksk8.screens.menu.ui_state.model

import com.zayprojetcs.weeksk8.utils.DetectedWearable

data class MenuUiStateModel(
    val isLoading: Boolean = false,
    val connectedDevice: DetectedWearable? = null,
)