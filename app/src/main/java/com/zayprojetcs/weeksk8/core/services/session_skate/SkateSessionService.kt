package com.zayprojetcs.weeksk8.core.services.session_skate

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zayprojetcs.weeksk8.MainActivity
import com.zayprojetcs.weeksk8.core.helper.manager.SensorSessionManager
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetIdSession
import com.zayprojetcs.weeksk8.screens.detail_session_skate.helper.DetailSessionUiManager
import com.zaysk8.core.helper.SessionSyncManager
import com.zaysk8.core.helper.SyncTimestamps
import com.zaysk8.core.model.SkateSessionPhase
import com.zaysk8.core.model.SkateSessionStateModel
import com.zaysk8.core.utils.ContinuousVibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class SkateSessionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    // Duraciones individuales en segundos
    private var totalSessionDurationSec: String = ""
    private var warmupDurationSec = 0L
    private var skatePerRoundDurationSec = 0L   // Tiempo exacto de UNA ronda de patinaje
    private var restPerRoundDurationSec = 0L    // Tiempo exacto de UN descanso
    private var extraTimeDurationSec = 0L
    private var stretchingDurationSec = 0L

    // Módulos de Sincronización, Sensores y Retroalimentación
    private lateinit var continuousVibrator: ContinuousVibrator
    private lateinit var sessionSyncManager: SessionSyncManager
    private lateinit var sensorSessionManager: SensorSessionManager

    companion object {
        const val ACTION_START_SESSION = "ACTION_START_SESSION"
        const val ACTION_START_FROM_WATCH = "ACTION_START_FROM_WATCH"
        const val ACTION_START_NEXT_PHASE = "ACTION_START_NEXT_PHASE"
        const val ACTION_START_FINISH_ROUNDS = "ACTION_START_FINISH_ROUNDS"
        const val ACTION_PAUSE_TOGGLE = "ACTION_PAUSE_TOGGLE"
        const val ACTION_STOP_SESSION = "ACTION_STOP_SESSION"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"

        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "skate_session_channel"

        private val _currentRoomSession = MutableStateFlow<RoomSession?>(null)
        private val _sessionState = MutableStateFlow(SkateSessionStateModel())
        val sessionState: StateFlow<SkateSessionStateModel> = _sessionState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        continuousVibrator = ContinuousVibrator(this)
        sessionSyncManager = SessionSyncManager(applicationContext)

        sensorSessionManager = SensorSessionManager(
            context = applicationContext
        )

        observeWearableSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SESSION -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId != -1L) {
                    loadSessionAndStart(sessionId)
                }
            }

            ACTION_START_FROM_WATCH -> {
                handleStartFromWatch()
            }

            ACTION_START_NEXT_PHASE -> startNextPhaseManually()
            ACTION_START_FINISH_ROUNDS -> finishRoundsPhaseManually()
            ACTION_PAUSE_TOGGLE -> togglePause()
            ACTION_STOP_SESSION -> cancelSession()
        }
        return START_STICKY
    }

    private fun finishRoundsPhaseManually() {
        val currentState = _sessionState.value

        // Validar que la sesión esté activa y sea de rondas no definidas/infinitas (totalRounds <= 0)
        if (!currentState.isRunning || currentState.totalRounds > 0) return

        // 1. Detener vibración y sensores de la fase de rondas actual
        continuousVibrator.stopVibration()
        sensorSessionManager.stopAll()

        // 2. Obtener la fase posterior a las rondas (EXTRA_TIME, STRETCHING o COMPLETED) y su duración
        val nextPhase = getPostRoundsPhase()
        val duration = getPostRoundsDuration(nextPhase)

        // 3. Si no hay fases posteriores configuradas o la duración es 0, finalizar la sesión
        if (nextPhase == SkateSessionPhase.COMPLETED || duration <= 0L) {
            finishSession(syncToWear = true)
            return
        }

        // 4. Construir el estado preparado para la siguiente fase (esperando inicio manual)
        val updatedState = currentState.copy(
            isFinishRounds = true,
            isPaused = false,
            isWaitingManualStart = true,
            currentPhase = nextPhase,
            phaseTimeRemainingSec = duration,
            phaseTotalDurationSec = duration,
            phaseStartTimeMs = System.currentTimeMillis()
        )

        // 5. Alertar al usuario sobre la nueva fase mediante vibración continua
        continuousVibrator.startContinuousVibration()

        // 6. Actualizar la UI, la notificación y sincronizar el estado con Wear OS
        updateServiceState(updatedState, syncToWear = true)
    }

    private fun handleStartFromWatch() {
        createNotificationChannel()

        val currentState = _sessionState.value
        val notification = buildNotification(currentState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            val latestSync = sessionSyncManager.observeSessionState().firstOrNull()
            if (latestSync != null) {
                val (remoteState, _) = latestSync
                _sessionState.value = remoteState
                DetailSessionUiManager.updateState(remoteState)
                updateNotification(remoteState)

                if (remoteState.isSessionStarted) {
                    sensorSessionManager.switchPhase(remoteState.currentPhase.name)
                    _currentRoomSession.value?.let { sessionId ->
                        sensorSessionManager.startPhase(
                            sessionId.idSession,
                            remoteState.currentPhase.name
                        )
                    }
                }
            }

            if (timerJob?.isActive != true) {
                runTimerLoop()
            }
        }
    }

    private fun observeWearableSync() {
        sessionSyncManager.observeSessionState()
            .onEach { (remoteState, _) ->
                val currentLocalState = _sessionState.value

                if (remoteState != currentLocalState) {

                    if (remoteState.isSessionStarted && !currentLocalState.isSessionStarted) {
                        continuousVibrator.stopVibration()
                        sensorSessionManager.switchPhase(remoteState.currentPhase.name)
                        _currentRoomSession.value?.let { sessionId ->
                            sensorSessionManager.startPhase(
                                sessionId.idSession,
                                remoteState.currentPhase.name
                            )
                        }

                        if (timerJob?.isActive != true) {
                            runTimerLoop()
                        }
                    }

                    if (currentLocalState.isWaitingManualStart && !remoteState.isWaitingManualStart) {
                        continuousVibrator.stopVibration()
                        sensorSessionManager.switchPhase(remoteState.currentPhase.name)
                        _currentRoomSession.value?.let { sessionId ->
                            sensorSessionManager.startPhase(
                                sessionId.idSession,
                                remoteState.currentPhase.name
                            )
                        }
                    }

                    if (remoteState.isPaused != currentLocalState.isPaused) {
                        _currentRoomSession.value?.let { sessionId ->
                            if (remoteState.isPaused) {
                                sensorSessionManager.stopAll()
                            } else {
                                sensorSessionManager.startPhase(
                                    sessionId.idSession,
                                    remoteState.currentPhase.name
                                )
                            }
                        }
                    }

                    if (!remoteState.isRunning && currentLocalState.isRunning) {
                        finishSession(syncToWear = false)
                        return@onEach
                    }

                    _sessionState.value = remoteState
                    DetailSessionUiManager.updateState(remoteState)
                    updateNotification(remoteState)
                }
            }
            .launchIn(serviceScope)
    }

    private fun updateServiceState(newState: SkateSessionStateModel, syncToWear: Boolean = false) {
        _sessionState.value = newState
        DetailSessionUiManager.updateState(newState)
        updateNotification(newState)

        if (syncToWear) {
            serviceScope.launch {
                val timestamps = SyncTimestamps(
                    overallStartTimeMs = newState.overallStartTimeMs,
                    phaseStartTimeMs = newState.phaseStartTimeMs
                )
                sessionSyncManager.updateSessionState(newState, timestamps)
            }
        }
    }

    private fun loadSessionAndStart(sessionId: Long) {
        serviceScope.launch {
            DetailSessionUiManager.resetState()

            val session = getDataSession(sessionId)
            session ?: return@launch

            withContext(Dispatchers.Default) {
                startSession(session)
            }
        }
    }

    suspend fun getDataSession(sessionId: Long): RoomSession? {
        val session = application.repoRoomGetIdSession(sessionId) ?: return null
        _currentRoomSession.value = session

        totalSessionDurationSec = session.getDurationTimeSession()
        warmupDurationSec = session.warmupMinutes * 60L

        // Uso directo de las propiedades por ronda (en segundos)
        // Nota: Si roundSkateTime/roundRestTime están en minutos en Room, multiplica por 60L
        skatePerRoundDurationSec = session.roundSkateTime * 60L
        restPerRoundDurationSec = session.roundRestTime * 60L

        extraTimeDurationSec = (session.marginMinutes ?: 0) * 60L
        stretchingDurationSec = session.cooldownMinutes * 60L
        return session
    }

    private fun startSession(session: RoomSession) {
        if (_sessionState.value.isRunning) return

        createNotificationChannel()

        val initialPhase =
            if (warmupDurationSec > 0) SkateSessionPhase.WARMUP else SkateSessionPhase.SKATE
        val initialDuration =
            if (initialPhase == SkateSessionPhase.WARMUP) warmupDurationSec else skatePerRoundDurationSec

        // Si calculatedRounds > 0 se usa dicho límite, de lo contrario 0 indica Rondas Infinitas
        val totalRoundsCalculated =
            if (session.calculatedRounds > 0) session.calculatedRounds else 0

        val initialState = SkateSessionStateModel(
            hasActiveSessionConfig = true,
            isRunning = true,
            isPaused = false,
            isSessionStarted = false,
            isWaitingManualStart = true,
            currentPhase = initialPhase,
            currentRound = 1,
            totalRounds = totalRoundsCalculated,
            generalElapsedTimeSec = 0L,
            phaseTimeRemainingSec = initialDuration,
            phaseTotalDurationSec = initialDuration,
            warmupDurationSec = warmupDurationSec,
            skateDurationSec = skatePerRoundDurationSec,
            restDurationSec = restPerRoundDurationSec,
            stretchingDurationSec = stretchingDurationSec,
            overallStartTimeMs = System.currentTimeMillis()
        )

        _sessionState.value = initialState
        continuousVibrator.startContinuousVibration()

        val notification = buildNotification(initialState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            val timestamps = SyncTimestamps(overallStartTimeMs = initialState.overallStartTimeMs)
            sessionSyncManager.updateSessionState(initialState, timestamps)
        }

        runTimerLoop()
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L.milliseconds)
                val currentState = _sessionState.value

                if (!currentState.isRunning) continue

                val newGeneralTime = if (currentState.isSessionStarted && !currentState.isPaused) {
                    currentState.generalElapsedTimeSec + 1
                } else {
                    currentState.generalElapsedTimeSec
                }

                val isPhaseActive = !currentState.isPaused && !currentState.isWaitingManualStart

                if (isPhaseActive) {
                    val newPhaseTime = currentState.phaseTimeRemainingSec - 1

                    if (newPhaseTime <= 0) {
                        handlePhaseCompletion(currentState, newGeneralTime)
                    } else {
                        val updatedState = currentState.copy(
                            generalElapsedTimeSec = newGeneralTime,
                            phaseTimeRemainingSec = newPhaseTime
                        )
                        updateServiceState(updatedState, syncToWear = false)
                    }
                } else {
                    val updatedState = currentState.copy(
                        generalElapsedTimeSec = newGeneralTime
                    )
                    updateServiceState(updatedState, syncToWear = false)
                }
            }
        }
    }

    private fun handlePhaseCompletion(state: SkateSessionStateModel, currentGeneralTime: Long) {
        sensorSessionManager.stopAll()

        val (nextPhase, nextRound, duration) = calculateNextPhaseAndRound(state)

        if (nextPhase == SkateSessionPhase.COMPLETED) {
            finishSession(syncToWear = true)
            return
        }

        if (duration <= 0) {
            val intermediateState = state.copy(currentPhase = nextPhase, currentRound = nextRound)
            handlePhaseCompletion(intermediateState, currentGeneralTime)
            return
        }

        val waitingState = state.copy(
            isPaused = false,
            isWaitingManualStart = true,
            currentPhase = nextPhase,
            currentRound = nextRound,
            generalElapsedTimeSec = currentGeneralTime,
            phaseTimeRemainingSec = duration,
            phaseTotalDurationSec = duration,
            phaseStartTimeMs = System.currentTimeMillis()
        )

        continuousVibrator.startContinuousVibration()
        updateServiceState(waitingState, syncToWear = true)
    }

    private fun calculateNextPhaseAndRound(state: SkateSessionStateModel): Triple<SkateSessionPhase, Int, Long> {
        val isInfiniteRounds = state.totalRounds <= 0

        return when (state.currentPhase) {
            SkateSessionPhase.WARMUP -> {
                Triple(SkateSessionPhase.SKATE, 1, skatePerRoundDurationSec)
            }

            SkateSessionPhase.SKATE -> {
                if (state.isFinishRounds && state.totalRounds <= 0) {
                    val postPhase = getPostRoundsPhase()
                    return Triple(postPhase, state.currentRound, getPostRoundsDuration(postPhase))
                }
                if (restPerRoundDurationSec > 0) {
                    Triple(SkateSessionPhase.REST, state.currentRound, restPerRoundDurationSec)
                } else if (isInfiniteRounds || state.currentRound < state.totalRounds) {
                    Triple(
                        SkateSessionPhase.SKATE,
                        state.currentRound + 1,
                        skatePerRoundDurationSec
                    )
                } else {
                    val postPhase = getPostRoundsPhase()
                    Triple(postPhase, state.currentRound, getPostRoundsDuration(postPhase))
                }
            }

            SkateSessionPhase.REST -> {
                if (isInfiniteRounds || state.currentRound < state.totalRounds) {
                    Triple(
                        SkateSessionPhase.SKATE,
                        state.currentRound + 1,
                        skatePerRoundDurationSec
                    )
                } else {
                    val postPhase = getPostRoundsPhase()
                    Triple(postPhase, state.currentRound, getPostRoundsDuration(postPhase))
                }
            }

            SkateSessionPhase.EXTRA_TIME -> {
                if (stretchingDurationSec > 0) {
                    Triple(SkateSessionPhase.STRETCHING, state.currentRound, stretchingDurationSec)
                } else {
                    Triple(SkateSessionPhase.COMPLETED, state.currentRound, 0L)
                }
            }

            SkateSessionPhase.STRETCHING -> {
                Triple(SkateSessionPhase.COMPLETED, state.currentRound, 0L)
            }

            SkateSessionPhase.COMPLETED -> Triple(
                SkateSessionPhase.COMPLETED,
                state.currentRound,
                0L
            )

            SkateSessionPhase.NOT_STARTED -> Triple(
                SkateSessionPhase.NOT_STARTED,
                state.currentRound,
                0L
            )

            SkateSessionPhase.CANCEL -> Triple(
                SkateSessionPhase.CANCEL,
                state.currentRound,
                0L
            )
        }
    }

    private fun startNextPhaseManually() {
        val currentState = _sessionState.value
        serviceScope.launch {
            _currentRoomSession.value?.let {
                getDataSession(it.idSession)
            }
        }
        if (currentState.isRunning && currentState.isWaitingManualStart) {
            continuousVibrator.stopVibration()

            val runningState = currentState.copy(
                isSessionStarted = true,
                isWaitingManualStart = false,
                phaseStartTimeMs = System.currentTimeMillis()
            )
            updateServiceState(runningState, syncToWear = true)

            sensorSessionManager.switchPhase(runningState.currentPhase.name)

            _currentRoomSession.value?.let { sessionId ->
                sensorSessionManager.startPhase(sessionId.idSession, runningState.currentPhase.name)
            }
        }
    }

    private fun getPostRoundsPhase(): SkateSessionPhase {
        return when {
            extraTimeDurationSec > 0 -> SkateSessionPhase.EXTRA_TIME
            stretchingDurationSec > 0 -> SkateSessionPhase.STRETCHING
            else -> SkateSessionPhase.COMPLETED
        }
    }

    private fun getPostRoundsDuration(phase: SkateSessionPhase): Long {
        return when (phase) {
            SkateSessionPhase.EXTRA_TIME -> extraTimeDurationSec
            SkateSessionPhase.STRETCHING -> stretchingDurationSec
            else -> 0L
        }
    }

    private fun togglePause() {
        val currentState = _sessionState.value

        serviceScope.launch {
            _currentRoomSession.value?.let {
                getDataSession(it.idSession)
            }
        }
        if (currentState.isWaitingManualStart) {
            startNextPhaseManually()
            return
        }
        val updatedState = currentState.copy(isPaused = !currentState.isPaused)
        updateServiceState(updatedState, syncToWear = true)

        _currentRoomSession.value?.let { sessionId ->
            if (updatedState.isPaused) {
                sensorSessionManager.stopAll()
            } else {
                sensorSessionManager.startPhase(sessionId.idSession, updatedState.currentPhase.name)
            }
        }
    }

    private fun finishSession(syncToWear: Boolean = true) {
        val finalState = _sessionState.value.copy(
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = false,
            currentPhase = SkateSessionPhase.COMPLETED
        )

        sensorSessionManager.stopAll()
        continuousVibrator.stopVibration()
        updateServiceState(finalState, syncToWear = syncToWear)

        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelSession(syncToWear: Boolean = true) {
        val finalState = _sessionState.value.copy(
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = false,
            currentPhase = SkateSessionPhase.CANCEL
        )

        sensorSessionManager.stopAll()
        continuousVibrator.stopVibration()
        updateServiceState(finalState, syncToWear = syncToWear)

        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


    // --- NOTIFICACIÓN ---

    private fun updateNotification(state: SkateSessionStateModel) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: SkateSessionStateModel): Notification {
        val generalFormatted = formatTime(state.generalElapsedTimeSec)
        val phaseFormatted = formatTime(state.phaseTimeRemainingSec)

        // Muestra "Ronda X/Y" si totalRounds > 0 o "Ronda X" si es infinta
        val roundLabel = if (state.totalRounds > 0) {
            "Ronda ${state.currentRound}/${state.totalRounds}"
        } else {
            "Ronda ${state.currentRound}"
        }

        // 1. Título formateado de la fase
        val phaseDisplayName = when (state.currentPhase) {
            SkateSessionPhase.SKATE, SkateSessionPhase.REST ->
                "${state.currentPhase.displayName} ($roundLabel)"

            else -> state.currentPhase.displayName
        }

        // 2. Encabezado dinámico según el estado
        val title = when {
            !state.hasActiveSessionConfig -> "Sin Sesión Configurada"
            !state.isSessionStarted -> "Sesión Lista para Iniciar"
            state.isWaitingManualStart -> "🔔 ¡Iniciar $phaseDisplayName!"
            state.isPaused -> "⏸ Pausado - $phaseDisplayName"
            else -> "$phaseDisplayName $phaseFormatted"
        }

        // 3. Subtexto de estado detallado
        val statusDescription = when {
            !state.isSessionStarted -> "Toca para abrir la aplicación e iniciar."
            state.isWaitingManualStart -> "Esperando confirmación para empezar la fase."
            state.isPaused -> "La sesión se encuentra en pausa."
            else -> "Sesión en curso..."
        }

        // 4. Texto corto (para la vista comprimida) y Expandido (BigTextStyle)
        val shortContent = "TIEMPO TOTAL DE LA SESION: $totalSessionDurationSec"
        val expandedContent = StringBuilder().apply {
            append("CONTENIDO DE LA SESSIÓN:\n")
            if (warmupDurationSec > 0L) {
                append("CALENTAMIENTO: ${formatTime(warmupDurationSec)} min\n")
            }
            if (state.totalRounds > 0) {
                append(
                    "SKATE: ${state.totalRounds} rondas de ${formatTime(skatePerRoundDurationSec)} min patinando y ${
                        formatTime(restPerRoundDurationSec)
                    } min descansando.\n"
                )
            } else {
                append(
                    "SKATE: Rondas infinitas de ${formatTime(skatePerRoundDurationSec)} min patinando y ${
                        formatTime(restPerRoundDurationSec)
                    } min descansando.\n"
                )
            }
            if (extraTimeDurationSec > 0L) {
                append("TIEMPO EXTRA: ${formatTime(extraTimeDurationSec)} min\n")
            }

            if (stretchingDurationSec > 0L) {
                append("ESTIRAMIENTO: ${formatTime(stretchingDurationSec)} min")
            }

        }.toString()

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            200,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedContent))
            .setSubText("SESIÓN SKATE - $generalFormatted")
            .setSmallIcon(R.drawable.ic_media_play)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(this, com.zayprojetcs.weeksk8.R.color.skate_accent))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state.isSessionStarted && state.phaseTotalDurationSec > 0) {
            val maxProgress = state.phaseTotalDurationSec.toInt()
            val elapsedTimeInPhase =
                (state.phaseTotalDurationSec - state.phaseTimeRemainingSec).toInt()
            builder.setProgress(maxProgress, elapsedTimeInPhase.coerceIn(0, maxProgress), false)
        } else {
            builder.setProgress(0, 0, false)
        }

        if (state.isWaitingManualStart) {
            builder.setFullScreenIntent(contentPendingIntent, true)
        }

        if (state.isWaitingManualStart || !state.isSessionStarted) {
            val startIntent = Intent(this, SkateSessionService::class.java).apply {
                action = ACTION_START_NEXT_PHASE
            }
            val startPendingIntent = PendingIntent.getService(
                this,
                101,
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_media_play, "▶ COMANZAR FASE", startPendingIntent)

            if (state.totalRounds == 0 && state.currentRound >= 2 && state.currentPhase == SkateSessionPhase.REST) {
                val finishRoundIntent = Intent(this, SkateSessionService::class.java).apply {
                    action = ACTION_START_FINISH_ROUNDS
                }
                val finishRoundPendingIntent = PendingIntent.getService(
                    this,
                    101,
                    finishRoundIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.ic_media_play,
                    "FINALIZAR RONDAS",
                    finishRoundPendingIntent
                )
            }


        } else {
            val pauseIntent = Intent(this, SkateSessionService::class.java).apply {
                action = ACTION_PAUSE_TOGGLE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                102,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val actionText = if (state.isPaused) "▶ REANUDAR" else "⏸ PAUSAR"
            val actionIcon =
                if (state.isPaused) R.drawable.ic_media_play else R.drawable.ic_media_pause
            builder.addAction(actionIcon, actionText, pausePendingIntent)
        }

        /*val stopIntent = Intent(this, SkateSessionService::class.java).apply {
            action = ACTION_STOP_SESSION
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 103, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(R.drawable.ic_menu_close_clear_cancel, "⏹ CANCELAR", stopPendingIntent)*/

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cronómetro de Sesión de Skate",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (::sensorSessionManager.isInitialized) {
            sensorSessionManager.destroy()
        }

        if (_sessionState.value.currentPhase == SkateSessionPhase.CANCEL) {
            DetailSessionUiManager.resetState()
        }

        serviceScope.cancel()
        super.onDestroy()
    }
}