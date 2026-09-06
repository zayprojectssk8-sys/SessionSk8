package com.zayprojetcs.weeksk8.core.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.zayprojetcs.weeksk8.core.room.model.StepMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertStepMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class StepDetectorHelper(
    val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val isAvailable: Boolean = stepSensor != null

    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var stepCountInMinute = 0
    private var minCadenceSpm = Float.MAX_VALUE
    private var maxCadenceSpm = Float.MIN_VALUE
    private val stepTimestamps = mutableListOf<Long>()

    fun startTracking(sessionId: Long, phaseId: String) {
        if (!isAvailable) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
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
        if (event.values[0] == 1.0f) {
            val now = System.currentTimeMillis()
            synchronized(this) {
                stepCountInMinute++
                stepTimestamps.add(now)

                // Calcular cadencia instantánea entre los últimos 2 pasos
                if (stepTimestamps.size >= 2) {
                    val timeDiffMs = now - stepTimestamps[stepTimestamps.size - 2]
                    if (timeDiffMs > 0) {
                        val instantCadence = (60_000f / timeDiffMs)
                        minCadenceSpm = minOf(minCadenceSpm, instantCadence)
                        maxCadenceSpm = maxOf(maxCadenceSpm, instantCadence)
                    }
                }
            }
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            val entity = StepMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                stepCount = stepCountInMinute,
                minCadenceSpm = if (minCadenceSpm == Float.MAX_VALUE) 0f else minCadenceSpm,
                maxCadenceSpm = if (maxCadenceSpm == Float.MIN_VALUE) 0f else maxCadenceSpm
            )

            scope.launch { context.repoRoomInsertStepMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        stepCountInMinute = 0
        minCadenceSpm = Float.MAX_VALUE
        maxCadenceSpm = Float.MIN_VALUE
        stepTimestamps.clear()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}