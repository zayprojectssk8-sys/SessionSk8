package com.zayprojetcs.weeksk8.screens.create_session_skate.modules

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
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
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.RoundPreset
import com.zaysk8.core.utils.getFormatDurationSession


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionSkateConfigRoundScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onPresetSelected: (RoundPreset) -> Unit
) {

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LoadConfigSessionData(createSessionSkateUiStateModel)

        Spacer(modifier = Modifier.height(5.dp))

        LoadResultTypeRoundSelected(createSessionSkateUiStateModel)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Selecciona el ritmo de la sesión",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // --- SECCIÓN DE PRESETS ---
            RoundPreset.entries.forEach { preset ->
                val isSelected = createSessionSkateUiStateModel.roundPreset == preset

                OutlinedCard(
                    onClick = { onPresetSelected(preset) },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = isSelected),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onPresetSelected(preset) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (preset.isRecommended) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.extraSmall
                                        ) {
                                            Text(
                                                text = "RECOMENDADO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tiempos del preset
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 40.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${preset.skateMinutes} min Skate") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DirectionsRun,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${preset.restMinutes} min Agua") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }


        }
    }


}

@Composable
fun LoadConfigSessionData(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onBackClick: () -> Unit = {}
) {
    val currentSliderValue = (createSessionSkateUiStateModel.durationSessionMinutes ?: 0).toFloat()
    val timeFormat = getFormatDurationSession(currentSliderValue.toInt())
    // --- HEADER DE DURACIÓN SELECCIONADA ---
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBackClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Modo de tiempo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (createSessionSkateUiStateModel.durationSessionMinutes == null) "Sesión Abierta" else "$timeFormat / ${createSessionSkateUiStateModel.durationSessionMinutes} min totales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        if (createSessionSkateUiStateModel.durationSessionMinutes == null) "Ilimitado" else "Programado",
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (createSessionSkateUiStateModel.durationSessionMinutes == null) Icons.Default.AllInclusive else Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun LoadResultTypeRoundSelected(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
) {
    // Cálculo de rondas e intervalos


    // --- RESUMEN Y CÁLCULO DE RONDAS ---
    AnimatedContent(
        targetState = createSessionSkateUiStateModel.durationSessionMinutes,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SummaryTransition"
    ) { duration ->
        if (duration == null) {
            // MODO 1: SESIÓN ABIERTA
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rondas ilimitadas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "El reloj emitirá una alerta cada ${createSessionSkateUiStateModel.roundPreset.skateMinutes} min para tu pausa de ${createSessionSkateUiStateModel.roundPreset.restMinutes} min de hidratación hasta que finalices la sesión manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        } else {
            // MODO 2: TIEMPO PROGRAMADO (CÁLCULO AUTOMÁTICO)
            if ((createSessionSkateUiStateModel.calculatedRounds ?: 0) > 0) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        Text(
                            text = "Desglose de la sesión",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Rondas completas:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${createSessionSkateUiStateModel.calculatedRounds} rondas",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tiempo efectivo de skate:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${createSessionSkateUiStateModel.totalSkateTime} min", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tiempo total de hidratación:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${createSessionSkateUiStateModel.totalRestTime} min", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Margen de tiempo extra:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val textMargin =
                                if (createSessionSkateUiStateModel.marginMinutes == null) "0 min" else "+${createSessionSkateUiStateModel.marginMinutes} min"
                            Text(
                                text = "+${createSessionSkateUiStateModel.marginMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // TIEMPO INSUFICIENTE PARA 1 RONDA COMPLETA
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "El tiempo seleccionado ($duration min) es menor a la duración de 1 ronda completa de ${createSessionSkateUiStateModel.roundPreset.title} (${createSessionSkateUiStateModel.roundPreset.totalRoundMinutes} min). Elige más tiempo o cambia de preset.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

}