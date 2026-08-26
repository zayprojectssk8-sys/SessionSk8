package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Skateboarding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    PROGRESS("PROGRESO", Icons.Outlined.Skateboarding),
    TRICKS("TRUCOS", Icons.AutoMirrored.Outlined.List)
}

@Composable
fun NavigationSuiteCustom(
    itemSelected: @Composable (AppDestinations) -> Unit
) {

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PROGRESS) }

    val navigationColor = NavigationSuiteDefaults.colors(
        navigationBarContentColor = MaterialTheme.colorScheme.background,
        navigationBarContainerColor = MaterialTheme.colorScheme.background,
        navigationRailContainerColor = MaterialTheme.colorScheme.background,
        navigationRailContentColor = MaterialTheme.colorScheme.background
    )

    val navigationItemColor = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = Color.Transparent
        ), navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = Color.Transparent
        )
    )


    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxWidth(),
        navigationSuiteColors = navigationColor,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.background,
        navigationSuiteItems = {
            AppDestinations.entries.forEach {

                item(
                    colors = navigationItemColor,
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = {
                        Text(
                            text = it.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    alwaysShowLabel = false,
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )

            }

        }
    ) {
        itemSelected(currentDestination)
    }
}
