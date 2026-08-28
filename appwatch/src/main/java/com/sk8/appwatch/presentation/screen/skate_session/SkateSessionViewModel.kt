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
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.sk8.appwatch.presentation.core.data_store.DataStoreWatchManager.Companion.dataStoreWatchInstance
import com.sk8.appwatch.presentation.core.services.SkateTrackingService
import com.sk8.appwatch.presentation.core.wear_helper.WearExerciseManager
import com.sk8.appwatch.presentation.screen.skate_session.ui_state.SkateSessionUiStateModel
import com.sk8.appwatch.presentation.utils.sendConnectionHandshakeToPhone
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.MESSAGE_PATH_PHONE_TO_WEAR_START_SESSION
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SkateSessionViewModel(application: Application) : AndroidViewModel(application),
    MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {
    val wearExerciseManager by lazy { WearExerciseManager(application) }

    private val _uiState = MutableStateFlow(SkateSessionUiStateModel())
    val uiState: StateFlow<SkateSessionUiStateModel> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var messageClient: MessageClient? = null
    private var capabilityClient: CapabilityClient? = null
    fun loadCompleteLinkPhone(nodeId: String) {
        viewModelScope.launch {
            application.sendConnectionHandshakeToPhone()
            application.dataStoreWatchInstance().saveDeviceConnectNodeId(nodeId)
        }
    }

    private fun startListeningForWatch() {

        messageClient = Wearable.getMessageClient(getApplication()).also {
            it.addListener(this)
        }
        capabilityClient = Wearable.getCapabilityClient(getApplication()).also {
            it.addListener(this, CAPABILITY_CLIENT_NAME)
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


    override fun onCleared() {
        super.onCleared()
        collectJob?.cancel()
        stopListening()
    }

    private fun startForegroundService() {
        val intent = Intent(getApplication(), SkateTrackingService::class.java)
        getApplication<Application>().startForegroundService(intent)
    }

    private fun stopForegroundService() {
        val intent = Intent(getApplication(), SkateTrackingService::class.java)
        getApplication<Application>().stopService(intent)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH_PHONE_TO_WEAR_START_SESSION) {
            onToggleSession()
        }

    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        val isWatchConnected = capabilityInfo.nodes.isNotEmpty()
        Log.wtf(
            javaClass.simpleName,
            "onCapabilityChanged capabilityInfo.nodes ${capabilityInfo.nodes}"
        )
        Log.wtf(javaClass.simpleName, "onCapabilityChanged isWatchConnected $isWatchConnected")
    }

    private fun stopListening() {
        messageClient?.removeListener(this)
        capabilityClient?.removeListener(this)
    }
}