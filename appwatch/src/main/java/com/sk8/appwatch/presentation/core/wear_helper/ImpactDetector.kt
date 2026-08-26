package com.sk8.appwatch.presentation.core.wear_helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ImpactDetector(
    context: Context,
    private val onImpactDetected: (gForce: Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val IMPACT_THRESHOLD_MS2 = 39.24f // 4G (4 * 9.81 m/s²)
    private val COOLDOWN_MS = 3000L
    private var lastImpactTime = 0L

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)

            if (magnitude >= IMPACT_THRESHOLD_MS2) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastImpactTime > COOLDOWN_MS) {
                    lastImpactTime = currentTime
                    val gForce = magnitude / SensorManager.GRAVITY_EARTH
                    onImpactDetected(gForce)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}