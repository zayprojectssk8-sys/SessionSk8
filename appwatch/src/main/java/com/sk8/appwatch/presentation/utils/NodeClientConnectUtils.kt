package com.sk8.appwatch.presentation.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun Context.sendConnectionHandshakeToPhone() {
    try {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)

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

suspend fun Context.sendCommandToPhone(path: String, message: String) {
    val nodeClient = Wearable.getNodeClient(this)
    val messageClient = Wearable.getMessageClient(this)

    withContext(Dispatchers.IO) {
        try {
            // Tasks.await() bloquea el hilo IO en lugar de ser una suspend function nativa
            val nodes = Tasks.await(nodeClient.connectedNodes)

            for (node in nodes) {
                Tasks.await(messageClient.sendMessage(node.id, path, message.toByteArray()))
            }
        } catch (e: Exception) {
            Log.wtf("javaClass.simpleName", " onMessageReceived APP ${e.localizedMessage} ")
            e.printStackTrace()
        }
    }
}