package com.sk8.appwatch.presentation.screen.home_watch

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sk8.appwatch.presentation.screen.app_empty_communication.AppEmptyCommunicationScreen
import com.sk8.appwatch.presentation.screen.home_watch.routes.WearHomeNavigationRoutes
import com.sk8.appwatch.presentation.screen.link_device_phone.LinkDevicePhoneScreen
import com.sk8.appwatch.presentation.screen.skate_session.SkateSessionScreen
import com.sk8.appwatch.presentation.utils.getRequiredWearOsPermissionsGranted

@Composable
fun WearAppNavigation(
    claveAppCommunication: String? = null,
    navController: NavHostController = rememberSwipeDismissableNavController()
) {

    val context = LocalContext.current

    val wearDestinationStart =
        if (claveAppCommunication == null) WearHomeNavigationRoutes.LINK_DEVICE_PHONE
        else {
            if (context.getRequiredWearOsPermissionsGranted()) WearHomeNavigationRoutes.SESSION_METRICS
            else WearHomeNavigationRoutes.LINK_DEVICE_PHONE
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
            SkateSessionScreen(claveAppCommunication)
        }
    }
}