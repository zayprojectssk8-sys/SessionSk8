package com.zayprojetcs.weeksk8.core.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.zayprojetcs.weeksk8.core.room.model.AccelMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertAccelMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

class AccelSensorHelper(
    val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    // Flag que indica si el hardware existe en el dispositivo
    val isAvailable: Boolean = accelSensor != null
    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    // Acumuladores de ventana (60s)
    private var minImpactG = Float.MAX_VALUE
    private var maxImpactG = Float.MIN_VALUE
    private var minBodyAccel = Float.MAX_VALUE
    private var maxBodyAccel = Float.MIN_VALUE
    private var minVibration = Float.MAX_VALUE
    private var maxVibration = Float.MIN_VALUE

    // Estado de filtros
    private var lowPassFiltered = 9.81f
    private var samplesCount = 0

    fun startTracking(sessionId: Long, phaseId: String) {
        if (!isAvailable) return

        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        timerJob = scope.launch {
            while (isActive) {
                delay(60_000.milliseconds)
                flushMinuteData()
            }
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        timerJob?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 1. Magnitud bruta
        val rawMagnitude = sqrt(x * x + y * y + z * z)

        // 2. Filtro Paso Bajo para movimiento corporal
        lowPassFiltered += 0.1f * (rawMagnitude - lowPassFiltered)

        // 3. Componente de alta frecuencia (Vibración)
        val highFreqVibration = abs(rawMagnitude - lowPassFiltered)

        synchronized(this) {
            samplesCount++
            minImpactG = minOf(minImpactG, rawMagnitude)
            maxImpactG = maxOf(maxImpactG, rawMagnitude)
            minBodyAccel = minOf(minBodyAccel, lowPassFiltered)
            maxBodyAccel = maxOf(maxBodyAccel, lowPassFiltered)
            minVibration = minOf(minVibration, highFreqVibration)
            maxVibration = maxOf(maxVibration, highFreqVibration)
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (samplesCount == 0) return

            val entity = AccelMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                minImpactG = if (minImpactG == Float.MAX_VALUE) 0f else minImpactG,
                maxImpactG = if (maxImpactG == Float.MIN_VALUE) 0f else maxImpactG,
                minBodyAccel = if (minBodyAccel == Float.MAX_VALUE) 0f else minBodyAccel,
                maxBodyAccel = if (maxBodyAccel == Float.MIN_VALUE) 0f else maxBodyAccel,
                minVibration = if (minVibration == Float.MAX_VALUE) 0f else minVibration,
                maxVibration = if (maxVibration == Float.MIN_VALUE) 0f else maxVibration
            )

            scope.launch { context.repoRoomInsertAccelMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minImpactG = Float.MAX_VALUE
        maxImpactG = Float.MIN_VALUE
        minBodyAccel = Float.MAX_VALUE
        maxBodyAccel = Float.MIN_VALUE
        minVibration = Float.MAX_VALUE
        maxVibration = Float.MIN_VALUE
        samplesCount = 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}