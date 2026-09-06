package com.zayprojetcs.weeksk8.core.helper.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.zayprojetcs.weeksk8.core.helper.AccelSensorHelper
import com.zayprojetcs.weeksk8.core.helper.ActivityRecognitionHelper
import com.zayprojetcs.weeksk8.core.helper.BarometerHelper
import com.zayprojetcs.weeksk8.core.helper.GpsSensorHelper
import com.zayprojetcs.weeksk8.core.helper.GyroSensorHelper
import com.zayprojetcs.weeksk8.core.helper.MagnetometerHelper
import com.zayprojetcs.weeksk8.core.helper.StepDetectorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class SensorSessionManager(
    val context: Context
) {
    // Scope ligado al ciclo de vida de la sesión activa
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Instancias de los 7 Helpers
    private val accelHelper = AccelSensorHelper(context, managerScope)
    private val activityHelper = ActivityRecognitionHelper(context, managerScope)
    private val baroHelper = BarometerHelper(context, managerScope)
    private val gpsHelper = GpsSensorHelper(context, managerScope)
    private val gyroHelper = GyroSensorHelper(context, managerScope)
    private val magHelper = MagnetometerHelper(context, managerScope)
    private val stepHelper = StepDetectorHelper(context, managerScope)

    private var currentSessionId: Long? = null
    private var currentPhaseId: String? = null
    private var isTracking = false

    /**
     * Inicia el rastreo para una sesión y fase específicas (ej. WARMUP, SKATE).
     */
    fun startPhase(sessionId: Long, phaseId: String) {
        if (isTracking) {
            stopAll()
        }

        currentSessionId = sessionId
        currentPhaseId = phaseId
        isTracking = true

        // Enciende la recolección en los 7 helpers
        accelHelper.startTracking(sessionId, phaseId)

        if (gyroHelper.isAvailable) gyroHelper.startTracking(sessionId, phaseId)
        if (baroHelper.isAvailable) baroHelper.startTracking(sessionId, phaseId)
        if (magHelper.isAvailable) magHelper.startTracking(sessionId, phaseId)
        if (stepHelper.isAvailable && hasActivityPermission()) stepHelper.startTracking(sessionId, phaseId)

        if (hasLocationPermission()) gpsHelper.startTracking(sessionId, phaseId)
        if (hasActivityPermission()) activityHelper.startTracking(sessionId, phaseId)
    }

    /**
     * Cambia suavemente de fase (ejemplo: de "WARMUP" a "SKATE") dentro de la misma sesión.
     */
    fun switchPhase(newPhaseId: String) {
        val sessionId = currentSessionId ?: return
        startPhase(sessionId, newPhaseId)
    }

    /**
     * Detiene la captura en todos los sensores.
     */
    fun stopAll() {
        if (!isTracking) return

        accelHelper.stopTracking()
        activityHelper.stopTracking()
        baroHelper.stopTracking()
        gpsHelper.stopTracking()
        gyroHelper.stopTracking()
        magHelper.stopTracking()
        stepHelper.stopTracking()

        isTracking = false
        currentPhaseId = null
    }

    /**
     * Libera los recursos del scope (invocado al destruir el Servicio).
     */
    fun destroy() {
        stopAll()
        managerScope.cancel()
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun hasActivityPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // En Android 9 (API 28) e inferiores, no requería permiso en tiempo de ejecución
            true
        }
    }
}