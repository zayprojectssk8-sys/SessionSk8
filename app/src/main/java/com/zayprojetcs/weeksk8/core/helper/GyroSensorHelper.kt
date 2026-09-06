package com.zayprojetcs.weeksk8.core.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.zayprojetcs.weeksk8.core.room.model.GyroMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertGyroMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

class GyroSensorHelper(
    val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    val isAvailable: Boolean = gyroSensor != null

    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var minRotationDps = Float.MAX_VALUE
    private var maxRotationDps = Float.MIN_VALUE
    private var sumRotationDps = 0f
    private var samplesCount = 0

    fun startTracking(sessionId: Long, phaseId: String) {
        if (!isAvailable) return

        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        gyroSensor?.let {
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
        val radX = event.values[0]
        val radY = event.values[1]
        val radZ = event.values[2]

        // Convertir rad/s a grados/s (DPS)
        val dps = sqrt(radX * radX + radY * radY + radZ * radZ) * (180f / Math.PI.toFloat())

        synchronized(this) {
            samplesCount++
            sumRotationDps += dps
            minRotationDps = minOf(minRotationDps, dps)
            maxRotationDps = maxOf(maxRotationDps, dps)
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (samplesCount == 0) return

            val avgDps = sumRotationDps / samplesCount

            val entity = GyroMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                minRotationDps = if (minRotationDps == Float.MAX_VALUE) 0f else minRotationDps,
                maxRotationDps = if (maxRotationDps == Float.MIN_VALUE) 0f else maxRotationDps,
                avgRotationDps = avgDps
            )

            scope.launch { context.repoRoomInsertGyroMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minRotationDps = Float.MAX_VALUE
        maxRotationDps = Float.MIN_VALUE
        sumRotationDps = 0f
        samplesCount = 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}