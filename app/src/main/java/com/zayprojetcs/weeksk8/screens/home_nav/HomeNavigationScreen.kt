package com.zayprojetcs.weeksk8.screens.home_nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.LinkDeviceSmartWatchScreen
import com.zayprojetcs.weeksk8.screens.create_day_skate.CreateDaySkateScreen
import com.zayprojetcs.weeksk8.screens.dashboard.DashboardScreen
import com.zayprojetcs.weeksk8.screens.home_nav.routes.HomeNavigationRoutes
import com.zayprojetcs.weeksk8.screens.menu.MenuScreen
import com.zayprojetcs.weeksk8.screens.start_day_skate.StartDaySkateScreen
import com.zayprojetcs.weeksk8.ui.theme.ForceOrientationPortrait

@Composable
fun HomeNavigationScreen(viewModel: HomeNavigationViewModel = viewModel()) {
    val startDay by viewModel.startDay.collectAsStateWithLifecycle()


    if (startDay != null) {
        StartDaySkateScreen()
        return
    }

    ForceOrientationPortrait()

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeNavigationRoutes.Menu) {

        composable<HomeNavigationRoutes.Menu> {
            MenuScreen(onNavigateLinkSmartWatch = {
                navController.navigate(HomeNavigationRoutes.LinkDeviceSmartWatch)
            })
        }
        composable<HomeNavigationRoutes.LinkDeviceSmartWatch> {
            LinkDeviceSmartWatchScreen(onFinished = {
                navController.popBackStack()
            })
        }

        composable<HomeNavigationRoutes.DashBoard> {
            DashboardScreen(onCreateDaySkate = {
                navController.navigate(HomeNavigationRoutes.CreateDaySkate)
            })
        }

        composable<HomeNavigationRoutes.CreateDaySkate> {
            CreateDaySkateScreen()
        }

    }
}
