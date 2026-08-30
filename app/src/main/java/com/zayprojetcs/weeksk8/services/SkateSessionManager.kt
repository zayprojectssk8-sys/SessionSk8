package com.zayprojetcs.weeksk8.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SkateSessionManager {
    private val _sessionState = MutableStateFlow(SkateSessionState())
    val sessionState: StateFlow<SkateSessionState> = _sessionState.asStateFlow()

    fun updateState(newState: SkateSessionState) {
        _sessionState.value = newState
    }

    fun resetState() {
        _sessionState.value = SkateSessionState()
    }
}