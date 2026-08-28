package com.zayprojetcs.weeksk8.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.SOURCE_NODE_ID
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Serializable
data class DetectedWearable(
    val name: String,
    val brand: WearableBrand,
    val isWearOs: Boolean,
    val nodeId: String? = null,
    val isAppInstalled: Boolean = false
)

enum class WearableBrand {
    SAMSUNG, HUAWEI, GARMIN, XIAOMI, GOOGLE, UNKNOWN
}

class WearableDetector(private val context: Context) {

    private val nodeClient = Wearable.getNodeClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val remoteActivityHelper = RemoteActivityHelper(context)

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun getBondedWearables(): List<DetectedWearable> {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: return emptyList()

        // 1. Obtener nodos Wear OS activos y los que ya tienen la app instalada
        val wearOsNodes = try {
            nodeClient.connectedNodes.await()
        } catch (_: Exception) {
            emptyList()
        }

        val nodesWithApp = try {
            capabilityClient.getCapability(CAPABILITY_CLIENT_NAME, CapabilityClient.FILTER_REACHABLE)
                .await().nodes.map { it.id }.toSet()
        } catch (_: Exception) {
            emptySet()
        }

        val detectedList = mutableListOf<DetectedWearable>()

        // 2. Evaluar cada dispositivo Bluetooth emparejado
        adapter.bondedDevices?.forEach { device ->
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

            val brand = when {
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

            // Solo agregamos si encaja en alguna categoría relevante
            if (brand != WearableBrand.UNKNOWN || isWearOs) {
                detectedList.add(
                    DetectedWearable(
                        name = name,
                        brand = brand,
                        isWearOs = isWearOs,
                        nodeId = if (isWearOs) matchingNode.id else device.address,
                        isAppInstalled = isInstalled
                    )
                )
            }
        }

        return detectedList
    }

    // 3. Método para activar la Play Store directamente en la pantalla del reloj
    fun promptInstallOnWear(nodeId: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=${context.packageName}".toUri()
            setPackage("com.android.vending")
            putExtra(SOURCE_NODE_ID, nodeId)
        }
        remoteActivityHelper.startRemoteActivity(intent, nodeId)
    }

    suspend fun automaticOpenWearApp(nodeId: String) {
        context.sendConnectionPhoneToWatch(nodeId)
    }

    fun promptInstallOnWearTest(nodeId: String) {
        // CAMBIO TEMPORAL SOLO PARA PRUEBAS:
        val testPackageName = "com.strava" // O "com.spotify.music"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=$testPackageName".toUri()
            setPackage("com.android.vending")
        }
        remoteActivityHelper.startRemoteActivity(intent, nodeId)
    }
}