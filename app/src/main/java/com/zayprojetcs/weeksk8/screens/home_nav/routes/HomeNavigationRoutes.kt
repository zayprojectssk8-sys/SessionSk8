package com.zayprojetcs.weeksk8.screens.home_nav.routes


import kotlinx.serialization.Serializable

@Serializable
sealed class HomeNavigationRoutes() {

    @Serializable
    object Menu : HomeNavigationRoutes()


    @Serializable
    object LinkDeviceSmartWatch : HomeNavigationRoutes()

    @Serializable
    object CreateSessionSkate : HomeNavigationRoutes()

    @Serializable
    object DetailSessionSkate : HomeNavigationRoutes()

}