package com.zayprojetcs.weeksk8.screens.create_session_skate.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.ui.customs.HorizontalDividerCustom
import com.zaysk8.core.utils.getFormatDurationSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreSessionSummaryScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
) {
    val baseSessionMinutes = createSessionSkateUiStateModel.durationSessionMinutes

    val roundDuration = createSessionSkateUiStateModel.roundPreset.totalRoundMinutes

    val roundsCount = createSessionSkateUiStateModel.durationSessionMinutes?.let {
        if (roundDuration > 0) it / roundDuration else 0
    } ?: 0
    val workRestRatio =
        "${createSessionSkateUiStateModel.roundPreset.skateMinutes}:${createSessionSkateUiStateModel.roundPreset.restMinutes}"

    // Cálculo del tiempo total sumando calentamiento y estiramiento
    val actualWarmup =
        if (createSessionSkateUiStateModel.warmupEnabled) createSessionSkateUiStateModel.warmupMinutes else 0
    val actualCooldown =
        if (createSessionSkateUiStateModel.cooldownEnabled) createSessionSkateUiStateModel.cooldownMinutes else 0
    val totalMinutes = (baseSessionMinutes ?: 0) + actualWarmup + actualCooldown

    // Formateo dinámico del texto de duración
    val durationText = getFormatDurationSession(totalMinutes)

    val shape16 = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ==========================================
        // HEADER HERO: TIEMPO TOTAL CALCULADO
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape16)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = shape16
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TIEMPO TOTAL DE SESIÓN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                if (baseSessionMinutes == null) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "$durationText / $totalMinutes min",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Desglose de minutos
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (createSessionSkateUiStateModel.warmupEnabled) {
                        Text(
                            text = "Calent. ${actualWarmup}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(" • ", style = MaterialTheme.typography.bodySmall)
                    }

                    val textTime =
                        if (baseSessionMinutes == null) "Skate Ilimitado" else "Skate ${baseSessionMinutes}m"
                    Text(
                        text = textTime,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (createSessionSkateUiStateModel.cooldownEnabled) {
                        Text(" • ", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Estir. ${actualCooldown}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ==========================================
        // BLOQUE 1: ESTRUCTURA DE RONDAS E HIDRATACIÓN
        // ==========================================
        SummarySectionCard(
            title = "Estructura & Hidratación",
            icon = Icons.Default.Timer
        ) {
            SummaryItemRow(
                label = "Rondas de Skate",
                value = if (baseSessionMinutes == null) "Rondas ilimitadas" else "$roundsCount rondas ($workRestRatio)"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SummaryItemRow(
                label = "Recordatorio de Agua",
                value = "Hidratate cada ${createSessionSkateUiStateModel.roundPreset.skateMinutes} min"
            )
        }

        // ==========================================
        // BLOQUE 2: PREPARACIÓN Y RECUPERACIÓN
        // ==========================================
        SummarySectionCard(
            title = "Fases de Preparación",
            icon = Icons.Default.FitnessCenter
        ) {
            SummaryItemRow(
                label = "Calentamiento Previo",
                value = if (createSessionSkateUiStateModel.warmupEnabled) "${createSessionSkateUiStateModel.warmupMinutes} min (Dinámico)" else "Desactivado"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SummaryItemRow(
                label = "Estiramiento Final",
                value = if (createSessionSkateUiStateModel.cooldownEnabled) "${createSessionSkateUiStateModel.cooldownMinutes} min (Estático)" else "Desactivado"
            )
        }

        // ==========================================
        // BLOQUE 3: TRUCOS Y ADMINISTRACIÓN
        // ==========================================
        SummarySectionCard(
            title = "Rutina de Trucos",
            icon = Icons.Default.FormatListNumbered
        ) {
            SummaryItemRow(
                label = "Trucos Seleccionados",
                value = if (createSessionSkateUiStateModel.selectedTrickList.isNotEmpty()) "${createSessionSkateUiStateModel.selectedTrickList.size} trucos" else "Sesión Libre"
            )
            if (createSessionSkateUiStateModel.selectedTrickList.isNotEmpty()) {
                HorizontalDividerCustom()
                SummaryItemRow(
                    label = "Modo de Registro",
                    value = createSessionSkateUiStateModel.trickTrackingMode.title
                )

                SummaryItemRow(
                    label = "Modo de Practica",
                    value = createSessionSkateUiStateModel.trickOrderMode.title
                )


                SummaryItemRow(
                    label = "Orden",
                    value = createSessionSkateUiStateModel.trickDistributionMode.title
                )
            }
        }

        // ==========================================
        // BLOQUE 4: ESTADO WEAR OS
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (createSessionSkateUiStateModel.detectedWearable != null)
                    MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (createSessionSkateUiStateModel.detectedWearable != null) Icons.Default.Watch else Icons.Default.WatchOff,
                    contentDescription = null,
                    tint = if (createSessionSkateUiStateModel.detectedWearable != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (createSessionSkateUiStateModel.detectedWearable != null) "${createSessionSkateUiStateModel.detectedWearable.name} Conectado" else "Smartwatch no detectado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (createSessionSkateUiStateModel.detectedWearable != null) {
                        Text(
                            text = if (createSessionSkateUiStateModel.detectedWearable.isWearOs) "Wear OS" else "Smartband / BLE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (createSessionSkateUiStateModel.detectedWearable != null)
                            "Podremos monitorizar tus métricas de frecuencia cardíaca durante la sesión."
                        else "La sesión se ejecutará únicamente desde el teléfono.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummarySectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun SummaryItemRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}