package com.zayprojetcs.weeksk8.screens.menu


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager.Companion.dataStoreAppManager
import com.zayprojetcs.weeksk8.screens.menu.ui_state.MenuUiState
import com.zayprojetcs.weeksk8.screens.menu.ui_state.model.MenuUiStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    val dataStore by lazy { application.dataStoreAppManager() }

    private val _menuUiState = MutableStateFlow(MenuUiStateModel())
    val menuUiState: StateFlow<MenuUiStateModel> = combine(
        dataStore.deviceConnectBluetooth,
        _menuUiState
    ) { connectedDevice, mapUiState ->
        mapUiState.copy(
            connectedDevice = connectedDevice,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MenuUiStateModel(isLoading = true)
    )

    fun loadEvent(event: MenuUiState) {

    }


}