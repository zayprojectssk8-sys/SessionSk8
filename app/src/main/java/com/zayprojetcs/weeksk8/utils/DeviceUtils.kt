package com.zayprojetcs.weeksk8.utils

import android.bluetooth.BluetoothManager
import android.content.Context

fun Context.isBluetoothOff(): Boolean {
    val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter

    // Retorna true si el dispositivo no tiene Bluetooth o si está apagado
    return bluetoothAdapter == null || !bluetoothAdapter.isEnabled
}