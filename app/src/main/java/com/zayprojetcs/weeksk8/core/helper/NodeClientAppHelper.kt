package com.zayprojetcs.weeksk8.core.helper

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.utils.getBondedDevicesAdapter
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST
import com.zaysk8.core.utils.SOURCE_NODE_ID
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class NodeClientAppHelper(private val context: Context) {

    private val nodeClient = Wearable.getNodeClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val remoteActivityHelper = RemoteActivityHelper(context)
    private val messageClient = Wearable.getMessageClient(context)


    // Flujo para escuchar mensajes entrantes
    fun observeMessages(): Flow<MessageEvent> = callbackFlow {
        val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
            trySend(messageEvent)
        }

        // Registrar listener de mensajes
        messageClient.addListener(listener)

        // Se ejecuta automáticamente cuando el ViewModel (o Flow) se destruye/cancela
        awaitClose {
            messageClient.removeListener(listener)
        }
    }

    // Flujo para observar cambios en los nodos conectados
    fun observeCapabilityChanges(): Flow<CapabilityInfo> = callbackFlow {
        val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            trySend(capabilityInfo)
        }

        // Registrar listener de capacidad
        capabilityClient.addListener(listener, CAPABILITY_CLIENT_NAME)

        awaitClose {
            capabilityClient.removeListener(listener)
        }
    }

    suspend fun getBondedWearables(): List<DeviceWearable> {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: return emptyList()

        Log.wtf(javaClass.simpleName, " getBondedWearables adapter")

        // 1. Obtener nodos Wear OS activos y los que ya tienen la app instalada
        val wearOsNodes = try {
            nodeClient.connectedNodes.await()
        } catch (_: Exception) {
            emptyList()
        }

        Log.wtf(javaClass.simpleName, " getBondedWearables wearOsNodes $wearOsNodes")


        val nodesWithApp = try {
            capabilityClient.getCapability(
                CAPABILITY_CLIENT_NAME,
                CapabilityClient.FILTER_REACHABLE
            )
                .await().nodes.map { it.id }.toSet()
        } catch (_: Exception) {
            emptySet()
        }

        Log.wtf(javaClass.simpleName, " getBondedWearables nodesWithApp $nodesWithApp")

        var detectedList = mutableListOf<DeviceWearable>()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            detectedList = adapter.getBondedDevicesAdapter(wearOsNodes, nodesWithApp)
        }


        Log.wtf(javaClass.simpleName, " getBondedWearables detectedList $detectedList")
        return detectedList
    }


    // 3. Méttodo para activar la Play Store directamente en la pantalla del reloj
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
        Log.wtf(javaClass.simpleName, "CONECTEEED: unpairWearable $nodeId")

        return  try {
            withTimeoutOrNull(timeoutMs.milliseconds) {
                coroutineScope {
                    Log.wtf(javaClass.simpleName, "CONECTEEED withTimeoutOrNull  nodeId: $nodeId")

                    // 1. Iniciar la suscripción a observeMessages() ANTES de enviar la petición
                    //    para evitar condiciones de carrera (race conditions) si el reloj responde rápido.
                    val confirmationDeferred = async {
                        observeMessages().first { event ->
                            event.path == MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED &&
                                    event.sourceNodeId == nodeId
                        }
                    }


                    // 2. Enviar el mensaje de desvinculación al reloj
                    messageClient.sendMessage(
                        nodeId,
                        MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST,
                        byteArrayOf()
                    ).await()

                    Log.wtf(javaClass.simpleName, "CONECTEEED sendMessage  nodeId: $nodeId")


                    // 3. Esperar la respuesta capturada por el flujo
                    confirmationDeferred.await()
                    Log.wtf(
                        javaClass.simpleName,
                        "CONECTEEED sendMessage  confirmationDeferred: $confirmationDeferred"
                    )
                    true
                }
            } ?: false
        } catch (e: Exception) {
            Log.wtf(javaClass.simpleName, "CONECTEEED  Exception: $e")

            Log.wtf(javaClass.simpleName, "Error en unpairWearable: ${e.message}")
            false
        }
    }

    suspend fun Context.sendConnectionPhoneToWatch(nodeId: String) {
        try {
            Log.wtf(
                javaClass.simpleName,
                "CONECTEEED APP sendConnectionPhoneToWatch: $MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP"
            )
            messageClient.sendMessage(nodeId, MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP, byteArrayOf())
                .await()
        } catch (e: Exception) {
            Log.wtf(javaClass.simpleName, "CONECTEEED APP sendConnectionPhoneToWatch: $e")

            e.printStackTrace()
        }
    }

     suspend fun sendConnectionUnpair(nodeId: String) {
        try {
            Log.wtf(
                javaClass.simpleName,
                "CONECTEEED APP sendConnectionPhoneToWatch: $MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST"
            )
            messageClient.sendMessage(
                nodeId,
                MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST,
                byteArrayOf()
            )
                .await()
        } catch (e: Exception) {
            Log.wtf(javaClass.simpleName, "CONECTEEED APP sendConnectionPhoneToWatch: $e")

            e.printStackTrace()
        }
    }
}