package com.zayprojetcs.weeksk8.screens.create_session_skate.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickSelectionMode
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickTrackingMode

@Composable
fun TrickSelectedSessionScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onTrickModeSelected: (TrickSelectionMode) -> Unit,
    onOpenTrickPickerClick: () -> Unit,
    onModeSelected: (TrickTrackingMode) -> Unit
) {
    // --- SECCIÓN 2: MODO DE TRUCOS ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Sports,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Objetivo de trucos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        TrickSelectionMode.entries.forEach { mode ->
            val isSelected = createSessionSkateUiStateModel.trickModeSession == mode
            OutlinedCard(
                onClick = { onTrickModeSelected(mode) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                        onClick = { onTrickModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Detalle interactivo según el modo
                        if (mode == TrickSelectionMode.CUSTOM && isSelected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AssistChip(
                                onClick = onOpenTrickPickerClick,
                                label = {
                                    Text(if (createSessionSkateUiStateModel.selectedCustomTricksCount > 0) "${createSessionSkateUiStateModel.selectedCustomTricksCount} trucos listados" else "Seleccionar trucos")
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                            )
                        } else if (mode == TrickSelectionMode.RANDOM_UNLOCKED && isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pool disponible: ${createSessionSkateUiStateModel.unlockedTricksCount} trucos desbloqueados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = createSessionSkateUiStateModel.trickModeSession != TrickSelectionMode.FREE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            TrickTrackingConfigScreen(
                createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                onModeSelected = onModeSelected
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickTrackingConfigScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onModeSelected: (TrickTrackingMode) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "¿Cómo quieres llevar la cuenta?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // --- SECCIÓN DE MODOS ---
        TrickTrackingMode.entries.forEach { mode ->
            val isSelected = createSessionSkateUiStateModel.trickTrackingMode == mode

            OutlinedCard(
                onClick = { onModeSelected(mode) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(enabled = isSelected),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

    }
}