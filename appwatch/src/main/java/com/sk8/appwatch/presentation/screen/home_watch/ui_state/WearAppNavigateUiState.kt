package com.sk8.appwatch.presentation.screen.home_watch.ui_state

sealed interface WearAppNavigateUiState{
    object AppEmptyCommunication : WearAppNavigateUiState
    object LinkDevicePhone : WearAppNavigateUiState
    object SessionMetrics : WearAppNavigateUiState
}