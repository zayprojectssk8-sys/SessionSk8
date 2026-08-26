package com.sk8.appwatch.presentation.screen.skate_session

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.sk8.appwatch.presentation.core.services.SkateTrackingService
import com.sk8.appwatch.presentation.core.wear_helper.WearExerciseManager
import com.sk8.appwatch.presentation.screen.skate_session.ui_state.SkateSessionUiStateModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class SkateSessionViewModel(application: Application) : AndroidViewModel(application) {
    val wearExerciseManager by lazy { WearExerciseManager(application) }

    private val _uiState = MutableStateFlow(SkateSessionUiStateModel())
    val uiState: StateFlow<SkateSessionUiStateModel> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    fun onToggleSession() {
        if (_uiState.value.isTracking) {
            stopSession()
        } else {
            startSession()
        }
    }

    private fun startSession() {
        // 1. Iniciar Foreground Service para evitar que Wear OS mate la app con la pantalla apagada
        startForegroundService()

        // 2. Escuchar el flujo completo de métricas
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTracking = true)
            Log.wtf(javaClass.simpleName, "startSkateSession startSession")

            wearExerciseManager.startSkateSession().collect { metrics ->
                Log.wtf(
                    javaClass.simpleName,
                    "Metrics Update: BPM=${metrics.bpm}, Speed=${metrics.currentSpeedKmH}"
                )

                // Mapeo directo de MetricsSession al UI State
                _uiState.value = _uiState.value.copy(
                    isTracking = true,
                    bpm = metrics.bpm,
                    calories = metrics.calories,
                    currentSpeedKmH = metrics.currentSpeedKmH,
                    maxSpeedKmH = metrics.maxSpeedKmH,
                    avgSpeedKmH = metrics.avgSpeedKmH,
                    distanceMeters = metrics.distanceMeters,
                    distanceKm = metrics.distanceKm,
                    activeTimeSeconds = metrics.activeTimeSeconds,
                    restTimeSeconds = metrics.restTimeSeconds,
                    isCurrentlyActive = metrics.isCurrentlyActive
                )
            }
        }
    }

    private fun stopSession() {
        viewModelScope.launch {
            Log.wtf(javaClass.simpleName, "startSkateSession stopSession")

            // 1. Cancelar recolección y detener Health Services + Health Connect
            collectJob?.cancel()
            wearExerciseManager.stopSkateSession()

            // 2. Detener Foreground Service
            stopForegroundService()

            // 3. Resetear UI State
            _uiState.value = SkateSessionUiStateModel(isTracking = false)
        }
    }


    fun getRequiredSkatePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                "android.permission.health.READ_HEART_RATE",
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else { // API 30 a 33 (Wear OS 3.0 - 4.0 legacy)
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun areStandardPermissionsGranted(): Boolean {
        return getRequiredSkatePermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                application,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectJob?.cancel()
    }

    private fun startForegroundService() {
        val intent = Intent(getApplication(), SkateTrackingService::class.java)
        getApplication<Application>().startForegroundService(intent)
    }

    private fun stopForegroundService() {
        val intent = Intent(getApplication(), SkateTrackingService::class.java)
        getApplication<Application>().stopService(intent)
    }
}