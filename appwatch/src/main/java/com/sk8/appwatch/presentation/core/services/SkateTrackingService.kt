package com.sk8.appwatch.presentation.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sk8.appwatch.R
import com.sk8.appwatch.presentation.core.helper.NodeClientWatchHelper
import com.sk8.appwatch.presentation.core.sensors.ImpactDetectorSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SkateTrackingService : LifecycleService() {

    private lateinit var impactDetectorSensor: ImpactDetectorSensor
    private lateinit var nodeClientWatchHelper: NodeClientWatchHelper
    private var fallCount = 0
    private val _fallCountFlow = MutableStateFlow(0)
    val fallCountFlow: StateFlow<Int> = _fallCountFlow.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        nodeClientWatchHelper = NodeClientWatchHelper(this)

        impactDetectorSensor = ImpactDetectorSensor(this) { gForce ->
            // 1. Incrementar el contador local de caídas
            fallCount++
            _fallCountFlow.value = fallCount

            // 2. Notificar al smartphone vía Bluetooth
            lifecycleScope.launch(Dispatchers.IO) {
                nodeClientWatchHelper.sendImpactAlertToPhone(gForce)
            }
        }
    }
    fun getFinalFallCount(): Int = fallCount

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SkateTrackingService = this@SkateTrackingService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForegroundServiceWithNotification()
        impactDetectorSensor.startListening()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        impactDetectorSensor.stopListening()
    }

    private fun startForegroundServiceWithNotification() {
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WeekSk8")
            .setContentText("Registrando sesión de skate...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este icono o usa android.R.drawable.ic_media_play
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()

        // Android 14+ (API 34) requiere especificar el tipo de servicio en ejecución
        var foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            foregroundServiceType =
                foregroundServiceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        }
        startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Seguimiento de Sesión Skate",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificación activa mientras se rastrea la sesión de skate"
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "skate_tracking_channel"
        private const val NOTIFICATION_ID = 1001
    }
}