package com.zayprojetcs.weeksk8.services

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        when (messageEvent.path) {
            "/start_timer" -> {
                val countdown = String(messageEvent.data)
                // Ejecuta la lógica en el celular (ej. Iniciar cuenta regresiva o cámara)
            }
            "/trigger_action" -> {
                // Hacer algo en la app móvil
            }
        }
    }
}