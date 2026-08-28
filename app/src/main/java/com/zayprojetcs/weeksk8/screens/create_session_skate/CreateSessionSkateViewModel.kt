package com.zayprojetcs.weeksk8.screens.create_session_skate

import android.app.Application
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager.Companion.dataStoreAppManager
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickFlow
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickSelectionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TypeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CreateSessionSkateViewModel(application: Application) : AndroidViewModel(application) {


    private val _createSessionSkateUiStateModel = MutableStateFlow(CreateSessionSkateUiStateModel())


    val createSessionSkateUiStateModel: StateFlow<CreateSessionSkateUiStateModel> = combine(
        application.repoRoomGetListTrickFlow(),
        application.dataStoreAppManager().deviceConnectBluetooth,
        _createSessionSkateUiStateModel
    ) { tricksUnlock, deviceWatch, createSessionSkateUiStateModel ->

        createSessionSkateUiStateModel.copy(
            detectedWearable = deviceWatch,
            unlockTrickList = tricksUnlock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateSessionSkateUiStateModel()
    )


    fun loadEvent(event: CreateSessionSkateUiState) {
        when (event) {
            is CreateSessionSkateUiState.OnDurationSelected -> event.setDurationSelected()
            is CreateSessionSkateUiState.OnTrickModeSelected -> event.setTrickModeSelected()
            is CreateSessionSkateUiState.OnTrickSelected -> event.setTrickSelected()
            is CreateSessionSkateUiState.OnSetTrickSelectedList -> event.setTrickSelectedList()
            is CreateSessionSkateUiState.OnSetTypeConfig -> event.setTypeConfig()
            is CreateSessionSkateUiState.OnSetRoundPreset -> event.setRoundPreset()
            is CreateSessionSkateUiState.OnSetTrickTrackingMode -> event.setTrickTrackingMode()
            is CreateSessionSkateUiState.OnCooldownEnabledChanged -> event.setCooldownEnabledChanged()
            is CreateSessionSkateUiState.OnCooldownMinutesChanged -> event.setCooldownMinutesChanged()
            is CreateSessionSkateUiState.OnWarmupEnabledChanged -> event.setWarmupEnabledChanged()
            is CreateSessionSkateUiState.OnWarmupMinutesChanged -> event.setWarmupMinutesChanged()
            is CreateSessionSkateUiState.OnSetErrorDialog -> event.setErrorDialog()
            CreateSessionSkateUiState.OnValidateNavigationConfig -> validateNavigationConfig()
            CreateSessionSkateUiState.OnValidateNavigationBackPress -> validateNavigationBackPress()
            is CreateSessionSkateUiState.OnTrickDistributionMode -> event.setTrickDistributionMode()
            is CreateSessionSkateUiState.OnTrickOrderMode -> event.setTrickOrderMode()
        }
    }

    private fun CreateSessionSkateUiState.OnTrickOrderMode.setTrickOrderMode() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            trickOrderMode = trickOrderMode
        )
    }

    private fun CreateSessionSkateUiState.OnTrickDistributionMode.setTrickDistributionMode() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            trickDistributionMode = trickDistributionMode
        )
    }

    fun validateNavigationBackPress() {
        createSessionSkateUiStateModel.value.apply {
            val navigate = when (typeConfig) {

                TypeConfig.CONFIG_ROUNDS -> TypeConfig.CONFIG_SESSION
                TypeConfig.CONFIG_PROCESS_TRICK -> {
                    if (showSelectTrick) {
                        loadEvent(CreateSessionSkateUiState.OnTrickSelected(false))
                        typeConfig
                    } else TypeConfig.CONFIG_ROUNDS
                }

                TypeConfig.CONFIG_DISTRIBUTION_TRICK -> TypeConfig.CONFIG_PROCESS_TRICK
                TypeConfig.SUMMARY_SESSION -> {
                    if (trickModeSession == TrickSelectionMode.CUSTOM || trickModeSession == TrickSelectionMode.RANDOM_UNLOCKED) TypeConfig.CONFIG_DISTRIBUTION_TRICK
                    else TypeConfig.CONFIG_PROCESS_TRICK
                }

                else -> typeConfig
            }

            loadEvent(CreateSessionSkateUiState.OnSetTypeConfig(navigate))
        }
    }

    fun validateNavigationConfig() {
        createSessionSkateUiStateModel.value.apply {

            val event = when (typeConfig) {
                TypeConfig.CONFIG_SESSION -> {
                    when {
                        warmupEnabled && warmupMinutes == 0 -> {
                            loadEvent(
                                CreateSessionSkateUiState.OnSetErrorDialog(
                                    title = "Calentamiento",
                                    description = "No seleccionaste tiempo de calentamiento.",
                                )
                            )
                            typeConfig
                        }

                        cooldownEnabled && cooldownMinutes == 0 -> {
                            loadEvent(
                                CreateSessionSkateUiState.OnSetErrorDialog(
                                    title = "Estiramiento",
                                    description = "No seleccionaste tiempo de estiramiento final.",
                                )
                            )
                            typeConfig
                        }

                        else -> {
                            loadEvent(CreateSessionSkateUiState.OnSetRoundPreset(this.roundPreset))
                            TypeConfig.CONFIG_ROUNDS
                        }
                    }
                }

                TypeConfig.CONFIG_ROUNDS -> TypeConfig.CONFIG_PROCESS_TRICK

                TypeConfig.CONFIG_PROCESS_TRICK -> {

                    if (trickModeSession == TrickSelectionMode.CUSTOM) {

                        if (selectedTrickList.isNotEmpty()) TypeConfig.CONFIG_DISTRIBUTION_TRICK
                        else {
                            loadEvent(
                                CreateSessionSkateUiState.OnSetErrorDialog(
                                    title = "Selecciona al menos un truco",
                                    description = "Agrega los trucos que vas a practicar en esta sesión para poder iniciar el seguimiento.",
                                )
                            )
                            typeConfig
                        }

                    } else if (trickModeSession == TrickSelectionMode.RANDOM_UNLOCKED) {
                        if (unlockTrickList.isNotEmpty()) TypeConfig.CONFIG_DISTRIBUTION_TRICK
                        else {
                            loadEvent(
                                CreateSessionSkateUiState.OnSetErrorDialog(
                                    title = "Sin trucos desbloqueados",
                                    description = "Aún no has desbloqueado ningún truco en tu catálogo. Necesitas desbloquear al menos uno para poder agregarlo a tu sesión.",
                                )
                            )
                            typeConfig
                        }
                    } else {
                        if (selectedTrickList.isNotEmpty()) {
                            loadEvent(CreateSessionSkateUiState.OnSetTrickSelectedList(arrayListOf()))
                        }
                        TypeConfig.SUMMARY_SESSION
                    }
                }

                TypeConfig.CONFIG_DISTRIBUTION_TRICK -> TypeConfig.SUMMARY_SESSION

                TypeConfig.SUMMARY_SESSION -> TypeConfig.SUMMARY_SESSION
            }

            loadEvent(CreateSessionSkateUiState.OnSetTypeConfig(event))
        }
    }

    private fun CreateSessionSkateUiState.OnSetErrorDialog.setErrorDialog() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            titleErrorDialog = title,
            descErrorDialog = description
        )
    }

    private fun CreateSessionSkateUiState.OnCooldownEnabledChanged.setCooldownEnabledChanged() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            cooldownEnabled = cooldownEnabled
        )
    }

    private fun CreateSessionSkateUiState.OnCooldownMinutesChanged.setCooldownMinutesChanged() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            cooldownMinutes = cooldownMinutes
        )
    }

    private fun CreateSessionSkateUiState.OnWarmupEnabledChanged.setWarmupEnabledChanged() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            warmupEnabled = warmupEnabled
        )
    }

    private fun CreateSessionSkateUiState.OnWarmupMinutesChanged.setWarmupMinutesChanged() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            warmupMinutes = warmupMinutes
        )
    }

    private fun CreateSessionSkateUiState.OnSetTrickTrackingMode.setTrickTrackingMode() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            trickTrackingMode = trickTrackingMode
        )
    }

    private fun CreateSessionSkateUiState.OnSetRoundPreset.setRoundPreset() {
        createSessionSkateUiStateModel.value.apply {
            val roundDuration = eventRoundPreset.totalRoundMinutes
            val calculatedRounds = durationSessionMinutes?.let { it / roundDuration }
            val countLimitSelectTrick =
                if (durationSessionMinutes == null) 10 else calculatedRounds ?: 10
            val totalSkateTime =
                calculatedRounds?.let { it * eventRoundPreset.skateMinutes }
            val totalRestTime =
                calculatedRounds?.let { it * eventRoundPreset.restMinutes }
            val marginMinutes =
                durationSessionMinutes?.let { it - (calculatedRounds!! * roundDuration) }

            _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
                roundPreset = eventRoundPreset,
                calculatedRounds = calculatedRounds ?: 0,
                totalSkateTime = totalSkateTime ?: 0,
                countLimitSelectTrick = countLimitSelectTrick,
                totalRestTime = totalRestTime ?: 0,
                marginMinutes = marginMinutes,
            )
        }
    }

    private fun CreateSessionSkateUiState.OnSetTypeConfig.setTypeConfig() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            typeConfig = typeConfig
        )
    }

    private fun CreateSessionSkateUiState.OnSetTrickSelectedList.setTrickSelectedList() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            selectedTrickList = trickList,
            selectedCustomTricksCount = trickList.size
        )
    }

    private fun CreateSessionSkateUiState.OnTrickSelected.setTrickSelected() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            showSelectTrick = showSelectTrick
        )
    }

    fun CreateSessionSkateUiState.OnDurationSelected.setDurationSelected() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            durationSessionMinutes = minutes
        )
    }

    fun CreateSessionSkateUiState.OnTrickModeSelected.setTrickModeSelected() {
        _createSessionSkateUiStateModel.value = _createSessionSkateUiStateModel.value.copy(
            trickModeSession = trickSelectionMode
        )
    }
}
