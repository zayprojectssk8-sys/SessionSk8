package com.zayprojetcs.weeksk8.screens.detail_session_skate

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager
import com.zayprojetcs.weeksk8.core.helper.NodeClientAppHelper
import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.model.combine.CombinedMinuteMetrics
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetIdSessionFlow
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetSensorMetrics
import com.zayprojetcs.weeksk8.screens.detail_session_skate.helper.DetailSessionUiManager
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.ActiveSessionUiState
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PhaseSensorSummary
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PropertyStatus
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.SensorDataPoint
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.SessionDetailUiState
import com.zayprojetcs.weeksk8.utils.formatSecondsToHHMMSS
import com.zayprojetcs.weeksk8.utils.formatSecondsToMMSS
import com.zayprojetcs.weeksk8.utils.getRequiredSessionPermissionsGranted
import com.zaysk8.core.helper.SessionSyncManager
import com.zaysk8.core.helper.SyncTimestamps
import com.zaysk8.core.model.SkateSessionPhase
import com.zaysk8.core.model.SkateSessionStateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionDetailViewModel(application: Application) : AndroidViewModel(application) {

    val dataStoreAppManager by lazy { DataStoreAppManager(application.applicationContext) }
    val nodeClientAppHelper by lazy { NodeClientAppHelper(application.applicationContext) }
    val sessionSyncManager by lazy { SessionSyncManager(application.applicationContext) }


    // Estado interno para almacenar los resúmenes de sensores por fase (Key: "WARMUP_1", "SKATE_1", etc.)
    private val _phaseSummaries = MutableStateFlow<Map<String, PhaseSensorSummary>>(emptyMap())

    // Evita realizar peticiones duplicadas a Room para fases que ya fueron consultadas
    private val loadedPhases = mutableSetOf<String>()

    private val _uiState = MutableStateFlow(SessionDetailUiState())

    init {
        searchConnectedWearOs()
    }

    private fun searchConnectedWearOs() {
        // 1. Iniciamos la corrutina en el hilo principal (comportamiento por defecto de viewModelScope)
        viewModelScope.launch {

            // UI actualiza a estado de carga en el Hilo Principal
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 2. Cambiamos temporalmente a Dispatchers.IO solo para hacer la búsqueda
                val deviceWearableConnect = withContext(Dispatchers.IO) {
                    nodeClientAppHelper.getConnectedNodes()
                }

                // 3. Al terminar withContext, regresamos automáticamente al Hilo Principal
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        deviceWearable = deviceWearableConnect
                    )
                }
            } catch (e: Exception) {
                Log.e("WearConnect", "Error obteniendo nodos", e)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Error desconocido"
                    )
                }
            }
        }
    }

    /*val uiState2: StateFlow<SessionDetailUiState> = combine(
        application.repoRoomGetIdSessionFlow(dataStoreAppManager.currentIdSession!!),
        DetailSessionUiManager.sessionState,
        _phaseSummaries,
        _uiState
    ) { roomSession, serviceState, phaseSummaries, uiState ->
        mapToUiState(roomSession, serviceState, phaseSummaries, uiState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionDetailUiState(isLoading = true)
    )*/

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SessionDetailUiState> = dataStoreAppManager.currentIdSession
        .flatMapLatest { sessionId ->
            if (sessionId == null) {
                flowOf(
                    SessionDetailUiState(
                        isLoading = false,
                        errorMessage = "No hay sesión seleccionada"
                    )
                )
            } else {
                combine(
                    application.repoRoomGetIdSessionFlow(sessionId),
                    DetailSessionUiManager.sessionState,
                    _phaseSummaries,
                    _uiState
                ) { roomSession, serviceState, phaseSummaries, localUiState ->
                    mapToUiState(roomSession, serviceState, phaseSummaries, localUiState)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionDetailUiState(isLoading = true)
        )

    fun onPermissionGranted() {
        _uiState.update { it.copy(permissionSessionGranted = true) }
    }

    private fun mapToUiState(
        session: RoomSession?,
        serviceState: SkateSessionStateModel,
        phaseSummaries: Map<String, PhaseSensorSummary>,
        uiState: SessionDetailUiState
    ): SessionDetailUiState {
        if (session == null) return SessionDetailUiState(isLoading = true)

        val currentPhase = serviceState.currentPhase

        val warmupStatus = when {
            session.warmupMinutes == 0 -> PropertyStatus.DISABLED
            currentPhase == SkateSessionPhase.WARMUP -> PropertyStatus.IN_PROGRESS
            serviceState.isSessionStarted && currentPhase != SkateSessionPhase.WARMUP -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        val skateRoundsStatus = when (currentPhase) {
            SkateSessionPhase.SKATE, SkateSessionPhase.REST -> PropertyStatus.IN_PROGRESS
            SkateSessionPhase.STRETCHING, SkateSessionPhase.EXTRA_TIME, SkateSessionPhase.COMPLETED -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        val cooldownStatus = when {
            session.cooldownMinutes == 0 -> PropertyStatus.DISABLED
            currentPhase == SkateSessionPhase.STRETCHING -> PropertyStatus.IN_PROGRESS
            currentPhase == SkateSessionPhase.COMPLETED -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        // --- DISPARAR CONSULTA DE SENSORES AL COMPLETAR CADA FASE ---

        if (warmupStatus == PropertyStatus.COMPLETED) {
            val phaseKey = "${SkateSessionPhase.WARMUP.name}_1"
            loadPhaseSummary(session.idSession, phaseKey, SkateSessionPhase.WARMUP, 1)
        }

        if (skateRoundsStatus == PropertyStatus.COMPLETED) {
            val totalRounds = if (serviceState.totalRounds > 0) serviceState.totalRounds else 1
            for (round in 1..totalRounds) {
                val phaseKey = "${SkateSessionPhase.SKATE.name}_$round"
                loadPhaseSummary(session.idSession, phaseKey, SkateSessionPhase.SKATE, round)
            }
        }

        if (cooldownStatus == PropertyStatus.COMPLETED) {
            val phaseKey = "${SkateSessionPhase.STRETCHING.name}_1"
            loadPhaseSummary(session.idSession, phaseKey, SkateSessionPhase.STRETCHING, 1)
        }

        return uiState.copy(
            session = session,
            skateSessionStateModel = serviceState,
            activeState = ActiveSessionUiState(
                isRunning = serviceState.isRunning,
                isPaused = serviceState.isPaused,
                isSessionStarted = serviceState.isSessionStarted,
                isWaitingUserAction = serviceState.isWaitingManualStart,
                phase = currentPhase,
                currentRound = serviceState.currentRound,
                totalRounds = serviceState.totalRounds,
                generalElapsedTimeSec = serviceState.generalElapsedTimeSec,
                phaseTimeRemainingSec = serviceState.phaseTimeRemainingSec
            ),
            formattedTotalTimer = formatSecondsToHHMMSS(serviceState.generalElapsedTimeSec),
            formattedPhaseTimer = formatSecondsToMMSS(serviceState.phaseTimeRemainingSec),
            warmupStatus = warmupStatus,
            skateRoundsStatus = skateRoundsStatus,
            cooldownStatus = cooldownStatus,
            phaseSummaries = phaseSummaries,
            permissionSessionGranted = application.getRequiredSessionPermissionsGranted(),
            isLoading = false
        )
    }

    /**
     * Consulta los datos agregados en Room para una fase específica y los emite en el estado.
     */
    private fun loadPhaseSummary(
        sessionId: Long,
        phaseKey: String,
        phase: SkateSessionPhase,
        roundNumber: Int
    ) {
        if (loadedPhases.contains(phaseKey)) return
        loadedPhases.add(phaseKey)

        viewModelScope.launch {
            application.repoRoomGetSensorMetrics(sessionId, phaseKey)
                .map { minuteMetricsList ->
                    minuteMetricsList.toPhaseSensorSummary(
                        phase = phase,
                        roundNumber = roundNumber
                    )
                }
                .collect { summary ->
                    _phaseSummaries.update { currentMap ->
                        currentMap + (phaseKey to summary)
                    }
                }
        }
    }

    fun onWearableSelected(device: DeviceWearable) = viewModelScope.launch {
        val nodeId = device.nodeId ?: return@launch
        val currentRoomSession = uiState.value.session

        // 1. Determinar fase inicial y duración con valores de respaldo para evitar ceros
        val warmupSec = (currentRoomSession?.warmupMinutes ?: 5) * 60L
        val skateSec = (currentRoomSession?.totalSkateTime ?: 10) * 60L
        val restSec = (currentRoomSession?.totalRestTime ?: 2) * 60L
        val stretchingSec = (currentRoomSession?.cooldownMinutes ?: 5) * 60L
        val totalRounds = currentRoomSession?.calculatedRounds ?: 3

        // Construcción del modelo asegurando `hasActiveSessionConfig = true`
        val sessionStateToSync = SkateSessionStateModel(
            hasActiveSessionConfig = true, // CRÍTICO: Indica al reloj que existe una configuración
            isSessionStarted = false,
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = true,
            currentPhase = SkateSessionPhase.WARMUP,
            currentRound = 1,
            totalRounds = totalRounds,
            warmupDurationSec = warmupSec,
            skateDurationSec = skateSec,
            restDurationSec = restSec,
            stretchingDurationSec = stretchingSec,
            phaseTotalDurationSec = warmupSec,
            phaseTimeRemainingSec = warmupSec
        )

        // 3. Guardar en DataClient
        sessionSyncManager.updateSessionState(
            state = sessionStateToSync,
            timestamps = SyncTimestamps()
        )

        // 4. Abrir la app en el reloj
        if (device.isAppInstalled) {
            nodeClientAppHelper.automaticOpenWearApp(nodeId)
        } else {
            nodeClientAppHelper.promptInstallOnWearTest(nodeId)
            _uiState.update {
                it.copy(userMessage = "Por favor, completa la instalación en tu reloj para continuar.")
            }
        }
    }
}

fun List<CombinedMinuteMetrics>.toPhaseSensorSummary(
    phase: SkateSessionPhase,
    roundNumber: Int = 1
): PhaseSensorSummary {
    if (isEmpty()) {
        return PhaseSensorSummary(phase = phase, roundNumber = roundNumber)
    }

    // 1. Total de pasos en la fase (suma de cada minuto)
    val totalSteps = sumOf { it.stepCount ?: 0 }

    // 2. Impacto máximo registrado en cualquier minuto de la fase
    val maxGForce = mapNotNull { it.maxImpactG }.maxOrNull() ?: 0f

    // 3. Promedio del giroscopio a lo largo de los minutos con datos
    val gyroValues = mapNotNull { it.avgRotationDps }
    val avgGyroscope = if (gyroValues.isNotEmpty()) gyroValues.average().toFloat() else 0f

    // 4. Ganancia de elevación (Diferencia entre la altitud máxima y mínima registrada)
    val minAlt = mapNotNull { it.minRelativeAltitudeM }.minOrNull() ?: 0f
    val maxAlt = mapNotNull { it.maxRelativeAltitudeM }.maxOrNull() ?: 0f
    val elevationGain = (maxAlt - minAlt).coerceAtLeast(0f)

    // 5. Puntos de la gráfica usando el índice del minuto y el pico de impacto/aceleración
    val movementPoints = map { minuteMetric ->
        SensorDataPoint(
            timestamp = minuteMetric.minuteIndex.toLong(),
            value = minuteMetric.maxImpactG ?: minuteMetric.maxBodyAccel ?: 0f
        )
    }

    return PhaseSensorSummary(
        phase = phase,
        roundNumber = roundNumber,
        totalSteps = totalSteps,
        maxGForce = maxGForce,
        avgGyroscope = avgGyroscope,
        elevationGainMeters = elevationGain,
        movementPoints = movementPoints
    )
}