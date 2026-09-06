package com.zayprojetcs.weeksk8.core.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zayprojetcs.weeksk8.core.room.model.GpsMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertGpsMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class GpsSensorHelper(
    val context: Context,
    private val scope: CoroutineScope
) {
    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var minSpeedKmh = Float.MAX_VALUE
    private var maxSpeedKmh = Float.MIN_VALUE
    private var minAltitude = Double.MAX_VALUE
    private var maxAltitude = Double.MIN_VALUE
    private var minAccuracy = Float.MAX_VALUE
    private var maxAccuracy = Float.MIN_VALUE
    private var totalDistanceMeters = 0f

    private var lastLocation: Location? = null
    private var samplesCount = 0

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                processLocation(location)
            }
        }
    }

    fun startTracking(sessionId: Long, phaseId: String) {
        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        timerJob = scope.launch {
            while (isActive) {
                delay(60_000.milliseconds)
                flushMinuteData()
            }
        }
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        timerJob?.cancel()
        lastLocation = null
    }

    fun onLocationUpdate(location: Location) {
        synchronized(this) {
            val speedKmh = location.speed * 3.6f
            val accuracy = location.accuracy
            val altitude = location.altitude

            if (lastLocation != null) {
                totalDistanceMeters += lastLocation!!.distanceTo(location)
            }
            lastLocation = location

            samplesCount++
            minSpeedKmh = minOf(minSpeedKmh, speedKmh)
            maxSpeedKmh = maxOf(maxSpeedKmh, speedKmh)
            minAltitude = minOf(minAltitude, altitude)
            maxAltitude = maxOf(maxAltitude, altitude)
            minAccuracy = minOf(minAccuracy, accuracy)
            maxAccuracy = maxOf(maxAccuracy, accuracy)
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (samplesCount == 0) return

            val entity = GpsMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                minSpeedKmh = if (minSpeedKmh == Float.MAX_VALUE) 0f else minSpeedKmh,
                maxSpeedKmh = if (maxSpeedKmh == Float.MIN_VALUE) 0f else maxSpeedKmh,
                minAltitudeMeters = if (minAltitude == Double.MAX_VALUE) 0.0 else minAltitude,
                maxAltitudeMeters = if (maxAltitude == Double.MIN_VALUE) 0.0 else maxAltitude,
                minAccuracyMeters = if (minAccuracy == Float.MAX_VALUE) 0f else minAccuracy,
                maxAccuracyMeters = if (maxAccuracy == Float.MIN_VALUE) 0f else maxAccuracy,
                totalDistanceMeters = totalDistanceMeters
            )

            scope.launch { context.repoRoomInsertGpsMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minSpeedKmh = Float.MAX_VALUE
        maxSpeedKmh = Float.MIN_VALUE
        minAltitude = Double.MAX_VALUE
        maxAltitude = Double.MIN_VALUE
        minAccuracy = Float.MAX_VALUE
        maxAccuracy = Float.MIN_VALUE
        totalDistanceMeters = 0f
        samplesCount = 0
    }

    private fun processLocation(location: Location) {
        onLocationUpdate(location)
    }
}