package com.zayprojetcs.weeksk8.core.helper.model

import kotlinx.serialization.Serializable

enum class WearableBrand(val nameBrand: String) {
    SAMSUNG(nameBrand = "Samsung"),
    HUAWEI(nameBrand = "Huawei"),
    GARMIN(nameBrand = "Garmin"),
    XIAOMI(nameBrand = "Xiaomi"),
    GOOGLE(nameBrand = "Google"),
    UNKNOWN(nameBrand = "Unknown")
}

@Serializable
data class DeviceWearable(
    val name: String,
    val brand: WearableBrand,
    val isWearOs: Boolean,
    val nodeId: String? = null,
    val isAppInstalled: Boolean = false
) {
    fun getTypeWearable(): String = if (isWearOs) "Wear OS" else "Smartband / BLE"
}