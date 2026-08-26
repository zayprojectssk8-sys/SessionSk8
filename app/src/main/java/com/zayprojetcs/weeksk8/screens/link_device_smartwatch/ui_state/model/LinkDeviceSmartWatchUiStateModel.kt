package com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.model

import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.DetectedWearableState
import com.zayprojetcs.weeksk8.utils.DetectedWearable

data class LinkDeviceSmartWatchUiStateModel(
    val wearables: List<DetectedWearable> = emptyList(),
    val isPermissionBluetoothGranted: Boolean = false,
    val currentDevice: DetectedWearable? = null,
    val detectedWearableState: DetectedWearableState = DetectedWearableState.Idle
)