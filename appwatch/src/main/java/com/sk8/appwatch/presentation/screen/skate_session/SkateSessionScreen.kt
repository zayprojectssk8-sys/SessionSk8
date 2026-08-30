package com.sk8.appwatch.presentation.screen.skate_session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.sk8.appwatch.presentation.screen.skate_session.ui_state.SkateSessionUiStateModel

@Composable
fun WearMetricsSessionScreen(
    metrics: SkateSessionUiStateModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Estado de la Sesión (Activa / En pausa)
            item {
                StatusBadge(isActive = metrics.isCurrentlyActive)
            }

            // 2. Tiempo Activo Principal
            item {
                Text(
                    text = formatSeconds(metrics.activeTimeSeconds),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 3. Frecuencia Cardíaca (BPM) y Calorías
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricChip(
                        label = "BPM",
                        value = if (metrics.bpm > 0) "${metrics.bpm}" else "--",
                        valueColor = Color(0xFFFF5252)
                    )
                    MetricChip(
                        label = "KCAL",
                        value = "%.0f".format(metrics.calories),
                        valueColor = Color(0xFFFF9800)
                    )
                }
            }

            // 4. Distancia y Velocidad Actual
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricChip(
                        label = "KM",
                        value = "%.2f".format(metrics.distanceKm),
                        valueColor = Color(0xFF4CAF50)
                    )
                    MetricChip(
                        label = "KM/H",
                        value = "%.1f".format(metrics.currentSpeedKmH),
                        valueColor = Color(0xFF2196F3)
                    )
                }
            }

            // 5. Métricas Secundarias de Velocidad (Max / Avg)
            item {
                Card(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SmallMetric(label = "Prom", value = "%.1f km/h".format(metrics.avgSpeedKmH))
                        SmallMetric(label = "Máx", value = "%.1f km/h".format(metrics.maxSpeedKmH))
                    }
                }
            }

            // 6. Contador de Caídas (Skate-Specific)
            item {
                CompactChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Caídas: ${metrics.fallCount}",
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.fallCount > 0) Color(0xFFFFD54F) else Color.Gray
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color(0xFF2C2C2C)
                    )
                )
            }

            // 7. Tiempo de Descanso
            item {
                Text(
                    text = "Descanso: ${formatSeconds(metrics.restTimeSeconds)}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isActive: Boolean) {
    val badgeColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFFFC107)
    val text = if (isActive) "EN SESIÓN" else "EN PAUSA"

    Box(
        modifier = Modifier
            .background(badgeColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SmallMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

private fun formatSeconds(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

@Composable
fun SkateSessionScreen(
    claveAppCommunication: String?,
    viewModel: SkateSessionViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        if (claveAppCommunication != null) {
            viewModel.loadCompleteLinkPhone(claveAppCommunication)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isTracking) {
            WearMetricsSessionScreen(metrics = uiState)
        } else {
            WaitScreen()
        }
    }
}

@Composable
fun WaitScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Skateboarding,
            contentDescription = "Esperando Session Skate",
            tint = MaterialTheme.colors.primary,
            modifier = Modifier
                .size(50.dp)
                .padding(bottom = 4.dp)
        )

        Text(
            text = "Esperando Session Skate",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

    }
}