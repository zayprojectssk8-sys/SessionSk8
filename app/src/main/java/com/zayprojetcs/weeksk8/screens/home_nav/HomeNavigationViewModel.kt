package com.zayprojetcs.weeksk8.screens.home_nav

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetStartDayFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeNavigationViewModel(application: Application) : AndroidViewModel(application) {

    val startDay = application.repoRoomGetStartDayFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

}