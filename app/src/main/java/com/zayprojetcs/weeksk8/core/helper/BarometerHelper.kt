package com.zayprojetcs.weeksk8.core.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.zayprojetcs.weeksk8.core.room.model.BaroMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertBaroMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class BarometerHelper(
    val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val baroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    // Flag que indica si el hardware existe en el dispositivo
    val isAvailable: Boolean = baroSensor != null

    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var minPressure = Float.MAX_VALUE
    private var maxPressure = Float.MIN_VALUE
    private var minAltitude = Float.MAX_VALUE
    private var maxAltitude = Float.MIN_VALUE
    private var samplesCount = 0

    fun startTracking(sessionId: Long, phaseId: String) {
        if (!isAvailable) return

        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        baroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
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
        val pressureHpa = event.values[0]
        val altitudeMeters =
            SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureHpa)

        synchronized(this) {
            samplesCount++
            minPressure = minOf(minPressure, pressureHpa)
            maxPressure = maxOf(maxPressure, pressureHpa)
            minAltitude = minOf(minAltitude, altitudeMeters)
            maxAltitude = maxOf(maxAltitude, altitudeMeters)
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (samplesCount == 0) return

            val entity = BaroMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                minPressureHpa = if (minPressure == Float.MAX_VALUE) 0f else minPressure,
                maxPressureHpa = if (maxPressure == Float.MIN_VALUE) 0f else maxPressure,
                minRelativeAltitudeM = if (minAltitude == Float.MAX_VALUE) 0f else minAltitude,
                maxRelativeAltitudeM = if (maxAltitude == Float.MIN_VALUE) 0f else maxAltitude
            )

            scope.launch { context.repoRoomInsertBaroMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minPressure = Float.MAX_VALUE
        maxPressure = Float.MIN_VALUE
        minAltitude = Float.MAX_VALUE
        maxAltitude = Float.MIN_VALUE
        samplesCount = 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}