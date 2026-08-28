package com.zayprojetcs.weeksk8.screens.create_session_skate.modules

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.CreateSessionSkateUiState
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TrickSelectionMode
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.unit.sp
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TypeConfig
import com.zayprojetcs.weeksk8.ui.customs.LoadTitleSectionCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom
import com.zayprojetcs.weeksk8.ui.customs.view.SelectTrickScreen
import com.zaysk8.core.model.TFMTrick
import com.zaysk8.core.model.TypeStanceTrick
import com.zaysk8.core.model.TypeTrick
import com.zaysk8.core.utils.getFormatDurationSession
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSessionSkateConfigScreen(
    createSessionSkateUiStateModel: CreateSessionSkateUiStateModel,
    onDurationSelected: (Int?) -> Unit,
    onWarmupEnabledChanged: (Boolean) -> Unit,
    onWarmupMinutesChanged: (Int) -> Unit,
    onCooldownEnabledChanged: (Boolean) -> Unit,
    onCooldownMinutesChanged: (Int) -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            DurationSliderSelectorSessionScreen(
                selectedDurationMinutes = createSessionSkateUiStateModel.durationSessionMinutes,
                onDurationSelected = onDurationSelected
            )

            Divider()

            WarmupCooldownConfigScreen(
                createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                onWarmupEnabledChanged = onWarmupEnabledChanged,
                onWarmupMinutesChanged = onWarmupMinutesChanged,
                onCooldownEnabledChanged = onCooldownEnabledChanged,
                onCooldownMinutesChanged = onCooldownMinutesChanged
            )

            /*TrickSelectedSessionScreen(
                createSessionSkateUiStateModel = createSessionSkateUiStateModel,
                onTrickModeSelected = onTrickModeSelected,
                onOpenTrickPickerClick = onOpenTrickPickerClick
            )*/

        }


    }
}


@Composable
fun DurationSliderSelectorSessionScreen(
    selectedDurationMinutes: Int?, // null o 0 representa 'Sesión abierta'
    onDurationSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0f representa sesión abierta
    val currentSliderValue = (selectedDurationMinutes ?: 0).toFloat()

    // Formateo dinámico del texto de duración
    val durationText = getFormatDurationSession(currentSliderValue.toInt())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Duración de la sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Etiqueta destacada con el tiempo actual
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Slider discreto: Rango 0 a 300 min con 9 marcas intermedias (saltos de 30 min)
        Slider(
            value = currentSliderValue,
            onValueChange = { newValue ->
                val stepValue = (newValue / 30f).roundToInt() * 30
                val finalMinutes = if (stepValue == 0) null else stepValue
                onDurationSelected(finalMinutes)
            },
            valueRange = 0f..300f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )

        // Leyendas de límites (Extremos)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 (Abierta)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "2.5 h",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "5 h (Máximo)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(
            visible = (selectedDurationMinutes ?: 0) >= 150
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sesión alta en impacto. Recuerda hidratarte periódicamente y descansar para prevenir torceduras por fatiga muscular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}


