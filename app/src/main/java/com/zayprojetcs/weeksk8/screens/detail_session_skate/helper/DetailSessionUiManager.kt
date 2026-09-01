package com.zayprojetcs.weeksk8.screens.detail_session_skate.helper

import com.zayprojetcs.weeksk8.core.services.session_skate.model.SkateSessionStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DetailSessionUiManager {
    private val _sessionState = MutableStateFlow(SkateSessionStateModel())
    val sessionState: StateFlow<SkateSessionStateModel> = _sessionState.asStateFlow()

    fun updateState(newState: SkateSessionStateModel) {
        _sessionState.value = newState
    }

    fun resetState() {
        _sessionState.value = SkateSessionStateModel()
    }
}