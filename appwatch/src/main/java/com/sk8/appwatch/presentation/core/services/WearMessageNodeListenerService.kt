package com.sk8.appwatch.presentation.core.services

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sk8.appwatch.presentation.MainActivity
import com.sk8.appwatch.presentation.core.data_store.DataStoreWatchManager
import com.sk8.appwatch.presentation.core.helper.NodeClientWatchHelper
import com.zaysk8.core.utils.MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST
import com.zaysk8.core.utils.SOURCE_NODE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearMessageNodeListenerService : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val dataStoreWatchManager by lazy { DataStoreWatchManager(application) }
    private val nodeClientWatchHelper by lazy { NodeClientWatchHelper(application) }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.wtf(javaClass.simpleName, "CONECTEEED WATCH onMessageReceived: $messageEvent")

        when (messageEvent.path) {
            MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP -> openAppWearOs(messageEvent)
            MESSAGE_PATH_WEAR_TO_PHONE_UNPAIR_REQUEST -> requestUnpair(messageEvent)
            else -> super.onMessageReceived(messageEvent)
        }

    }

    private fun openAppWearOs(messageEvent: MessageEvent) {
        // Crear el Intent para lanzar la Activity principal del reloj
        val intent = Intent(this, MainActivity::class.java).apply {
            // SINGLE_TOP es la clave para que 'onNewIntent' se dispare si ya está abierta
            // CLEAR_TOP asegura que volvamos a la raíz si había otras pantallas abiertas encima
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(SOURCE_NODE_ID, messageEvent.sourceNodeId)
        }
        startActivity(intent)
    }

    private fun requestUnpair(messageEvent: MessageEvent) {
        val phoneNodeId = messageEvent.sourceNodeId
         Log.wtf(
                "javaClass.simpleName",
                "OnDissociateDevice: requestUnpair ${messageEvent}"
            )
        // 1. Limpiar datos locales de sesión / preferencias en el reloj
        serviceScope.launch {
            dataStoreWatchManager.clearDeviceConnectNodeId()

            // 2. Responder al celular que la desvinculación fue aceptada y procesada
            nodeClientWatchHelper.requestUnpairPhone(phoneNodeId)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}