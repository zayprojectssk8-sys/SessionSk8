package com.zayprojetcs.weeksk8.services

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager
import com.zayprojetcs.weeksk8.utils.WearableDetector
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_CHECK_CONNECTION_STATUS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppMessageNodeListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val wearableDetector by lazy { WearableDetector(this) }
    val dataStoreAppManager by lazy { DataStoreAppManager(this) }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.wtf(javaClass.simpleName, "CONECTEEED WATCH onMessageReceived: $messageEvent")

        super.onMessageReceived(messageEvent)
        when (messageEvent.path) {
            MESSAGE_PATH_WEAR_TO_PHONE_CHECK_CONNECTION_STATUS -> loadStatusConnectionWearOs(
                messageEvent.sourceNodeId
            )

            "/trigger_action" -> {
                // Hacer algo en la app móvil
            }
        }
    }

    private fun loadStatusConnectionWearOs(sourceNodeId: String) {
        if (ActivityCompat.checkSelfPermission(
                application, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            serviceScope.launch {
                val device = wearableDetector.getBondedWearables()
                    .find { it.nodeId == sourceNodeId }

                device?.let {
                    dataStoreAppManager.saveDeviceConnectBluetooth(it)
                }
            }
        }
    }
}
