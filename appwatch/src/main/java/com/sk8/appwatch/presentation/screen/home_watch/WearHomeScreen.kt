package com.sk8.appwatch.presentation.screen.home_watch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sk8.appwatch.presentation.screen.home_watch.routes.WearHomeNavigationRoutes
import com.sk8.appwatch.presentation.screen.link_device_phone.LinkDevicePhoneScreen
import com.sk8.appwatch.presentation.screen.session_skate.WearSessionScreen

@Composable
fun WearAppNavigation(
    viewModel: WearAppViewModel = viewModel(),
    navController: NavHostController = rememberSwipeDismissableNavController()
) {

    val linkDeviceSmartWatchUiState by viewModel.linkDeviceSmartWatchUiState.collectAsStateWithLifecycle()

    if (linkDeviceSmartWatchUiState.isLoader) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                indicatorColor = MaterialTheme.colors.primary
            )
        }
        return
    }


    val wearDestinationStart = if (linkDeviceSmartWatchUiState.isPermissionsGranted) {
        WearHomeNavigationRoutes.SESSION_METRICS
    } else {
        WearHomeNavigationRoutes.PERMISSIONS_WATCH
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = wearDestinationStart
    ) {
        composable(WearHomeNavigationRoutes.PERMISSIONS_WATCH) {
            LinkDevicePhoneScreen(onSuccessPermission = {
                navController.navigate(WearHomeNavigationRoutes.SESSION_METRICS)
            })
        }

        // Pantalla 2: Métrica en Vivo (La pantalla que creamos previamente)
        composable(WearHomeNavigationRoutes.SESSION_METRICS) {
            WearSessionScreen()
        }
    }
}