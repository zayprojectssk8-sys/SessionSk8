package com.zayprojetcs.weeksk8.screens.create_session_skate

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.CreateSessionSkateConfigRoundScreen
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.CreateSessionSkateConfigScreen
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.CreateSessionTrickDistributionScreen
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.PreSessionSummaryScreen
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.TrickSelectedSessionScreen
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnCooldownEnabledChanged
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnCooldownMinutesChanged
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnDurationSelected
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnSetErrorDialog
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnSetRoundPreset
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnSetTrickSelectedList
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnTrickDistributionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnTrickModeSelected
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnTrickOrderMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnTrickSelected
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnValidateNavigationBackPress
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnValidateNavigationConfig
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnWarmupEnabledChanged
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState.OnWarmupMinutesChanged
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TypeConfig
import com.zayprojetcs.weeksk8.ui.customs.AlertInfoCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustomCreateSession
import com.zayprojetcs.weeksk8.ui.customs.view.SelectTrickScreen
import com.zaysk8.core.model.TypeStanceTrick


@Composable
fun CreateSessionSkateScreen(
    onNavigateBackPress: () -> Unit,
    viewModel: CreateSessionSkateViewModel = viewModel()
) {
    val createSessionSkateUiStateModel by viewModel.createSessionSkateUiStateModel.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        if (createSessionSkateUiStateModel.typeConfig == TypeConfig.CONFIG_SESSION) {
            onNavigateBackPress()
        } else {
            viewModel.loadEvent(OnValidateNavigationBackPress)
        }
    }


    LoadAlertError(
        createSessionSkateUiStateModel = createSessionSkateUiStateModel,
        sendEvent = { viewModel.loadEvent(it) })


    ScaffoldCustomCreateSession(
        title = "CREAR SESIÓN SKATE",
        hideTitle = createSessionSkateUiStateModel.showSelectTrick,
        hideButton = createSessionSkateUiStateModel.showSelectTrick,
        onButtonClick = {
            viewModel.loadEvent(OnValidateNavigationConfig)
        },
        content = {

            if (createSessionSkateUiStateModel.showSelectTrick) {

                SelectTrickScreen(
                    trickListSelected = createSessionSkateUiStateModel.selectedTrickList,
                    limitSelected = createSessionSkateUiStateModel.countLimitSelectTrick,
                    availableStances = TypeStanceTrick.entries,
                    onSendSelection = {
                        viewModel.loadEvent(OnSetTrickSelectedList(it))
                        viewModel.loadEvent(OnTrickSelected(false))
                    }
                )
                return@ScaffoldCustomCreateSession
            }

            when (createSessionSkateUiStateModel.typeConfig) {
                TypeConfig.CONFIG_SESSION -> CreateSessionSkateConfigScreen(
                    createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                    onDurationSelected = {
                        viewModel.loadEvent(OnDurationSelected(it))
                    },
                    onWarmupEnabledChanged = {
                        viewModel.loadEvent(OnWarmupEnabledChanged(it))
                    },
                    onWarmupMinutesChanged = {
                        viewModel.loadEvent(OnWarmupMinutesChanged(it))
                    },
                    onCooldownEnabledChanged = {
                        viewModel.loadEvent(OnCooldownEnabledChanged(it))
                    },
                    onCooldownMinutesChanged = {
                        viewModel.loadEvent(OnCooldownMinutesChanged(it))
                    }
                )

                TypeConfig.CONFIG_ROUNDS -> CreateSessionSkateConfigRoundScreen(
                    createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                    onPresetSelected = {
                        viewModel.loadEvent(OnSetRoundPreset(it))
                    }
                )

                TypeConfig.CONFIG_PROCESS_TRICK -> TrickSelectedSessionScreen(
                    createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                    onTrickModeSelected = {
                        viewModel.loadEvent(OnTrickModeSelected(it))
                    },
                    onOpenTrickPickerClick = {
                        viewModel.loadEvent(OnTrickSelected(true))
                    },
                    onModeSelected = {
                        viewModel.loadEvent(CreateSessionSkateUiState.OnSetTrickTrackingMode(it))
                    }
                )

                TypeConfig.CONFIG_DISTRIBUTION_TRICK -> CreateSessionTrickDistributionScreen(
                    uiStateModel = createSessionSkateUiStateModel,
                    onDistributionModeSelected = {
                        viewModel.loadEvent(OnTrickDistributionMode(it))
                    },
                    onOrderModeSelected = {
                        viewModel.loadEvent(OnTrickOrderMode(it))
                    }
                )


                TypeConfig.SUMMARY_SESSION -> PreSessionSummaryScreen(
                    createSessionSkateUiStateModel = createSessionSkateUiStateModel
                )
            }
        }
    )

}

@Composable
fun LoadAlertError(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    sendEvent: (CreateSessionSkateUiState) -> Unit
) {
    if (createSessionSkateUiStateModel.titleErrorDialog != null && createSessionSkateUiStateModel.descErrorDialog != null) {
        AlertInfoCustom(
            createSessionSkateUiStateModel.titleErrorDialog,
            createSessionSkateUiStateModel.descErrorDialog,
            onDismiss = {
                sendEvent(
                    OnSetErrorDialog(
                        title = null,
                        description = null,
                    )
                )
            }
        )
    }
}


