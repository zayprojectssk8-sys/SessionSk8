package com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.model

import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable

sealed interface DetectedWearableState {
    data object Idle : DetectedWearableState
    data class DeviceDetected(val device: DeviceWearable) : DetectedWearableState
    data class DeviceConnected(val device: DeviceWearable) : DetectedWearableState
    data class AwaitingWatch(val device: DeviceWearable, val message: String) :
        DetectedWearableState

    data object ScanningDevices : DetectedWearableState
    data object EmptyDevices : DetectedWearableState
    data class Success(val device: DeviceWearable) : DetectedWearableState
    data class Disconnected(val reason: String) : DetectedWearableState
}


data class LinkDeviceSmartWatchUiStateModel(
    val wearables: List<DeviceWearable> = emptyList(),
    val startScannerDevices: Boolean = false,
    val isPermissionBluetoothGranted: Boolean = false,
    val currentDevice: DeviceWearable? = null,
    val connectDevice: DeviceWearable? = null,
    val detectedWearableState: DetectedWearableState = DetectedWearableState.Idle
)