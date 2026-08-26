package com.sk8.appwatch.presentation.core.wear_helper

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearMessageSender(context: Context) {

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    companion object {
        const val IMPACT_ALERT_PATH = "/weeksk8/impact_alert"
    }

    suspend fun sendImpactAlertToPhone(gForce: Float) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            val payload = "IMPACT_DETECTED:%.2fG".format(gForce).toByteArray()

            for (node in nodes) {
                messageClient.sendMessage(node.id, IMPACT_ALERT_PATH, payload).await()
                Log.d("WearMessageSender", "Alerta enviada a: ${node.displayName}")
            }
        } catch (e: Exception) {
            Log.e("WearMessageSender", "Error al enviar impacto: ${e.message}")
        }
    }
}