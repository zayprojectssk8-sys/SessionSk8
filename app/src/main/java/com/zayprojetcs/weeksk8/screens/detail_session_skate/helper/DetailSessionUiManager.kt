package com.zayprojetcs.weeksk8.screens.detail_session_skate.helper

import com.zaysk8.core.model.SkateSessionStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object DetailSessionUiManager {

    private val _sessionState = MutableStateFlow(SkateSessionStateModel())
    val sessionState: StateFlow<SkateSessionStateModel> = _sessionState.asStateFlow()

    /**
     * Reemplaza el estado completo de la sesión.
     */
    fun updateState(newState: SkateSessionStateModel) {
        _sessionState.value = newState
    }

    /**
     * Permite transformaciones atómicas e hilos seguros sobre el estado actual.
     */
    fun update(transform: (SkateSessionStateModel) -> SkateSessionStateModel) {
        _sessionState.update(transform)
    }

    /**
     * Reinicia el estado de la interfaz al valor inicial por defecto.
     */
    fun resetState() {
        _sessionState.value = SkateSessionStateModel()
    }
}