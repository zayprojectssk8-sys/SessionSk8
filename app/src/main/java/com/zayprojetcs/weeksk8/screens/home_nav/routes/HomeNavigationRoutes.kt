package com.zayprojetcs.weeksk8.screens.home_nav.routes


import kotlinx.serialization.Serializable

@Serializable
sealed class HomeNavigationRoutes() {

    @Serializable
    object Menu : HomeNavigationRoutes()
    @Serializable
    object LinkDeviceSmartWatch : HomeNavigationRoutes()

    @Serializable
    object DashBoard : HomeNavigationRoutes()

    @Serializable
    object CreateDaySkate : HomeNavigationRoutes()
    @Serializable
    object StartDaySkate : HomeNavigationRoutes()
}