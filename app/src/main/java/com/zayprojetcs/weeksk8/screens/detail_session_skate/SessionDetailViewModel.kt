package com.zayprojetcs.weeksk8.screens.detail_session_skate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetStartSessionFlow
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.ActiveSessionUiState
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PropertyStatus
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.SessionDetailUiState
import com.zayprojetcs.weeksk8.services.SkateSessionManager
import com.zayprojetcs.weeksk8.services.SkateSessionPhase
import com.zayprojetcs.weeksk8.services.SkateSessionState
import com.zaysk8.core.model.SessionPhase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class SessionDetailViewModel(application: Application) : AndroidViewModel(application) {


    val uiState: StateFlow<SessionDetailUiState> = combine(
        application.repoRoomGetStartSessionFlow(),
        SkateSessionManager.sessionState
    ) { roomSession, serviceState ->
        mapToUiState(roomSession?.roomSession, serviceState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionDetailUiState(isLoading = true)
    )


    private fun mapToUiState(
        session: RoomSession?,
        serviceState: SkateSessionState
    ): SessionDetailUiState {
        if (session == null) return SessionDetailUiState(isLoading = true)

        val currentPhase = mapDomainPhaseToUiPhase(serviceState.currentPhase)

        val warmupStatus = when {
            session.warmupMinutes == 0 -> PropertyStatus.DISABLED
            currentPhase == SessionPhase.WARMUP -> PropertyStatus.IN_PROGRESS
            serviceState.isSessionStarted && currentPhase != SessionPhase.WARMUP -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        val skateRoundsStatus = when {
            currentPhase == SessionPhase.SKATE_RUNNING || currentPhase == SessionPhase.REST_RUNNING -> PropertyStatus.IN_PROGRESS
            currentPhase == SessionPhase.COOL_DOWN || currentPhase == SessionPhase.EXTRA_TIME_RUNNING || currentPhase == SessionPhase.FINISHED -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        val cooldownStatus = when {
            session.cooldownMinutes == 0 -> PropertyStatus.DISABLED
            currentPhase == SessionPhase.COOL_DOWN -> PropertyStatus.IN_PROGRESS
            currentPhase == SessionPhase.FINISHED -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        return SessionDetailUiState(
            session = session,
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
            isLoading = false
        )
    }

    private fun mapDomainPhaseToUiPhase(servicePhase: SkateSessionPhase): SessionPhase {
        return when (servicePhase) {
            SkateSessionPhase.WARMUP -> SessionPhase.WARMUP
            SkateSessionPhase.SKATE -> SessionPhase.SKATE_RUNNING
            SkateSessionPhase.REST -> SessionPhase.REST_RUNNING
            SkateSessionPhase.EXTRA_TIME -> SessionPhase.EXTRA_TIME_RUNNING
            SkateSessionPhase.STRETCHING -> SessionPhase.COOL_DOWN
            SkateSessionPhase.COMPLETED -> SessionPhase.FINISHED
        }
    }
}

fun Long.toFormattedTimer(): String {
    val totalSeconds = this / 1000
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Convierte segundos totales a formato "MM:SS" (Ej: 05:30, 12:05)
 */
fun formatSecondsToMMSS(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

/**
 * Convierte segundos totales a formato "HH:MM:SS" para el temporizador general (Ej: 01:15:30)
 */
fun formatSecondsToHHMMSS(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
}