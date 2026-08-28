package com.sk8.appwatch.presentation.screen.link_device_phone

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.sk8.appwatch.presentation.utils.getRequiredWearOsPermissions

@Composable
fun WearPermissionRationaleScreen(
    onGrantPermissionsClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(Color.Black),
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "Permisos de Sesión",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = "Para registrar tu sesión de skate necesitamos acceder a:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                PermissionItem(
                    title = "Sensores del cuerpo",
                    desc = "Medir BPM y calorías"
                )
            }

            item {
                PermissionItem(
                    title = "Actividad física",
                    desc = "Contar caídas e impactos"
                )
            }

            item {
                PermissionItem(
                    title = "Ubicación GPS",
                    desc = "Velocidad y distancia recorrida"
                )
            }

            item {
                CompactChip(
                    onClick = onGrantPermissionsClick,
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Otorgar Permisos",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = desc,
            fontSize = 9.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LinkDevicePhoneScreen(onSuccessPermission: () -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onSuccessPermission()
        }
    }

    WearPermissionRationaleScreen(onGrantPermissionsClick = {
        permissionLauncher.launch(getRequiredWearOsPermissions())
    })

}