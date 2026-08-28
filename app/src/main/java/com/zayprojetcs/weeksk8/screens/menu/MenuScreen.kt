package com.zayprojetcs.weeksk8.screens.menu


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.LoadDeviceDetectedScreen
import com.zayprojetcs.weeksk8.ui.customs.HorizontalDividerCustom
import com.zayprojetcs.weeksk8.ui.customs.MenuGroupCustom
import com.zayprojetcs.weeksk8.ui.customs.MenuItemButtonCustom
import com.zayprojetcs.weeksk8.ui.customs.OutlineCardIInfoCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom

@Composable
fun MenuScreen(
    onNavigateLinkSmartWatch: () -> Unit,
    onNavigateCreateSession: () -> Unit,
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

                if (mapUiState.connectedDevice == null) {
                    SmartwatchPromoCard(
                        onConnectClick = onNavigateLinkSmartWatch,
                        onDismissClick = {})
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LoadDeviceDetectedScreen(
                            mapUiState.connectedDevice!!,
                            null
                        )
                    }
                }


                MenuGroupCustom {
                    MenuItemButtonCustom(
                        title = "CREAR SESIÓN SKATE",
                        subtitle = "Diseña tu rutina con tiempos fijos o sesión libre",
                        icon = Icons.Default.Skateboarding,
                        onClick = onNavigateCreateSession,
                        containerColor = Color.Transparent
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    MenuItemButtonCustom(
                        title = "MIS SESIONES",
                        subtitle = "Revisa tus sesiones pasadas, trucos logrados y tiempos",
                        icon = Icons.Default.History,
                        badgeText = "12",
                        onClick = onNavigateCreateSession,
                        containerColor = Color.Transparent
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


@Composable
fun SmartwatchPromoCard(
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit,
    onDismissClick: (() -> Unit)? = null
) {
    OutlineCardIInfoCustom(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        imageVector = Icons.Outlined.Watch,
        title = "Vincular mi reloj",
        description = "Conecta tu reloj para registrar impactos, detectar caídas y medir tu ritmo cardíaco en tiempo real durante tus sesiones.",
        buttonLabel = "Vincular mi reloj",
        onConnectClick = onConnectClick,
        onDismissClick = onDismissClick
    )
}

