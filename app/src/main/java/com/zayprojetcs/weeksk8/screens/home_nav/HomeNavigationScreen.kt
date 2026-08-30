package com.zayprojetcs.weeksk8.screens.home_nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zayprojetcs.weeksk8.screens.create_session_skate.CreateSessionSkateScreen
import com.zayprojetcs.weeksk8.screens.detail_session_skate.SessionDetailScreen
import com.zayprojetcs.weeksk8.screens.home_nav.routes.HomeNavigationRoutes
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.LinkDeviceSmartWatchScreen
import com.zayprojetcs.weeksk8.screens.menu.MenuScreen
import com.zayprojetcs.weeksk8.ui.theme.ForceOrientationPortrait

@Composable
fun HomeNavigationScreen(viewModel: HomeNavigationViewModel = viewModel()) {

    val startSession by viewModel.startSession.collectAsStateWithLifecycle()

    /*if (startSession != null) {
        StartDaySkateScreen()
        return
    }*/

    ForceOrientationPortrait()

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeNavigationRoutes.Menu) {

        composable<HomeNavigationRoutes.Menu> {
            MenuScreen(
                onNavigateLinkSmartWatch = {
                    navController.navigate(HomeNavigationRoutes.LinkDeviceSmartWatch)
                },
                onNavigateCreateSession = {
                    navController.navigate(HomeNavigationRoutes.CreateSessionSkate)
                },
                onNavigateDetailSession = {
                    navController.navigate(HomeNavigationRoutes.DetailSessionSkate)
                })
        }
        composable<HomeNavigationRoutes.CreateSessionSkate> {
            CreateSessionSkateScreen(onNavigateBackPress = {
                navController.popBackStack()
            })
        }

        composable<HomeNavigationRoutes.DetailSessionSkate> {
            SessionDetailScreen()
        }

        composable<HomeNavigationRoutes.LinkDeviceSmartWatch> {
            LinkDeviceSmartWatchScreen(onFinished = {
                navController.popBackStack()
            })
        }
    }
}
