package com.zayprojetcs.weeksk8.screens.detail_session_skate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetStartSessionFlow
import com.zayprojetcs.weeksk8.core.services.session_skate.model.SkateSessionPhase
import com.zayprojetcs.weeksk8.core.services.session_skate.model.SkateSessionStateModel
import com.zayprojetcs.weeksk8.screens.detail_session_skate.helper.DetailSessionUiManager
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.ActiveSessionUiState
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PropertyStatus
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.SessionDetailUiState
import com.zayprojetcs.weeksk8.utils.formatSecondsToHHMMSS
import com.zayprojetcs.weeksk8.utils.formatSecondsToMMSS
import com.zaysk8.core.model.SessionPhase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SessionDetailViewModel(application: Application) : AndroidViewModel(application) {


    val uiState: StateFlow<SessionDetailUiState> = combine(
        application.repoRoomGetStartSessionFlow(),
        DetailSessionUiManager.sessionState
    ) { roomSession, serviceState ->
        mapToUiState(roomSession?.roomSession, serviceState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionDetailUiState(isLoading = true)
    )


    private fun mapToUiState(
        session: RoomSession?,
        serviceState: SkateSessionStateModel
    ): SessionDetailUiState {
        if (session == null) return SessionDetailUiState(isLoading = true)

        val currentPhase = mapDomainPhaseToUiPhase(serviceState.currentPhase)

        val warmupStatus = when {
            session.warmupMinutes == 0 -> PropertyStatus.DISABLED
            currentPhase == SessionPhase.WARMUP -> PropertyStatus.IN_PROGRESS
            serviceState.isSessionStarted && currentPhase != SessionPhase.WARMUP -> PropertyStatus.COMPLETED
            else -> PropertyStatus.PENDING
        }

        val skateRoundsStatus = when (currentPhase) {
            SessionPhase.SKATE_RUNNING, SessionPhase.REST_RUNNING -> PropertyStatus.IN_PROGRESS
            SessionPhase.COOL_DOWN, SessionPhase.EXTRA_TIME_RUNNING, SessionPhase.FINISHED -> PropertyStatus.COMPLETED
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

