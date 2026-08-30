package com.sk8.appwatch.presentation.screen.home_watch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sk8.appwatch.presentation.screen.app_empty_communication.AppEmptyCommunicationScreen
import com.sk8.appwatch.presentation.screen.home_watch.routes.WearHomeNavigationRoutes
import com.sk8.appwatch.presentation.screen.link_device_phone.LinkDevicePhoneScreen
import com.sk8.appwatch.presentation.screen.skate_session.SkateSessionScreen

@Composable
fun WearAppNavigation(
    claveAppCommunication: String? = null,
    viewModel: WearAppViewModel = viewModel(),
    navController: NavHostController = rememberSwipeDismissableNavController()
) {

    val linkDeviceSmartWatchUiState by viewModel.linkDeviceSmartWatchUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.validateDevicePhoneConnected()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


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

    val wearDestinationStart = when {
        linkDeviceSmartWatchUiState.connectedNodeId != null -> WearHomeNavigationRoutes.SESSION_METRICS
        claveAppCommunication == null && linkDeviceSmartWatchUiState.connectedNodeId == null -> WearHomeNavigationRoutes.APP_EMPTY_COMMUNICATION
        else -> WearHomeNavigationRoutes.LINK_DEVICE_PHONE
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = wearDestinationStart
    ) {
        // Pantalla 1: Menú Principal / Inicio de Sesión
        composable(WearHomeNavigationRoutes.APP_EMPTY_COMMUNICATION) {
            AppEmptyCommunicationScreen()
        }

        composable(WearHomeNavigationRoutes.LINK_DEVICE_PHONE) {
            LinkDevicePhoneScreen(onSuccessPermission = {
                navController.navigate(WearHomeNavigationRoutes.SESSION_METRICS)
            })
        }

        // Pantalla 2: Métrica en Vivo (La pantalla que creamos previamente)
        composable(WearHomeNavigationRoutes.SESSION_METRICS) {
            SkateSessionScreen(claveAppCommunication = claveAppCommunication)
        }
    }
}