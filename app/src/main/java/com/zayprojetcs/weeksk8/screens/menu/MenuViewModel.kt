package com.zayprojetcs.weeksk8.screens.menu


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomCountHistorySessionSkate
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetStartSessionFlow
import com.zayprojetcs.weeksk8.screens.menu.ui_state.model.MenuUiStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    val dataStoreAppManager by lazy { DataStoreAppManager(application) }


    private val _menuUiState = MutableStateFlow(MenuUiStateModel())
    val menuUiState: StateFlow<MenuUiStateModel> = combine(
        dataStoreAppManager.deviceConnectBluetooth,
        application.repoRoomCountHistorySessionSkate(),
        application.repoRoomGetStartSessionFlow(),
        _menuUiState
    ) { connectedDevice, historyCount, openOrCreatedSession, mapUiState ->
        mapUiState.copy(
            connectedDevice = connectedDevice,
            historyCountSession = historyCount,
            sessionWithRoundsTrick = openOrCreatedSession,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MenuUiStateModel(isLoading = true)
    )



}