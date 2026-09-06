package com.zayprojetcs.weeksk8.core.helper

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.zayprojetcs.weeksk8.core.room.model.ActivityMinuteEntity
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertActivityMinute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ActivityRecognitionHelper(
    val context: Context,
    private val scope: CoroutineScope
) {
    private var sessionId: Long = 0
    private var phaseId: String = ""
    private var minuteIndex: Int = 0
    private var timerJob: Job? = null

    private var currentActivity: String = "UNKNOWN"
    private var minConfidence = Int.MAX_VALUE
    private var maxConfidence = Int.MIN_VALUE
    private var hasDataInMinute = false

    private val activityClient = ActivityRecognition.getClient(context)
    private var pendingIntent: PendingIntent? = null
    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent) ?: return
                val mostProbable = result.mostProbableActivity
                onActivityDetected(
                    activityType = toActivityString(mostProbable.type),
                    confidence = mostProbable.confidence
                )
            }
        }
    }

    companion object {
        private const val ACTION_ACTIVITY_UPDATE = "com.zayprojetcs.weeksk8.ACTION_ACTIVITY_UPDATE"
        private const val DETECTION_INTERVAL_MS = 5000L
    }

    fun hasLocationPermission(context: Context): Boolean {
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

    fun startTracking(sessionId: Long, phaseId: String) {
        this.sessionId = sessionId
        this.phaseId = phaseId
        this.minuteIndex = 0
        resetWindowMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        registerUpdates()

        timerJob = scope.launch {
            while (isActive) {
                delay(60_000.milliseconds)
                flushMinuteData()
            }
        }
    }

    fun stopTracking() {
        timerJob?.cancel()
        removeUpdates()
    }

    fun onActivityDetected(activityType: String, confidence: Int) {
        synchronized(this) {
            currentActivity = activityType
            minConfidence = minOf(minConfidence, confidence)
            maxConfidence = maxOf(maxConfidence, confidence)
            hasDataInMinute = true
        }
    }

    private fun registerUpdates() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(ACTION_ACTIVITY_UPDATE)
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            isReceiverRegistered = true
        }

        val intent = Intent(ACTION_ACTIVITY_UPDATE).setPackage(context.packageName)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        pendingIntent?.let { pi ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                activityClient.requestActivityUpdates(DETECTION_INTERVAL_MS, pi)
            }
        }
    }

    private fun removeUpdates() {
        pendingIntent?.let { pi ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                activityClient.removeActivityUpdates(pi)
            }
        }
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
    }

    private fun flushMinuteData() {
        synchronized(this) {
            if (!hasDataInMinute) return

            val entity = ActivityMinuteEntity(
                sessionId = sessionId,
                phaseId = phaseId,
                minuteIndex = minuteIndex,
                detectedActivity = currentActivity,
                minConfidence = if (minConfidence == Int.MAX_VALUE) 0 else minConfidence,
                maxConfidence = if (maxConfidence == Int.MIN_VALUE) 0 else maxConfidence
            )

            scope.launch { context.repoRoomInsertActivityMinute(entity) }
            minuteIndex++
            resetWindowMetrics()
        }
    }

    private fun resetWindowMetrics() {
        minConfidence = Int.MAX_VALUE
        maxConfidence = Int.MIN_VALUE
        hasDataInMinute = false
    }

    private fun toActivityString(type: Int): String {
        return when (type) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.WALKING -> "WALKING"
            else -> "UNKNOWN"
        }
    }
}