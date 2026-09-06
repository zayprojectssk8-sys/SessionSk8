package com.zayprojetcs.weeksk8.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.wearable.Node
import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.core.helper.model.WearableBrand

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothAdapter.getBondedDevicesAdapter(
    wearOsNodes: List<Node>,
    nodesWithApp: Set<String>
): MutableList<DeviceWearable> {
    val detectedList = mutableListOf<DeviceWearable>()

    // 2. Evaluar cada dispositivo Bluetooth emparejado
    bondedDevices?.forEach { device ->
        val name = device.name ?: ""

        // Verifica si este dispositivo Bluetooth coincide con un nodo activo de Wear OS
        val matchingNode = wearOsNodes.find { node ->
            node.displayName.equals(name, ignoreCase = true) || name.contains(
                node.displayName,
                ignoreCase = true
            )
        }

        val isWearOs = matchingNode != null
        val isInstalled = matchingNode?.let { nodesWithApp.contains(it.id) } ?: false

        val brand = getTypeBrandWearOs(device.name)

        // Solo agregamos si encaja en alguna categoría relevante
        if (brand != WearableBrand.UNKNOWN || isWearOs) {
            detectedList.add(
                DeviceWearable(
                    name = name,
                    brand = brand,
                    isWearOs = isWearOs,
                    nodeId = if (isWearOs) matchingNode.id else device.address,
                    isAppInstalled = isInstalled
                )
            )
        }

    }
    Log.wtf(
        javaClass.simpleName,
        " getBondedWearables getBondedDevicesAdapter detectedList $detectedList"
    )

    return detectedList
}

fun getTypeBrandWearOs(name: String): WearableBrand {
    return when {
        name.contains("Galaxy Watch", ignoreCase = true) || name.contains(
            "Samsung",
            ignoreCase = true
        ) -> WearableBrand.SAMSUNG

        name.contains("Pixel Watch", ignoreCase = true) || name.contains(
            "Google",
            ignoreCase = true
        ) -> WearableBrand.GOOGLE

        name.contains("HUAWEI", ignoreCase = true) || name.contains(
            "Band",
            ignoreCase = true
        ) -> WearableBrand.HUAWEI

        name.contains("Garmin", ignoreCase = true) -> WearableBrand.GARMIN
        name.contains("Mi Smart Band", ignoreCase = true) || name.contains(
            "Xiaomi",
            ignoreCase = true
        ) -> WearableBrand.XIAOMI

        else -> WearableBrand.UNKNOWN
    }
}