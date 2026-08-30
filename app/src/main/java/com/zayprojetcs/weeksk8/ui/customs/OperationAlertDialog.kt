package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zaysk8.core.utils.OperationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun <T> LoadOperationResult(
    operationResult: SharedFlow<OperationResult<T>>,
    onConfirm: (() -> Unit)?,
    onRetry: (() -> Unit)?
) {
    var resultState by remember { mutableStateOf<OperationResult<T>?>(null) }

// Escuchar eventos del SharedFlow
    LaunchedEffect(Unit) {
        operationResult.collect { result ->
            resultState = result
        }
    }

// Renderizado según el estado actual
    when (val state = resultState) {
        is OperationResult.Loading -> {
            LoadingAlertDialog(message = "Guardando sesión de skate...")
        }

        is OperationResult.Success -> {
            SuccessAlertDialog(
                onConfirm = {
                    resultState = null
                    onConfirm?.invoke()
                }
            )
        }

        is OperationResult.Error -> {
            ErrorAlertDialog(
                description = state.message,
                onConfirm = {
                    resultState = null
                },
                onRetry = onRetry
            )
        }

        null -> {}
    }
}

@Composable
fun SuccessAlertDialog(
    onConfirm: () -> Unit
) {
    // Cierre automático opcional después de la animación de confirmación
    LaunchedEffect(Unit) {
        delay(1200L.milliseconds)
        onConfirm()
    }

    Dialog(
        onDismissRequest = onConfirm
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorAlertDialog(
    title: String = "Error al guardar",
    description: String,
    confirmButtonText: String = "Entendido",
    onConfirm: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            onRetry?.let { retryAction ->
                TextButton(onClick = retryAction) {
                    Text("Reintentar")
                }
            }
        }
    )
}

@Composable
fun LoadingAlertDialog(
    message: String = "Guardando sesión en Room..."
) {
    Dialog(
        onDismissRequest = { /* Deshabilitar cierre al tocar fuera */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}