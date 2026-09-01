package com.zayprojetcs.weeksk8.core.helper

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class BluetoothReceiverHelper(val context: Context) {

    fun observeBluetoothState(): Flow<Boolean> = callbackFlow {
        val bluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                    when (state) {
                        BluetoothAdapter.STATE_ON -> trySend(true)
                        BluetoothAdapter.STATE_OFF -> trySend(false)
                    }
                }
            }
        }

        // 1. Registrar el receiver al iniciar la recolección del Flow
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)

        // Emitir estado inicial del Bluetooth
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        trySend(bluetoothAdapter?.isEnabled == true)

        // 2. Se ejecuta automáticamente para desregistrar cuando el Flow se cancela o destruye
        awaitClose {
            try {
                context.unregisterReceiver(bluetoothStateReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver no estaba registrado
            }
        }
    }
}