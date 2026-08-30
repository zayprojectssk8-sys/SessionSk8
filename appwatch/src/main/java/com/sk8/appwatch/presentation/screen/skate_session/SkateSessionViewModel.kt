package com.sk8.appwatch.presentation.screen.skate_session

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.sk8.appwatch.presentation.core.data_store.DataStoreWatchManager
import com.sk8.appwatch.presentation.core.helper.NodeClientWatchHelper
import com.sk8.appwatch.presentation.core.services.HealthWearExerciseManager
import com.sk8.appwatch.presentation.core.services.SkateTrackingService
import com.sk8.appwatch.presentation.screen.skate_session.ui_state.SkateSessionUiStateModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SkateSessionViewModel(application: Application) : AndroidViewModel(application) {
    val healthWearExerciseManager by lazy { HealthWearExerciseManager(application) }
    val nodeClientWatchHelper by lazy { NodeClientWatchHelper(application) }

    val dataStoreWatchManager by lazy { DataStoreWatchManager(application) }

    private val _uiState = MutableStateFlow(SkateSessionUiStateModel())
    val uiState: StateFlow<SkateSessionUiStateModel> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var messageClient: MessageClient? = null
    private var capabilityClient: CapabilityClient? = null


    fun loadCompleteLinkPhone(nodeId: String) {
        Log.wtf("javaClass.simpleName", " WearAppNavigation saveDeviceConnectNodeId: $nodeId")

        viewModelScope.launch {
            nodeClientWatchHelper.sendConnectionHandshakeToPhone()
            dataStoreWatchManager.saveDeviceConnectNodeId(nodeId = nodeId)
        }
    }

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

            healthWearExerciseManager.startSkateSession().collect { metrics ->
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
            healthWearExerciseManager.stopSkateSession()

            // 2. Detener Foreground Service
            stopForegroundService()

            // 3. Resetear UI State
            _uiState.value = SkateSessionUiStateModel(isTracking = false)
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