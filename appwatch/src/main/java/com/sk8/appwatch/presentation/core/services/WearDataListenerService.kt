package com.sk8.appwatch.presentation.core.services

import android.content.Intent
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.sk8.appwatch.presentation.MainActivity

class WearDataListenerService : WearableListenerService() {

    companion object {
        private const val SESSION_SYNC_PATH = "/skate_session/state_sync"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == SESSION_SYNC_PATH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                // Leemos las claves reales que guardas en tu SessionSyncManager
                val isSessionStarted = dataMap.getBoolean("is_session_started", false)
                val isRunning = dataMap.getBoolean("is_running", false)


                // Si la sesión fue iniciada en el teléfono, abre la app del reloj
                if (isSessionStarted && isRunning) {
                    val uiIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(uiIntent)
                }
            }
        }
    }
}