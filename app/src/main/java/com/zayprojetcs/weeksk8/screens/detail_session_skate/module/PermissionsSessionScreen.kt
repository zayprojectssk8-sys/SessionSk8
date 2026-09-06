package com.zayprojetcs.weeksk8.screens.detail_session_skate.module


import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zayprojetcs.weeksk8.utils.getRequiredSessionPermissions
import com.zayprojetcs.weeksk8.utils.getRequiredSessionPermissionsGranted

@Composable
fun PermissionsSessionScreen(
    onAllPermissionsGranted: () -> Unit,
    onSkipOrCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Launcher para solicitar los permisos múltiples
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        Log.wtf("", "PERSMISOSOSO permisos 45 -")

        val allGranted = context.getRequiredSessionPermissions().all { permission ->
            Log.wtf("JAVASCOOO", " PERSMISOSOSO $permission - ${ permissionsResult[permission]}")
            permissionsResult[permission] == true ||
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onAllPermissionsGranted()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sección Explicativa
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Permisos necesarios",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Para que el seguimiento de tu sesión de patinaje funcione correctamente en tu teléfono y reloj, necesitamos los siguientes accesos:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 1. Actividad física
                PermissionInfoItem(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    title = "Actividad física",
                    description = "Mide tu movimiento durante la sesión para detectar fases activas, descansos y rondas."
                )

                // 2. Notificaciones
                PermissionInfoItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    description = "Mantiene el cronómetro visible en segundo plano y te alerta sobre el inicio/fin de cada ronda."
                )

                // 3. Sensores corporales
                PermissionInfoItem(
                    icon = Icons.Default.Favorite,
                    title = "Sensores corporales",
                    description = "Monitorea tu frecuencia cardíaca y calorías quemadas desde el reloj durante el entrenamiento."
                )
            }

            // Botones de Acción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (context.getRequiredSessionPermissionsGranted()) {
                            onAllPermissionsGranted()
                        } else {
                            permissionLauncher.launch(context.getRequiredSessionPermissions())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Conceder permisos", style = MaterialTheme.typography.titleMedium)
                }

                if (onSkipOrCancel != null) {
                    TextButton(
                        onClick = onSkipOrCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ahora no", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionInfoItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

