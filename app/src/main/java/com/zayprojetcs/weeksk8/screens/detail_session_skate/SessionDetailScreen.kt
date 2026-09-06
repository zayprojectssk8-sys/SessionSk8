package com.zayprojetcs.weeksk8.screens.detail_session_skate

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.core.helper.model.DeviceWearable
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.services.session_skate.SkateSessionService
import com.zayprojetcs.weeksk8.screens.detail_session_skate.module.PermissionsSessionScreen
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PhaseSensorSummary
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PropertyStatus
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.SensorDataPoint
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom
import com.zaysk8.core.model.SkateSessionPhase
import java.util.Locale

@Composable
fun DeviceWearableCard(
    device: DeviceWearable,
    onClick: (DeviceWearable) -> Unit
) {
    Card(
        onClick = { onClick(device) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del ícono principal con diferenciación visual para Wear OS
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.isWearOs) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = device.getTypeWearable(),
                    tint = if (device.isWearOs) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información técnica del dispositivo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${device.getTypeWearable()} • ${device.brand}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Estado de la app (Instalada vs No instalada)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (device.isAppInstalled) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (device.isAppInstalled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = if (device.isAppInstalled) "App de Skate lista" else "Requiere instalar app",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (device.isAppInstalled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Chip indicador distintivo de Wear OS
            if (device.isWearOs) {
                Spacer(modifier = Modifier.width(8.dp))
                SuggestionChip(
                    onClick = { onClick(device) },
                    label = {
                        Text(
                            text = "Wear OS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun PhaseSensorMetricsCard(
    metrics: PhaseSensorSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "RESULTADOS DE SENSORES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // --- GRID DE METRICAS CLAVE (KPIs) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricChip(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    label = "Pasos",
                    value = "${metrics.totalSteps}"
                )
                MetricChip(
                    icon = Icons.Default.FlashOn,
                    label = "Pico G",
                    value = String.format(Locale.US, "%.1f G", metrics.maxGForce)
                )
                MetricChip(
                    icon = Icons.Default.Equalizer,
                    label = "Giro (rad/s)",
                    value = String.format(Locale.US, "%.1f", metrics.avgGyroscope)
                )
                MetricChip(
                    icon = Icons.Default.Terrain,
                    label = "Elevación",
                    value = String.format(Locale.US, "%.1fm", metrics.elevationGainMeters)
                )
            }

            // --- GRÁFICA DE MOVIMIENTO / INTENSIDAD ---
            if (metrics.movementPoints.isNotEmpty()) {
                Text(
                    text = "Intensidad de Movimiento",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SensorSparklineChart(
                    dataPoints = metrics.movementPoints,
                    lineColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SensorSparklineChart(
    dataPoints: List<SensorDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val maxY = (dataPoints.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val minY = (dataPoints.minOfOrNull { it.value } ?: 0f).coerceAtMost(0f)

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val pointsCount = dataPoints.size

            if (pointsCount < 2) return@Canvas

            val stepX = width / (pointsCount - 1)
            val strokePath = Path()
            val fillPath = Path()

            dataPoints.forEachIndexed { index, point ->
                val x = index * stepX
                val normalizedY = (point.value - minY) / (maxY - minY)
                val y = height - (normalizedY * height)

                if (index == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    strokePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == pointsCount - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Gradiente traslúcido bajo la curva
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.0f)
                    )
                )
            )

            // Línea principal de la gráfica
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.session ?: return

    val activeState = uiState.activeState
    val isWaitingAction = activeState.isWaitingUserAction || !activeState.isSessionStarted

    if (!uiState.permissionSessionGranted) {
        PermissionsSessionScreen(onAllPermissionsGranted = {
            viewModel.onPermissionGranted()
        }, onSkipOrCancel = {})
        return
    }


    ScaffoldCustom(
        title = "DETALLE SESIÓN SKATE",
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.deviceWearable != null) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        DeviceWearableCard(uiState.deviceWearable!!, onClick = {
                            viewModel.onWearableSelected(it)
                        })
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // --- HEADER: TIEMPO GENERAL ACUMULADO ---
                item {
                    GeneralTimerHeaderCard(
                        totalTime = uiState.formattedTotalTimer,
                        isSessionStarted = activeState.isSessionStarted,
                        isWaitingAction = isWaitingAction
                    )
                }

                // --- ALERTA DE ACCIÓN MANUAL ---
                item {
                    AnimatedVisibility(
                        visible = isWaitingAction,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (!activeState.isSessionStarted) "¡Sesión Lista!" else "¡Fase Lista!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Presiona iniciar para arrancar el cronómetro de la fase.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Button(
                                    onClick = {
                                        context.startSkateSession(session.idSession)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Text("INICIAR", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- SECCIÓN: FASES DE LA SESIÓN ---
                item {
                    Text(
                        text = "Fases de la Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // 1. CALENTAMIENTO
                if (session.warmupMinutes > 0) {
                    item {
                        PhaseCard(
                            title = "Calentamiento",
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            configuredTime = "${session.warmupMinutes} min",
                            status = uiState.warmupStatus,
                            phaseTimer = if (uiState.activeState.phase == SkateSessionPhase.WARMUP) uiState.formattedPhaseTimer else null,
                            isWaitingAction = isWaitingAction,
                            sensorSummary = uiState.phaseSummaries["WARMUP_1"]
                        )
                    }
                }

                // 2. RONDAS DE SKATE (PATINAJE Y DESCANSO)
                item {
                    SkateRoundsContainerCard(
                        totalRounds = session.calculatedRounds,
                        totalSkateMin = session.totalSkateTime,
                        totalRestMin = session.totalRestTime,
                        currentRound = activeState.currentRound,
                        currentPhase = activeState.phase,
                        phaseTimer = uiState.formattedPhaseTimer,
                        roundsStatus = uiState.skateRoundsStatus,
                        isWaitingAction = isWaitingAction,
                        phaseSummaries = uiState.phaseSummaries
                    )
                }

                // 3. TIEMPO EXTRA (MARGEN)
                session.marginMinutes?.let { margin ->
                    if (margin > 0) {
                        item {
                            PhaseCard(
                                title = "Tiempo Extra (Margen)",
                                icon = Icons.Default.MoreTime,
                                configuredTime = "$margin min",
                                status = if (uiState.activeState.phase == SkateSessionPhase.EXTRA_TIME) PropertyStatus.IN_PROGRESS else PropertyStatus.PENDING,
                                phaseTimer = if (uiState.activeState.phase == SkateSessionPhase.EXTRA_TIME) uiState.formattedPhaseTimer else null,
                                isWaitingAction = isWaitingAction
                            )
                        }
                    }
                }

                // 4. ESTIRAMIENTO FINAL
                if (session.cooldownMinutes > 0) {
                    item {
                        PhaseCard(
                            title = "Estiramiento Final",
                            icon = Icons.Default.SelfImprovement,
                            configuredTime = "${session.cooldownMinutes} min",
                            status = uiState.cooldownStatus,
                            phaseTimer = if (uiState.activeState.phase == SkateSessionPhase.STRETCHING) uiState.formattedPhaseTimer else null,
                            isWaitingAction = isWaitingAction,
                            sensorSummary = uiState.phaseSummaries["STRETCHING_1"]
                        )
                    }
                }

                // --- SECCIÓN: CONFIGURACIÓN DE TRUCOS ---
                item {
                    Text(
                        text = "Modo de Trucos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                item {
                    TrickConfigCard(session = session)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    )
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun GeneralTimerHeaderCard(
    totalTime: String,
    isSessionStarted: Boolean,
    isWaitingAction: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isWaitingAction -> MaterialTheme.colorScheme.tertiaryContainer
            isSessionStarted -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "HeaderColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Tiempo Acumulado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "TIEMPO GENERAL ACUMULADO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = totalTime,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isSessionStarted) "El tiempo global no se detiene entre rondas" else "Esperando inicio de la sesión",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PhaseCard(
    title: String,
    icon: ImageVector,
    configuredTime: String,
    status: PropertyStatus,
    phaseTimer: String?,
    isWaitingAction: Boolean,
    sensorSummary: PhaseSensorSummary? = null
) {
    val isInProgress = status == PropertyStatus.IN_PROGRESS
    val isCompleted = status == PropertyStatus.COMPLETED
    val cardColor =
        if (isInProgress) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isInProgress) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                )
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isInProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceColorAtElevation(
                            4.dp
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isInProgress) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configurado: $configuredTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isInProgress && phaseTimer != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = phaseTimer,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (isWaitingAction) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                        StatusBadge(status = status)
                    }
                } else {
                    StatusBadge(status = status)
                }
            }

            // --- MOSTRAR RESULTADO DE SENSORES AL COMPLETAR LA FASE ---
            AnimatedVisibility(
                visible = isCompleted && sensorSummary != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    sensorSummary?.let { summary ->
                        PhaseSensorMetricsCard(metrics = summary)
                    }
                }
            }
        }
    }
}

@Composable
fun SkateRoundsContainerCard(
    totalRounds: Int,
    totalSkateMin: Int,
    totalRestMin: Int,
    currentRound: Int,
    currentPhase: SkateSessionPhase,
    phaseTimer: String,
    roundsStatus: PropertyStatus,
    isWaitingAction: Boolean,
    phaseSummaries: Map<String, PhaseSensorSummary> = emptyMap()
) {
    val isRoundsActive = roundsStatus == PropertyStatus.IN_PROGRESS
    val isSkateActive = currentPhase == SkateSessionPhase.SKATE
    val isRestActive = currentPhase == SkateSessionPhase.REST

    val roundsCount = if (totalRounds > 0) totalRounds else 1
    val skateTimePerRoundSec = (totalSkateMin * 60) / roundsCount
    val restTimePerRoundSec = (totalRestMin * 60) / roundsCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isRoundsActive) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                )
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isRoundsActive) MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.25f
            ) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Skateboarding,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Rondas de Patinaje",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$roundsCount rondas programadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = roundsStatus)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // TARJETA INTERNA: RONDA DE PATINAJE INDIVIDUAL
            IndividualSubPhaseCard(
                title = "Patinaje (Ronda $currentRound/$roundsCount)",
                icon = Icons.Default.Skateboarding,
                configuredTime = formatMinSec(skateTimePerRoundSec),
                isActive = isSkateActive,
                isCompleted = roundsStatus == PropertyStatus.COMPLETED,
                phaseTimer = if (isSkateActive) phaseTimer else null,
                isWaitingAction = isWaitingAction,
                sensorSummary = phaseSummaries["SKATE_$currentRound"]
            )

            // TARJETA INTERNA: DESCANSO DE RONDA INDIVIDUAL
            if (totalRestMin > 0) {
                IndividualSubPhaseCard(
                    title = "Descanso (Ronda $currentRound/$roundsCount)",
                    icon = Icons.Default.FitnessCenter,
                    configuredTime = formatMinSec(restTimePerRoundSec),
                    isActive = isRestActive,
                    isCompleted = roundsStatus == PropertyStatus.COMPLETED,
                    phaseTimer = if (isRestActive) phaseTimer else null,
                    isWaitingAction = isWaitingAction,
                    sensorSummary = phaseSummaries["REST_$currentRound"]
                )
            }
        }
    }
}

@Composable
fun IndividualSubPhaseCard(
    title: String,
    icon: ImageVector,
    configuredTime: String,
    isActive: Boolean,
    isCompleted: Boolean = false,
    phaseTimer: String?,
    isWaitingAction: Boolean,
    sensorSummary: PhaseSensorSummary? = null
) {
    val bgColor =
        if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Duración ronda: $configuredTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isActive && phaseTimer != null) {
                    Text(
                        text = phaseTimer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isWaitingAction) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- GRÁFICA / MÉTRICAS EN RONDA INDIVIDUAL AL FINALIZAR ---
            AnimatedVisibility(
                visible = isCompleted && sensorSummary != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    sensorSummary?.let { summary ->
                        PhaseSensorMetricsCard(metrics = summary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PropertyStatus) {
    val (backgroundColor, textColor) = when (status) {
        PropertyStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        PropertyStatus.COMPLETED -> Color(0xFF2E7D32).copy(alpha = 0.2f) to Color(0xFF4CAF50)
        PropertyStatus.PENDING -> MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) to MaterialTheme.colorScheme.outline
        PropertyStatus.DISABLED -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.outline.copy(
            alpha = 0.5f
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun TrickConfigCard(session: RoomSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Seguimiento: ${session.getTrickTrackingMode().name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Orden: ${session.getTrickOrderMode().name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Distribución: ${session.getTrickDistributionMode().name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Trucos Seleccionados: ${session.selectedCustomTricksCount} (Desbloqueados: ${session.unlockedTricksCount})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun Context.startSkateSession(sessionId: Long) {
    val intent = Intent(this, SkateSessionService::class.java).apply {
        action = SkateSessionService.ACTION_START_SESSION
        putExtra(SkateSessionService.EXTRA_SESSION_ID, sessionId)
    }
    // ContextCompat maneja automáticamente las diferencias de versión de Android (API 26+)
    ContextCompat.startForegroundService(this, intent)
}

private fun formatMinSec(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return if (s > 0) "${m}m ${s}s" else "${m}m"
}