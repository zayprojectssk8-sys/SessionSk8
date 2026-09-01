package com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state

import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable

sealed interface LinkDeviceSmartWatchUiState {
    data class SetPermissionBluetooth(val isGranted: Boolean) : LinkDeviceSmartWatchUiState
    data class SetScannerDevices(val isScanner: Boolean) : LinkDeviceSmartWatchUiState
    data object StartListeningForWatch : LinkDeviceSmartWatchUiState
    //data object StopListening : LinkDeviceSmartWatchUiState
    data class OnDeviceFound(val deviceWearable: DeviceWearable?) : LinkDeviceSmartWatchUiState
    data class OnDissociateDevice(val deviceWearable: DeviceWearable?) : LinkDeviceSmartWatchUiState
    data object OnUserApprovedConnection : LinkDeviceSmartWatchUiState
    data object DismissDialog : LinkDeviceSmartWatchUiState
    data object ValidateWearableDetector : LinkDeviceSmartWatchUiState
}