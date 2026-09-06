package com.sk8.appwatch.presentation.screen.session_skate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.zaysk8.core.model.SkateSessionPhase
import java.util.Locale


@Composable
fun WearSessionScreen(
    viewModel: WearSessionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // =========================================================================
        // 1. ENCABEZADO
        // =========================================================================
        item {
            ListHeader {
                val headerText = when {
                    !uiState.hasActiveSessionConfig -> "SIN SESIÓN"
                    !uiState.isSessionStarted -> "SESIÓN LISTA"
                    uiState.currentPhase == SkateSessionPhase.SKATE -> {
                        val totalRoundsText =
                            if (uiState.totalRounds > 0) "${uiState.totalRounds}" else "∞"
                        "${uiState.currentPhase.displayName} (${uiState.currentRound}/$totalRoundsText)"
                    }

                    else -> uiState.currentPhase.displayName
                }

                Text(
                    text = headerText.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // =========================================================================
        // 2. TEMPORIZADOR O MENSAJE DE ESTADO
        // =========================================================================
        item {
            if (!uiState.hasActiveSessionConfig) {
                // Estado cuando no existe ninguna sesión creada
                Text(
                    text = "--:--",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            } else {
                val timerText = if (!uiState.isSessionStarted) {
                    formatSeconds(uiState.phaseTotalDurationSec.takeIf { it > 0 } ?: 300L)
                } else {
                    formatSeconds(uiState.phaseTimeRemainingSec)
                }

                Text(
                    text = timerText,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        uiState.isPaused -> MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        uiState.isWaitingManualStart -> MaterialTheme.colors.secondary
                        else -> MaterialTheme.colors.primary
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        // =========================================================================
        // 3. DESCRIPCIÓN DE ESTADO ACUMULADO
        // =========================================================================
        item {
            val statusSubtext = when {
                !uiState.hasActiveSessionConfig -> "Crea una sesión desde el celular"
                !uiState.isSessionStarted -> "Sesión lista para iniciar"
                uiState.currentPhase == SkateSessionPhase.COMPLETED -> "¡Gran trabajo!"
                else -> "Total: ${formatSeconds(uiState.generalElapsedTimeSec)}"
            }

            Text(
                text = statusSubtext,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // =========================================================================
        // 4. CONTROLES Y BOTONES DE ACCIÓN
        // =========================================================================
        when {
            // CASO 0: No existe ninguna sesión creada
            !uiState.hasActiveSessionConfig -> {
                item {
                    Chip(
                        onClick = {
                            viewModel.onRequestCreateSessionFromPhone()
                        },
                        label = {
                            Text(
                                text = "Abrir en Teléfono",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }

            // CASO A: Sesión ya creada y lista para iniciarse
            !uiState.isSessionStarted -> {
                val startLabel = if (uiState.currentPhase != SkateSessionPhase.NOT_STARTED) {
                    "Iniciar ${uiState.currentPhase.displayName}"
                } else {
                    "Iniciar Sesión"
                }

                item {
                    Chip(
                        onClick = { viewModel.onNextPhaseOrStartClicked() },
                        label = {
                            Text(
                                text = startLabel,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Iniciar Sesión"
                            )
                        },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }

            // CASO C: Sesión Finalizada (Evaluado antes de isWaitingManualStart)
            uiState.currentPhase == SkateSessionPhase.COMPLETED -> {
                item {
                    Text(
                        text = "¡Sesión Completada!",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.secondary
                    )
                }
            }

            // CASO B: Esperando confirmación manual para comenzar una fase/ronda
            uiState.isWaitingManualStart -> {
                item {
                    Chip(
                        onClick = { viewModel.onNextPhaseOrStartClicked() },
                        label = { Text("Comenzar Fase") },
                        icon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Comenzar Fase"
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }

            // CASO D: Sesión En Curso (Pausa, Avanzar, Terminar)
            else -> {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.onTogglePauseClicked() },
                            colors = ButtonDefaults.secondaryButtonColors(),
                            modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (uiState.isPaused) "Reanudar" else "Pausar"
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { viewModel.onNextPhaseOrStartClicked() },
                            colors = ButtonDefaults.primaryButtonColors(),
                            modifier = Modifier.size(ButtonDefaults.DefaultButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Siguiente Fase"
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }

                item {
                    Chip(
                        onClick = { viewModel.onFinishSessionClicked() },
                        label = { Text("Terminar", fontSize = 12.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Terminar",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}