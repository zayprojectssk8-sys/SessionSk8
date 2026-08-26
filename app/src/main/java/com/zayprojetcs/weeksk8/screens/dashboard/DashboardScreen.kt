package com.zayprojetcs.weeksk8.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zayprojetcs.weeksk8.screens.progress_user.ProgressUserScreen
import com.zayprojetcs.weeksk8.screens.tricks.TricksScreen
import com.zayprojetcs.weeksk8.ui.customs.AppDestinations
import com.zayprojetcs.weeksk8.ui.customs.NavigationSuiteCustom
import com.zayprojetcs.weeksk8.ui.customs.TextBodyLarge


@Composable
fun DashboardScreen(
    onCreateDaySkate: () -> Unit
) {
    NavigationSuiteCustom { currentDestination ->

        when (currentDestination) {
            AppDestinations.PROGRESS -> ProgressUserScreen(onCreateDaySkate = onCreateDaySkate)
            AppDestinations.TRICKS -> TricksScreen()
        }
    }
}
