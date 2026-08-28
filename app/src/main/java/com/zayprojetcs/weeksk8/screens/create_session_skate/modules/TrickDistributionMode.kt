package com.zayprojetcs.weeksk8.screens.create_session_skate.modules


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickDistributionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickOrderMode
import com.zaysk8.core.model.TFMTrick


data class TrickRoundAssignment(
    val trickName: String,
    val assignedRounds: Int,
    val hasExtraMargin: Boolean = false,
    val extraMarginMinutes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionTrickDistributionScreen(
    uiStateModel: CreateSessionSkateUiStateModel,
    onDistributionModeSelected: (TrickDistributionMode) -> Unit,
    onOrderModeSelected: (TrickOrderMode) -> Unit
) {

    // Cálculo de la distribución por truco
    val distributionAssignments = remember(
        uiStateModel.calculatedRounds,
        uiStateModel.selectedTrickList,
        uiStateModel.marginMinutes
    ) {
        calculateTrickRoundsDistribution(
            tricks = uiStateModel.selectedTrickList,
            totalRounds = uiStateModel.calculatedRounds,
            marginMinutes = uiStateModel.marginMinutes ?: 0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Modo de práctica",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        TrickDistributionMode.entries.forEach { mode ->
            val isSelected = uiStateModel.trickDistributionMode == mode

            OutlinedCard(
                onClick = { onDistributionModeSelected(mode) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(enabled = isSelected),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onDistributionModeSelected(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiStateModel.durationSessionMinutes != null && mode == TrickDistributionMode.ROUNDS_PER_TRICK && uiStateModel.trickDistributionMode == TrickDistributionMode.ROUNDS_PER_TRICK && uiStateModel.selectedTrickList.isNotEmpty()) {
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Distribución estimada por truco (${uiStateModel.calculatedRounds} rondas)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                distributionAssignments.forEach { assignment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = assignment.trickName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${assignment.assignedRounds} ronda${if (assignment.assignedRounds > 1) "s" else ""}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (assignment.hasExtraMargin) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                ) {
                                                    Text(
                                                        text = "+${assignment.extraMarginMinutes}m extra",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onTertiary,
                                                        modifier = Modifier.padding(
                                                            horizontal = 4.dp,
                                                            vertical = 2.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if ((uiStateModel.marginMinutes ?: 0) > 0) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "* El tiempo extra (${uiStateModel.marginMinutes} min) fue asignado al truco con menor cantidad de rondas.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }

        // --- DESGLOSE CÁLCULO DE RONDAS POR TRUCO (MODO A) ---

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- SECCIÓN 2: MODO DE ORDENAMIENTO DE TRUCOS ---
        Text(
            text = "Orden de los trucos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        TrickOrderMode.entries.forEach { mode ->
            val isSelected = uiStateModel.trickOrderMode == mode

            OutlinedCard(
                onClick = { onOrderModeSelected(mode) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(enabled = isSelected),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onOrderModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Función que distribuye las rondas entre la lista de trucos.
 * Si existe margen de tiempo extra, se le asigna una ronda con ese tiempo al truco con menos rondas.
 */
fun calculateTrickRoundsDistribution(
    tricks: List<TFMTrick>,
    totalRounds: Int,
    marginMinutes: Int
): List<TrickRoundAssignment> {
    if (tricks.isEmpty() || totalRounds <= 0) return emptyList()

    val baseRoundsPerTrick = totalRounds / tricks.size
    val remainderRounds = totalRounds % tricks.size

    // Inicializar asignación base
    val assignments = tricks.mapIndexed { index, trick ->
        val extraRound = if (index < remainderRounds) 1 else 0
        TrickRoundAssignment(
            trickName = trick.akaName,
            assignedRounds = baseRoundsPerTrick + extraRound
        )
    }.toMutableList()

    // Si hay un margen de tiempo sobrante, se le asigna al truco con menor número de rondas
    if (marginMinutes > 0) {
        val minRounds = assignments.minOf { it.assignedRounds }
        val targetIndex = assignments.indexOfLast { it.assignedRounds == minRounds }

        if (targetIndex != -1) {
            val currentTarget = assignments[targetIndex]
            assignments[targetIndex] = currentTarget.copy(
                hasExtraMargin = true,
                extraMarginMinutes = marginMinutes
            )
        }
    }

    return assignments
}