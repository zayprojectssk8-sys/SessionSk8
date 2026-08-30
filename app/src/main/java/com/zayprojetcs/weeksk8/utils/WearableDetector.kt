package com.zayprojetcs.weeksk8.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST
import com.zaysk8.core.utils.SOURCE_NODE_ID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class DetectedWearable(
    val name: String,
    val brand: WearableBrand,
    val isWearOs: Boolean,
    val nodeId: String? = null,
    val isAppInstalled: Boolean = false
) {
    fun getTypeWearable(): String = if (isWearOs) "Wear OS" else "Smartband / BLE"
}

enum class WearableBrand(val nameBrand: String) {
    SAMSUNG(nameBrand = "Samsung"),
    HUAWEI(nameBrand = "Huawei"),
    GARMIN(nameBrand = "Garmin"),
    XIAOMI(nameBrand = "Xiaomi"),
    GOOGLE(nameBrand = "Google"),
    UNKNOWN(nameBrand = "Unknown")
}

class WearableDetector(private val context: Context) {

    private val nodeClient = Wearable.getNodeClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val remoteActivityHelper = RemoteActivityHelper(context)
    private val messageClient = Wearable.getMessageClient(context)

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
            capabilityClient.getCapability(
                CAPABILITY_CLIENT_NAME,
                CapabilityClient.FILTER_REACHABLE
            )
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

    /**
     * Envía una solicitud de desvinculación al reloj y espera su confirmación.
     *
     * @param nodeId ID del reloj a desvincular.
     * @param timeoutMs Tiempo máximo de espera en milisegundos (por defecto 5s).
     * @return true si el reloj aceptó y confirmó la desvinculación; false si falló o expiro el tiempo.
     */
    suspend fun unpairWearable(nodeId: String, timeoutMs: Long = 5000L): Boolean {
        val confirmationDeferred = CompletableDeferred<Boolean>()
        Log.wtf(
                "javaClass.simpleName",
                "OnDissociateDevice: unpairWearable ${nodeId}"
            )

        // 1. Crear listener temporal para escuchar la respuesta del reloj
        val responseListener = MessageClient.OnMessageReceivedListener { messageEvent ->
            if (messageEvent.path == MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED && messageEvent.sourceNodeId == nodeId) {
                confirmationDeferred.complete(true)
            }
        }

        return try {
            // 2. Registrar listener temporal
            messageClient.addListener(responseListener)

            // 3. Enviar mensaje de desvinculación al reloj
            messageClient.sendMessage(nodeId, MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST, byteArrayOf()).await()

            // 4. Esperar a que el reloj confirme antes del timeout
            val isSuccess = withTimeoutOrNull(timeoutMs.milliseconds) {
                confirmationDeferred.await()
            } ?: false

            isSuccess
        } catch (_: Exception) {
            false
        } finally {
            // 5. Limpiar listener para evitar fugas de memoria
            messageClient.removeListener(responseListener)
        }
    }
}