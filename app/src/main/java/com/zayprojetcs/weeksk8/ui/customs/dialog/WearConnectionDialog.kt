package com.zayprojetcs.weeksk8.ui.customs.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.screens.menu.ui_state.MenuUiState
import com.zayprojetcs.weeksk8.utils.DetectedWearable
/*
@Composable
fun WearConnectionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state is DetectedWearableState.Idle) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (state is DetectedWearableState.DeviceDetected) {
                Button(onClick = onConfirm) {
                    Text("Permitir y Conectar")
                }
            } else if (state is DetectedWearableState.Success) {
                Button(onClick = onDismiss) {
                    Text("Entendido")
                }
            }
        },
        dismissButton = {
            if (state is DetectedWearableState.DeviceDetected) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        },
        title = {
            Text(
                text = when (state) {
                    is DetectedWearableState.DeviceDetected -> "Dispositivo Detectado"
                    is DetectedWearableState.AwaitingWatch -> "Vinculando Reloj"
                    is DetectedWearableState.Success -> "¡Conexión Exitosa!"
                    else -> ""
                }
            )
        },
        text = {
            when (state) {
                is DetectedWearableState.DeviceDetected -> {
                    val device = state.device
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Se ha encontrado un nuevo hardware disponible:")
                        HorizontalDivider()

                        DetailRow(label = "Nombre:", value = device.name)
                        DetailRow(label = "Marca:", value = device.brand.name)
                        DetailRow(
                            label = "Sistema OS:",
                            value = if (device.isWearOs) "Wear OS" else "Sistema Propietario / Band"
                        )
                        DetailRow(
                            label = "Estado App:",
                            value = if (device.isAppInstalled) "Instalada" else "No instalada en reloj"
                        )
                    }
                }

                is DetectedWearableState.AwaitingWatch -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                is DetectedWearableState.Success -> {
                    Text("La app del reloj se ha comunicado con el celular correctamente. Todo está listo para sincronizar tus sesiones de patinaje.")
                }

                is DetectedWearableState.Disconnected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }
    )
}*/

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}