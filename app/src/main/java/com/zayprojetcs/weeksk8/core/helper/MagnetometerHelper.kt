package com.zayprojetcs.weeksk8.core.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.zayprojetcs.weeksk8.core.room.model.MagMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertMagMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

class MagnetometerHelper(
    val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val isAvailable: Boolean = magSensor != null

    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var minFieldMicroTesla = Float.MAX_VALUE
    private var maxFieldMicroTesla = Float.MIN_VALUE
    private var minHeadingDegrees = Float.MAX_VALUE
    private var maxHeadingDegrees = Float.MIN_VALUE
    private var samplesCount = 0

    fun startTracking(sessionId: Long, phaseId: String) {
        if (!isAvailable) return

        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        magSensor?.let {
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
        val mx = event.values[0]
        val my = event.values[1]
        val mz = event.values[2]

        val fieldStrength = sqrt(mx * mx + my * my + mz * mz)
        var heading = Math.toDegrees(atan2(my.toDouble(), mx.toDouble())).toFloat()
        if (heading < 0) heading += 360f

        synchronized(this) {
            samplesCount++
            minFieldMicroTesla = minOf(minFieldMicroTesla, fieldStrength)
            maxFieldMicroTesla = maxOf(maxFieldMicroTesla, fieldStrength)
            minHeadingDegrees = minOf(minHeadingDegrees, heading)
            maxHeadingDegrees = maxOf(maxHeadingDegrees, heading)
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (samplesCount == 0) return

            val entity = MagMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                minFieldMicroTesla = if (minFieldMicroTesla == Float.MAX_VALUE) 0f else minFieldMicroTesla,
                maxFieldMicroTesla = if (maxFieldMicroTesla == Float.MIN_VALUE) 0f else maxFieldMicroTesla,
                minHeadingDegrees = if (minHeadingDegrees == Float.MAX_VALUE) 0f else minHeadingDegrees,
                maxHeadingDegrees = if (maxHeadingDegrees == Float.MIN_VALUE) 0f else maxHeadingDegrees
            )

            scope.launch { context.repoRoomInsertMagMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minFieldMicroTesla = Float.MAX_VALUE
        maxFieldMicroTesla = Float.MIN_VALUE
        minHeadingDegrees = Float.MAX_VALUE
        maxHeadingDegrees = Float.MIN_VALUE
        samplesCount = 0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}