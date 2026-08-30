package com.zayprojetcs.weeksk8.screens.menu


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.ui.customs.HorizontalDividerCustom
import com.zayprojetcs.weeksk8.ui.customs.MenuGroupCustom
import com.zayprojetcs.weeksk8.ui.customs.MenuItemButtonCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom

@Composable
fun MenuScreen(
    onNavigateLinkSmartWatch: () -> Unit,
    onNavigateCreateSession: () -> Unit,
    onNavigateDetailSession: () -> Unit,
    viewModel: MenuViewModel = viewModel()
) {

    val mapUiState by viewModel.menuUiState.collectAsStateWithLifecycle()


    // --- UI Principal de tu App ---
    ScaffoldCustom(
        title = "CONTENIDO",
        content = {
            if (mapUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 4.dp
                    )
                }
                return@ScaffoldCustom
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                MenuGroupCustom {
                    if (mapUiState.sessionWithRoundsTrick == null) {

                        MenuItemButtonCustom(
                            title = "CREAR SESIÓN SKATE",
                            subtitle = "Diseña tu rutina con tiempos fijos o sesión libre",
                            icon = Icons.Default.Skateboarding,
                            onClick = onNavigateCreateSession,
                            containerColor = Color.Transparent
                        )
                    } else {
                        MenuItemButtonCustom(
                            title = "SESIÓN SKATE CREADA",
                            subtitle = "Tienes una sesión creada de ${mapUiState.sessionWithRoundsTrick?.roomSession?.getDurationTimeSession()}.",
                            icon = Icons.Default.Skateboarding,
                            onClick = onNavigateDetailSession,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    MenuItemButtonCustom(
                        title = "MIS SESIONES",
                        subtitle = "Revisa tus sesiones pasadas, trucos logrados y tiempos",
                        icon = Icons.Default.History,
                        badgeText = mapUiState.historyCountSession.toString(),
                        onClick = onNavigateCreateSession,
                        containerColor = Color.Transparent
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))


                    MenuItemButtonCustom(
                        title = mapUiState.connectedDevice?.name ?: "VINCULAR MI RELOJ",
                        subtitle = if (mapUiState.connectedDevice != null) "Conectado dispositivo ${mapUiState.connectedDevice?.getTypeWearable()} ${mapUiState.connectedDevice?.brand?.name}." else "Conecta tu reloj para monitorear tu salud y rendimiento en tiempo real",
                        icon = Icons.Default.Watch,
                        onClick = onNavigateLinkSmartWatch,
                        containerColor = Color.Transparent,
                        contentColor = if (mapUiState.connectedDevice != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                }

                HorizontalDividerCustom()

                // Grupo 2: Ajustes generales (Botón individual)
                MenuItemButtonCustom(
                    title = "Ajustes de Sesión",
                    subtitle = "Notificaciones, tono de alerta y sensor Wear OS",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateCreateSession
                )

            }
        }
    )
}

