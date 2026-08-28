package com.sk8.appwatch.presentation.screen.link_device_phone.ui_state

sealed interface LinkDevicePhoneUiState {
    data object RequestPermissions : LinkDevicePhoneUiState
}