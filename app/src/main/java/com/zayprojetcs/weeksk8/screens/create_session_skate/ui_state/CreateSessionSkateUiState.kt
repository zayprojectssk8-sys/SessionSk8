package com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state

import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.RoundPreset
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickDistributionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickOrderMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickSelectionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickTrackingMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TypeConfig
import com.zaysk8.core.model.TFMTrick

sealed interface CreateSessionSkateUiState {

    data class OnDurationSelected(val minutes: Int?) : CreateSessionSkateUiState
    data class OnTrickModeSelected(val trickSelectionMode: TrickSelectionMode) :
        CreateSessionSkateUiState

    data class OnTrickSelected(val showSelectTrick: Boolean) : CreateSessionSkateUiState
    data class OnSetTypeConfig(val typeConfig: TypeConfig) : CreateSessionSkateUiState
    data class OnSetErrorDialog(val title: String?, val description: String?) : CreateSessionSkateUiState
    data object OnValidateNavigationConfig : CreateSessionSkateUiState
    data object OnFinishCreateSession : CreateSessionSkateUiState
    data object OnValidateNavigationBackPress : CreateSessionSkateUiState
    data class OnSetRoundPreset(val eventRoundPreset: RoundPreset) : CreateSessionSkateUiState
    data class OnTrickDistributionMode(val trickDistributionMode: TrickDistributionMode) : CreateSessionSkateUiState
    data class OnTrickOrderMode(val trickOrderMode: TrickOrderMode) : CreateSessionSkateUiState
    data class OnSetTrickSelectedList(val trickList: List<TFMTrick>) : CreateSessionSkateUiState
    data class OnSetTrickTrackingMode(val trickTrackingMode: TrickTrackingMode) : CreateSessionSkateUiState

    data class OnWarmupEnabledChanged(val warmupEnabled: Boolean) : CreateSessionSkateUiState
    data class OnWarmupMinutesChanged(val warmupMinutes: Int) : CreateSessionSkateUiState
    data class OnCooldownEnabledChanged(val cooldownEnabled: Boolean) : CreateSessionSkateUiState
    data class OnCooldownMinutesChanged(val cooldownMinutes: Int) : CreateSessionSkateUiState
}
