package com.zayprojetcs.weeksk8.screens.create_session_skate.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmupCooldownConfigScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onWarmupEnabledChanged: (Boolean) -> Unit,
    onWarmupMinutesChanged: (Int) -> Unit,
    onCooldownEnabledChanged: (Boolean) -> Unit,
    onCooldownMinutesChanged: (Int) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ==========================================
        // SECCIÓN 1: CALENTAMIENTO INICIAL
        // ==========================================
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (createSessionSkateUiStateModel.warmupEnabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder(enabled = createSessionSkateUiStateModel.warmupEnabled),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Calentamiento Previo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = createSessionSkateUiStateModel.warmupEnabled,
                        onCheckedChange = onWarmupEnabledChanged
                    )
                }

                // Tarjeta informativa "Por qué es bueno"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "¿Por qué calentamiento dinámico?",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Lubrica las articulaciones (tobillos y rodillas) para resistir el impacto de recepciones duras.\n" +
                                    "• Eleva la temperatura muscular aumentando la velocidad de respuesta y el estallido al picar el pop.\n" +
                                    "• Reduce radicalmente el riesgo de torceduras graves al pisar mal la tabla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = createSessionSkateUiStateModel.warmupEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {


                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tiempo de calentamiento: ${createSessionSkateUiStateModel.warmupMinutes} min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15).forEach { mins ->
                                FilterChip(
                                    selected = createSessionSkateUiStateModel.warmupMinutes == mins,
                                    onClick = { onWarmupMinutesChanged(mins) },
                                    label = { Text("$mins min") },
                                    leadingIcon = if (createSessionSkateUiStateModel.warmupMinutes == mins) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECCIÓN 2: ESTIRAMIENTO FINAL / COOL-DOWN
        // ==========================================
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (createSessionSkateUiStateModel.cooldownEnabled)
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder(enabled = createSessionSkateUiStateModel.cooldownEnabled),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Estiramiento Final",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = createSessionSkateUiStateModel.cooldownEnabled,
                        onCheckedChange = onCooldownEnabledChanged
                    )
                }

                // Tarjeta informativa "Por qué es bueno"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "¿Por qué estirar al terminar?",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Libera la tensión acumulada en gemelos, cuádriceps y espalda baja tras múltiples impactos.\n" +
                                    "• Acelera la eliminación de ácido láctico reduciendo el dolor muscular post-sesión (DOMS).\n" +
                                    "• Ayuda al cuerpo a bajar pulsaciones y entrar en estado de regeneración.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


                AnimatedVisibility(
                    visible = createSessionSkateUiStateModel.cooldownEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tiempo de estiramiento: ${createSessionSkateUiStateModel.cooldownMinutes} min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15).forEach { mins ->
                                FilterChip(
                                    selected = createSessionSkateUiStateModel.cooldownMinutes == mins,
                                    onClick = { onCooldownMinutesChanged(mins) },
                                    label = { Text("$mins min") },
                                    leadingIcon = if (createSessionSkateUiStateModel.cooldownMinutes == mins) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}