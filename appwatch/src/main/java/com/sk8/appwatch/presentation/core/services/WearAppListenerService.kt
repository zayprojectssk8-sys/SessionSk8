package com.sk8.appwatch.presentation.core.services

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sk8.appwatch.presentation.MainActivity
import com.zaysk8.core.utils.MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP
import com.zaysk8.core.utils.SOURCE_NODE_ID

class WearAppListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP) {

            // Crear el Intent para lanzar la Activity principal del reloj
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(SOURCE_NODE_ID, messageEvent.sourceNodeId)
            }
            startActivity(intent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}