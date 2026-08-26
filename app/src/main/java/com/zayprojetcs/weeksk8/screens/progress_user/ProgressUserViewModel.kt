package com.zayprojetcs.weeksk8.screens.progress_user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetDaysSkateFlow
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ProgressUserViewModel(application: Application) : AndroidViewModel(application) {

    val daysSkate = application.repoRoomGetDaysSkateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), arrayListOf())

    val tricksList = application.repoRoomGetListTrickFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), arrayListOf())
}