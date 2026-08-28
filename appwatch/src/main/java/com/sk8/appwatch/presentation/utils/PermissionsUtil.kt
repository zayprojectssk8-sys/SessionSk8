package com.sk8.appwatch.presentation.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun getRequiredWearOsPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
        arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            "android.permission.health.READ_HEART_RATE",
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else { // API 30 a 33 (Wear OS 3.0 - 4.0 legacy)
        arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

fun Context.getRequiredWearOsPermissionsGranted(): Boolean {
    return getRequiredWearOsPermissions().all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
