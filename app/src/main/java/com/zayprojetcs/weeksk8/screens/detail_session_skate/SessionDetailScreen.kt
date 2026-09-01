package com.zayprojetcs.weeksk8.screens.detail_session_skate

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.screens.detail_session_skate.ui_state.PropertyStatus
import com.zayprojetcs.weeksk8.core.services.session_skate.SkateSessionService
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustomCreateSession
import com.zaysk8.core.model.SessionPhase

@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel = viewModel()
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.session ?: return


    val activeState = uiState.activeState
    val isWaitingAction = activeState.isWaitingUserAction || !activeState.isSessionStarted

    ScaffoldCustomCreateSession(
        title = "DETALLE SESIÓN SKATE",
        onButtonClick = {

        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            icon = Icons.Default.DirectionsRun,
                            configuredTime = "${session.warmupMinutes} min",
                            status = uiState.warmupStatus,
                            phaseTimer = if (uiState.activeState.phase == SessionPhase.WARMUP) uiState.formattedPhaseTimer else null,
                            isWaitingAction = isWaitingAction
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
                        isWaitingAction = isWaitingAction
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
                                status = if (uiState.activeState.phase == SessionPhase.EXTRA_TIME_RUNNING) PropertyStatus.IN_PROGRESS else PropertyStatus.PENDING,
                                phaseTimer = if (uiState.activeState.phase == SessionPhase.EXTRA_TIME_RUNNING) uiState.formattedPhaseTimer else null,
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
                            phaseTimer = if (uiState.activeState.phase == SessionPhase.COOL_DOWN) uiState.formattedPhaseTimer else null,
                            isWaitingAction = isWaitingAction
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
    isWaitingAction: Boolean
) {
    val isInProgress = status == PropertyStatus.IN_PROGRESS
    val cardColor =
        if (isInProgress) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
    }
}

@Composable
fun SkateRoundsContainerCard(
    totalRounds: Int,
    totalSkateMin: Int,
    totalRestMin: Int,
    currentRound: Int,
    currentPhase: SessionPhase,
    phaseTimer: String,
    roundsStatus: PropertyStatus,
    isWaitingAction: Boolean
) {
    val isRoundsActive = roundsStatus == PropertyStatus.IN_PROGRESS
    val isSkateActive = currentPhase == SessionPhase.SKATE_RUNNING
    val isRestActive = currentPhase == SessionPhase.REST_RUNNING

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
                phaseTimer = if (isSkateActive) phaseTimer else null,
                isWaitingAction = isWaitingAction
            )

            // TARJETA INTERNA: DESCANSO DE RONDA INDIVIDUAL
            if (totalRestMin > 0) {
                IndividualSubPhaseCard(
                    title = "Descanso (Ronda $currentRound/$roundsCount)",
                    icon = Icons.Default.FitnessCenter,
                    configuredTime = formatMinSec(restTimePerRoundSec),
                    isActive = isRestActive,
                    phaseTimer = if (isRestActive) phaseTimer else null,
                    isWaitingAction = isWaitingAction
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
    phaseTimer: String?,
    isWaitingAction: Boolean
) {
    val bgColor =
        if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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

@Composable
fun SessionControlBottomBar(
    isPaused: Boolean,
    isWaitingStart: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipPhase: () -> Unit,
    onStopSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onStopSession,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Finalizar Sesión"
                )
            }

            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isWaitingStart) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    contentColor = if (isWaitingStart) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Reanudar / Iniciar" else "Pausar",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = onSkipPhase,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Saltar Fase"
                )
            }
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