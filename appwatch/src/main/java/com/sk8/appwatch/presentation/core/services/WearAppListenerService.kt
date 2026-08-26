package com.sk8.appwatch.presentation.core.services

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sk8.appwatch.presentation.MainActivity

class WearAppListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/open_app") {
            // Crear el Intent para lanzar la Activity principal del reloj
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}