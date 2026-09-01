package com.sk8.appwatch.presentation.screen.home_watch

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sk8.appwatch.presentation.core.data_store.DataStoreWatchManager
import com.sk8.appwatch.presentation.core.helper.NodeClientWatchHelper
import com.sk8.appwatch.presentation.screen.home_watch.ui_state.model.WearAppNavigateUiStateModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class WearAppViewModel(application: Application) : AndroidViewModel(application) {

    val dataStoreWatchManager by lazy { DataStoreWatchManager(application) }
    val nodeClientWatchHelper by lazy { NodeClientWatchHelper(application) }

    private val _linkDeviceSmartWatchUiState = MutableStateFlow(WearAppNavigateUiStateModel())
    val linkDeviceSmartWatchUiState: StateFlow<WearAppNavigateUiStateModel> =
        _linkDeviceSmartWatchUiState.asStateFlow()


    fun validateDevicePhoneConnected() = viewModelScope.launch {
        delay(3.milliseconds)
        val nodeId = dataStoreWatchManager.deviceConnectNodeId.firstOrNull()
        Log.wtf(javaClass.simpleName, " validateDevicePhoneConnected $nodeId")
        if (nodeId == null) {
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(isLoader = false)
            }
            return@launch
        }
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(connectedNodeId = nodeId)
        }

        val isAppInstalledOnPhone = nodeClientWatchHelper.verifyInstallAppInPhone(nodeId)

        Log.wtf(
            "javaClass.simpleName",
            " WearAppNavigation isAppInstalledOnPhone: $isAppInstalledOnPhone"
        )

        if (!isAppInstalledOnPhone) {
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(connectedNodeId = nodeId, isLoader = false)
            }
        } else {
            nodeClientWatchHelper.verifyConnectionWithPhone(nodeId)
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(connectedNodeId = nodeId, isLoader = false)
            }
        }

    }


}