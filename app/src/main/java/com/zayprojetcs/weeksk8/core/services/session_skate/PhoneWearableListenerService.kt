package com.zayprojetcs.weeksk8.core.services.session_skate

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class PhoneWearableListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "PhoneWearListener"
        private const val SESSION_SYNC_PATH = "/skate_session/state_sync"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == SESSION_SYNC_PATH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val isSessionStarted = dataMap.getBoolean("is_session_started")
                val isRunning = dataMap.getBoolean("is_running")

                Log.d(TAG, "Cambio detectado desde el reloj. Iniciada: $isSessionStarted | En ejecución: $isRunning")

                if (isSessionStarted && isRunning) {
                    // Arrancar o notificar al Foreground Service de la sesión en el teléfono
                    val intent = Intent(this, SkateSessionService::class.java).apply {
                        action = SkateSessionService.ACTION_START_FROM_WATCH
                    }

                    startForegroundService(intent)
                }
            }
        }
    }
}