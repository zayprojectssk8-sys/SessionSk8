package com.zayprojetcs.weeksk8.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat

fun Context.getRequiredBluetoothPermissionsGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else true
}

fun Context.getRequiredSessionPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
        if (hasBodySensor(this)) {
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    } else { // API 30 a 33 (Wear OS 3.0 - 4.0 legacy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}


fun Context.getRequiredSessionPermissionsGranted(): Boolean {
    return getRequiredSessionPermissions().all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun hasBodySensor(context: Context): Boolean {
    // 1. Verificación por características del hardware del sistema
    val hasFeature =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE)

    // 2. Verificación directa mediante el SensorManager
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val hasSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null

    return hasFeature || hasSensor
}