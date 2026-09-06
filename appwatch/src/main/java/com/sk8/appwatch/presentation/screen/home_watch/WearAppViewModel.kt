package com.sk8.appwatch.presentation.screen.home_watch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.sk8.appwatch.presentation.core.data_store.DataStoreWatchManager
import com.sk8.appwatch.presentation.core.helper.NodeClientWatchHelper
import com.sk8.appwatch.presentation.screen.home_watch.ui_state.model.WearAppNavigateUiStateModel
import com.sk8.appwatch.presentation.utils.getRequiredWearOsPermissionsGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class WearAppViewModel(application: Application) : AndroidViewModel(application) {

    val dataStoreWatchManager by lazy { DataStoreWatchManager(application) }
    val nodeClientWatchHelper by lazy { NodeClientWatchHelper(application) }

    private val _linkDeviceSmartWatchUiState = MutableStateFlow(WearAppNavigateUiStateModel())
    val linkDeviceSmartWatchUiState: StateFlow<WearAppNavigateUiStateModel> =
        _linkDeviceSmartWatchUiState.asStateFlow()


    init {
        validatePermissionsGrated()
    }

    fun validatePermissionsGrated() = viewModelScope.launch {
        delay(3.milliseconds)

        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                isPermissionsGranted = application.getRequiredWearOsPermissionsGranted(),
                isLoader = false
            )
        }
    }


}