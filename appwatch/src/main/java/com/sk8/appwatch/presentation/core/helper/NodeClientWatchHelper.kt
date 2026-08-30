package com.sk8.appwatch.presentation.core.helper

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_CHECK_CONNECTION_STATUS
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_IMPACT_ALERT
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED
import kotlinx.coroutines.tasks.await

class NodeClientWatchHelper(context: Context) {

    private val nodeClient = Wearable.getNodeClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    private val capabilityClient = Wearable.getCapabilityClient(context)

    suspend fun verifyInstallAppInPhone(nodeId: String): Boolean {
        return try {
            val nodes = capabilityClient
                .getCapability(
                    CAPABILITY_CLIENT_NAME,
                    CapabilityClient.FILTER_REACHABLE
                )
                .await()
                .nodes

            Log.wtf("javaClass.simpleName", " WearAppNavigation nodes: $nodes")
            nodes.any { it.id == nodeId }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error al verificar capacidades de Wear OS", e)
            false
        }
    }

    /**
     * Envía un mensaje de conexión al celular en el emparejamiento.
     */
    suspend fun sendConnectionHandshakeToPhone() {
        try {
            val nodes = nodeClient.connectedNodes.await()

            if (nodes.isEmpty()) return

            nodes.forEach { node ->
                messageClient.sendMessage(
                    node.id,
                    MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED,
                    "ACK".toByteArray()
                ).await()
            }
        } catch (e: Exception) {
            Log.e("WearHandshake", "Error enviando handshake al celular", e)
        }
    }

    // Responder al celular que la desvinculación fue aceptada y procesada
    suspend fun requestUnpairPhone(nodeId: String) {
        try {
            messageClient.sendMessage(
                nodeId,
                MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_CONFIRMED,
                byteArrayOf()
            )
        } catch (e: Exception) {
            Log.e("WearMessageSender", "Error al enviar impacto: ${e.message}")
        }
    }

    suspend fun sendImpactAlertToPhone(gForce: Float) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            val payload = "IMPACT_DETECTED:%.2fG".format(gForce).toByteArray()

            for (node in nodes) {
                messageClient.sendMessage(node.id, MESSAGE_PATH_WEAR_TO_PHONE_IMPACT_ALERT, payload)
                    .await()
                Log.d("WearMessageSender", "Alerta enviada a: ${node.displayName}")
            }
        } catch (e: Exception) {
            Log.e("WearMessageSender", "Error al enviar impacto: ${e.message}")
        }
    }

    fun verifyConnectionWithPhone(nodeId: String) {

        // Preguntar al celular si la conexión/datos siguen válidos
        messageClient.sendMessage(
            nodeId,
            MESSAGE_PATH_WEAR_TO_PHONE_CHECK_CONNECTION_STATUS,
            byteArrayOf()
        ).addOnFailureListener {

        }
    }
}