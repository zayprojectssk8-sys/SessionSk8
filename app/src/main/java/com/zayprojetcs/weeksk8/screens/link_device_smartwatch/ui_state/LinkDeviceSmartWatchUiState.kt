package com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state

import com.zayprojetcs.weeksk8.utils.DetectedWearable

sealed interface LinkDeviceSmartWatchUiState {
    data class SetPermissionBluetooth(val isGranted: Boolean) : LinkDeviceSmartWatchUiState
    data object StartListeningForWatch : LinkDeviceSmartWatchUiState
    data object StopListening : LinkDeviceSmartWatchUiState
    data class OnDeviceFound(val detectedWearable: DetectedWearable?) : LinkDeviceSmartWatchUiState
    data object OnUserApprovedConnection : LinkDeviceSmartWatchUiState
    data object DismissDialog : LinkDeviceSmartWatchUiState
    data object ValidateWearableDetector : LinkDeviceSmartWatchUiState
}