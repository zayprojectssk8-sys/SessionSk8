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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.zayprojetcs.weeksk8.MainActivity
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetIdSession
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertSession
import com.zayprojetcs.weeksk8.core.services.session_skate.model.SkateSessionPhase
import com.zayprojetcs.weeksk8.core.services.session_skate.model.SkateSessionStateModel
import com.zayprojetcs.weeksk8.screens.detail_session_skate.helper.DetailSessionUiManager
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class SkateSessionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    private var currentRoomSession: RoomSession? = null

    // Duraciones individuales en segundos
    private var warmupDurationSec = 0L
    private var skatePerRoundDurationSec = 0L   // Tiempo exacto de UNA ronda de patinaje
    private var restPerRoundDurationSec = 0L    // Tiempo exacto de UN descanso
    private var extraTimeDurationSec = 0L
    private var stretchingDurationSec = 0L

    companion object {
        const val ACTION_START_SESSION = "ACTION_START_SESSION"
        const val ACTION_START_NEXT_PHASE =
            "ACTION_START_NEXT_PHASE" // Para iniciar manualmente la siguiente fase/ronda
        const val ACTION_PAUSE_TOGGLE = "ACTION_PAUSE_TOGGLE"
        const val ACTION_STOP_SESSION = "ACTION_STOP_SESSION"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"

        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "skate_session_channel"

        private val _sessionState = MutableStateFlow(SkateSessionStateModel())
        val sessionState: StateFlow<SkateSessionStateModel> = _sessionState.asStateFlow()
    }

    // 1. Declarar la instancia del vibrador
    private lateinit var continuousVibrator: ContinuousVibrator

    override fun onCreate() {
        super.onCreate()
        // 2. Inicializar en onCreate
        continuousVibrator = ContinuousVibrator(this)
    }

    private fun updateServiceState(newState: SkateSessionStateModel) {
        _sessionState.value = newState

        // 1. Notificar a toda la App (ViewModel / UI)
        DetailSessionUiManager.updateState(newState)

        // 2. Actualizar la notificación flotante
        updateNotification(newState)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SESSION -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId != -1L) {
                    loadSessionAndStart(sessionId)
                }
            }

            ACTION_START_NEXT_PHASE -> startNextPhaseManually()
            ACTION_PAUSE_TOGGLE -> togglePause()
            ACTION_STOP_SESSION -> stopSession()
        }
        return START_STICKY
    }

    private fun loadSessionAndStart(sessionId: Long) {
        serviceScope.launch {
            val session = application.repoRoomGetIdSession(sessionId) ?: return@launch

            currentRoomSession = session

            val rounds = if (session.calculatedRounds > 0) session.calculatedRounds else 1

            // 1. Cálculo del tiempo por ronda individual (Minutos a Segundos)
            warmupDurationSec = session.warmupMinutes * 60L
            skatePerRoundDurationSec = (session.totalSkateTime * 60L) / rounds
            restPerRoundDurationSec = (session.totalRestTime * 60L) / rounds
            extraTimeDurationSec = (session.marginMinutes ?: 0) * 60L
            stretchingDurationSec = session.cooldownMinutes * 60L

            withContext(Dispatchers.Default) {
                startSession(session)
            }
        }
    }

    private fun startSession(session: RoomSession) {
        if (_sessionState.value.isRunning) return

        createNotificationChannel()

        val initialPhase =
            if (warmupDurationSec > 0) SkateSessionPhase.WARMUP else SkateSessionPhase.SKATE
        val initialDuration =
            if (initialPhase == SkateSessionPhase.WARMUP) warmupDurationSec else skatePerRoundDurationSec

        val initialState = SkateSessionStateModel(
            isRunning = true,
            isPaused = false,
            isSessionStarted = false,       // Aún no arranca el tiempo general
            isWaitingManualStart = true,    // Espera toque del usuario
            currentPhase = initialPhase,
            currentRound = 1,
            totalRounds = session.calculatedRounds,
            generalElapsedTimeSec = 0L,
            phaseTimeRemainingSec = initialDuration,
            phaseTotalDurationSec = initialDuration
        )

        _sessionState.value = initialState

        // Disparar la vibración de alerta para avisar que la sesión está lista
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

        runTimerLoop()
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val currentState = _sessionState.value

                if (!currentState.isRunning) continue

                // 1. El tiempo GENERAL solo avanza si la sesión ya arrancó por primera vez (isSessionStarted = true)
                val newGeneralTime = if (currentState.isSessionStarted && !currentState.isPaused) {
                    currentState.generalElapsedTimeSec + 1
                } else {
                    currentState.generalElapsedTimeSec
                }

                // 2. El tiempo de FASE solo avanza si NO está esperando confirmación manual
                val isPhaseActive = !currentState.isPaused && !currentState.isWaitingManualStart

                if (isPhaseActive) {
                    val newPhaseTime = currentState.phaseTimeRemainingSec - 1

                    if (newPhaseTime <= 0) {
                        // Llegó a 0:00 -> Transicionar de fase y esperar al usuario
                        handlePhaseCompletion(currentState, newGeneralTime)
                    } else {
                        val updatedState = currentState.copy(
                            generalElapsedTimeSec = newGeneralTime,
                            phaseTimeRemainingSec = newPhaseTime
                        )
                        _sessionState.value = updatedState
                        updateServiceState(updatedState)
                        updateNotification(updatedState)
                        // Usamos la función centralizada para sincronizar UI y Notificación
                    }
                } else {
                    // Durante la espera entre estados, el tiempo general SIGUE avanzando
                    val updatedState = currentState.copy(
                        generalElapsedTimeSec = newGeneralTime
                    )
                    _sessionState.value = updatedState
                    updateServiceState(updatedState)
                    updateNotification(updatedState)
                }
            }
        }
    }

    /**
     * Al llegar a 00:00 en una fase:
     * - Se prepara la siguiente fase con su tiempo correspondiente.
     * - SE ACTIVA `isWaitingManualStart = true` para congelar el tiempo de fase.
     * - `isPaused` se mantiene en `false` para que el tiempo general continúe.
     */
    private fun handlePhaseCompletion(state: SkateSessionStateModel, currentGeneralTime: Long) {
        val (nextPhase, nextRound, duration) = calculateNextPhaseAndRound(state)

        if (nextPhase == SkateSessionPhase.COMPLETED) {
            finishSession()
            return
        }

        // Si la siguiente fase no tiene duración (0 seg), saltar a la siguiente
        if (duration <= 0) {
            val intermediateState = state.copy(currentPhase = nextPhase, currentRound = nextRound)
            handlePhaseCompletion(intermediateState, currentGeneralTime)
            return
        }

        val waitingState = state.copy(
            isPaused = false,               // El tiempo general sigue su marcha
            isWaitingManualStart = true,    // Congela solo el tiempo de la nueva fase
            currentPhase = nextPhase,
            currentRound = nextRound,
            generalElapsedTimeSec = currentGeneralTime,
            phaseTimeRemainingSec = duration,
            phaseTotalDurationSec = duration
        )

        _sessionState.value = waitingState
        // 3. ACTIVAR VIBRACIÓN CONTINUA al terminar la fase
        continuousVibrator.startContinuousVibration()
        //triggerVibrationAlert()
        updateNotification(waitingState)
    }

    private fun calculateNextPhaseAndRound(state: SkateSessionStateModel): Triple<SkateSessionPhase, Int, Long> {
        return when (state.currentPhase) {
            SkateSessionPhase.WARMUP -> {
                Triple(SkateSessionPhase.SKATE, 1, skatePerRoundDurationSec)
            }

            SkateSessionPhase.SKATE -> {
                if (restPerRoundDurationSec > 0) {
                    Triple(SkateSessionPhase.REST, state.currentRound, restPerRoundDurationSec)
                } else if (state.currentRound < state.totalRounds) {
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
                if (state.currentRound < state.totalRounds) {
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
        }
    }

    /**
     * Inicia el temporizador de la fase actual al presionar el botón
     */
    private fun startNextPhaseManually() {
        val currentState = _sessionState.value
        if (currentState.isRunning && currentState.isWaitingManualStart) {
            // Detener la vibración continua
            continuousVibrator.stopVibration()

            val runningState = currentState.copy(
                isSessionStarted = true,      // Arranca formalmente la sesión por primera vez
                isWaitingManualStart = false  // Descongela el conteo de la fase
            )
            _sessionState.value = runningState
            updateNotification(runningState)
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
        if (currentState.isWaitingManualStart) {
            startNextPhaseManually()
            return
        }
        val updatedState = currentState.copy(isPaused = !currentState.isPaused)
        _sessionState.value = updatedState
        updateNotification(updatedState)
    }

    private fun triggerVibrationAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 200, 500),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun finishSession() {
        val finalState = _sessionState.value.copy(
            isRunning = false,
            isPaused = false,
            isWaitingManualStart = false,
            currentPhase = SkateSessionPhase.COMPLETED
        )
        _sessionState.value = finalState

        currentRoomSession?.let { session ->
            serviceScope.launch(Dispatchers.IO) {
                val sessionUpdate = session.copy(
                    status = "COMPLETED",
                    closeDate = System.currentTimeMillis()
                )
                application.repoRoomInsertSession(sessionUpdate)
            }
        }

        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopSession() {
        finishSession()
    }

    // --- NOTIFICACIÓN ---

    private fun updateNotification(state: SkateSessionStateModel) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: SkateSessionStateModel): Notification {
        val generalFormatted = formatTime(state.generalElapsedTimeSec)
        val phaseFormatted = formatTime(state.phaseTimeRemainingSec)

        val title =
            if (state.currentPhase == SkateSessionPhase.SKATE || state.currentPhase == SkateSessionPhase.REST) {
                "${state.currentPhase.displayName} - Ronda ${state.currentRound}/${state.totalRounds}"
            } else {
                state.currentPhase.displayName
            }

        val content = when {
            !state.isSessionStarted -> "¡Sesión lista! Toca para INICIAR ($phaseFormatted)"
            state.isWaitingManualStart -> "¡Siguiente ronda lista! Toca para INICIAR ($phaseFormatted)"
            else -> "Fase: $phaseFormatted | General: $generalFormatted"
        }

        // PendingIntent para abrir la Activity cuando toque o cuando se active en pantalla completa
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            200,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. Intent cuando el usuario TOCA EL CUERPO de la notificación
        val mainTapIntent = Intent(this, SkateSessionService::class.java).apply {
            action =
                if (state.isWaitingManualStart) ACTION_START_NEXT_PHASE else ACTION_PAUSE_TOGGLE
        }

        val mainTapPendingIntent = PendingIntent.getService(
            this,
            100,
            mainTapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_media_play)
            .setContentIntent(mainTapPendingIntent) // <-- Vincula el toque del cuerpo de la notificación
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)               // Máxima prioridad
            .setCategory(NotificationCompat.CATEGORY_ALARM)            // Alarma fuerza el encendido de pantalla
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Si la sesión está esperando acción manual (ej. término de ronda), activa la pantalla completa
        if (state.isWaitingManualStart) {
            builder.setFullScreenIntent(
                fullScreenPendingIntent,
                true
            ) // Forzar que la pantalla se encienda y muestre la alerta
        }

        // 2. Agregar BOTÓN DE ACCIÓN rápido en la barra de notificación
        if (state.isWaitingManualStart) {
            val startPhaseIntent = Intent(this, SkateSessionService::class.java).apply {
                action = ACTION_START_NEXT_PHASE
            }
            val startPhasePendingIntent = PendingIntent.getService(
                this,
                101,
                startPhaseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                R.drawable.ic_media_play,
                "▶ INICIAR RONDA",
                startPhasePendingIntent
            )
        } else {
            // Botón Pausar / Reanudar si ya está corriendo
            val pauseIntent = Intent(this, SkateSessionService::class.java).apply {
                action = ACTION_PAUSE_TOGGLE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                102,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val buttonText = if (state.isPaused) "▶ REANUDAR" else "⏸ PAUSAR"
            builder.addAction(
                if (state.isPaused) R.drawable.ic_media_play else R.drawable.ic_media_pause,
                buttonText,
                pausePendingIntent
            )
        }

        // Botón para detener/cancelar la sesión
        val stopIntent = Intent(this, SkateSessionService::class.java).apply {
            action = ACTION_STOP_SESSION
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            103,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_menu_close_clear_cancel,
            "TERMINAR",
            stopPendingIntent
        )

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cronómetro de Sesión de Skate",
                NotificationManager.IMPORTANCE_HIGH // Subir la importancia a HIGH para permitir alertas táctiles
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
