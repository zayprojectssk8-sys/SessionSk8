package com.zayprojetcs.weeksk8.screens.link_device_smartwatch


import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.LinkDeviceSmartWatchUiState
import com.zayprojetcs.weeksk8.utils.DetectedWearable
import com.zayprojetcs.weeksk8.utils.getRequiredBluetoothPermissionsGranted

sealed interface DetectedWearableState {
    data object Idle : DetectedWearableState
    data class DeviceDetected(val device: DetectedWearable) : DetectedWearableState
    data class DeviceConnected(val device: DetectedWearable) : DetectedWearableState
    data class AwaitingWatch(val device: DetectedWearable, val message: String) :
        DetectedWearableState

    data object ScanningDevices : DetectedWearableState
    data object EmptyDevices : DetectedWearableState
    data class Success(val device: DetectedWearable) : DetectedWearableState
    data class Disconnected(val reason: String) : DetectedWearableState
}


@Composable
fun LinkDeviceSmartWatchScreen(
    onFinished: () -> Unit,
    viewModel: LinkDeviceSmartWatchViewModel = viewModel()
) {
    val linkDeviceSmartWatchUiState by viewModel.linkDeviceSmartWatchUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.loadEvent(LinkDeviceSmartWatchUiState.SetPermissionBluetooth(isGranted))
    }

    // 2. Iniciar y detener el listener de mensajes del reloj según el ciclo de vida de la UI
    DisposableEffect(linkDeviceSmartWatchUiState.isPermissionBluetoothGranted) {

        if (linkDeviceSmartWatchUiState.isPermissionBluetoothGranted) {
            viewModel.loadEvent(LinkDeviceSmartWatchUiState.ValidateWearableDetector)
            viewModel.loadEvent(LinkDeviceSmartWatchUiState.StartListeningForWatch)
        }
        onDispose {
            viewModel.loadEvent(LinkDeviceSmartWatchUiState.StopListening)
        }
    }

    LaunchedEffect(linkDeviceSmartWatchUiState.wearables.isNotEmpty()) {
        val mainWearable = linkDeviceSmartWatchUiState.wearables.firstOrNull()
        if (mainWearable != null) {
            viewModel.loadEvent(LinkDeviceSmartWatchUiState.OnDeviceFound(mainWearable))
        }
    }


    // --- UI Principal de tu App ---
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // CONTENIDO CENTRAL (Icono + Título + Descripción/Tarjeta)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = context.getRequiredBluetoothPermissionsGranted(),
                    label = "ScreenStateAnimation"
                ) { permissionGranted ->
                    if (!permissionGranted) {
                        // ESTADO 1: Sin permisos concedidos
                        InitialPermissionContent(onSetLauncherPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            } else {
                                viewModel.loadEvent(
                                    LinkDeviceSmartWatchUiState.SetPermissionBluetooth(true)
                                )
                            }
                        })
                    } else {
                        // ESTADO 2: Con permisos (Escaneando / Vacío / Dispositivo Encontrado)
                        DeviceSearchContent(
                            state = linkDeviceSmartWatchUiState.detectedWearableState,
                            onConnect = {
                                viewModel.loadEvent(LinkDeviceSmartWatchUiState.OnUserApprovedConnection)
                            },
                            onFinished = onFinished,
                            onDisassociateDevice = {
                                viewModel.loadEvent(
                                    LinkDeviceSmartWatchUiState.OnDissociateDevice(it)
                                )
                            },
                            onSearchDevices = {
                                viewModel.loadEvent(
                                    LinkDeviceSmartWatchUiState.SetPermissionBluetooth(true)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun DeviceSearchContent(
    state: DetectedWearableState,
    onSearchDevices: () -> Unit,
    onConnect: (DetectedWearable) -> Unit,
    onDisassociateDevice: (DetectedWearable) -> Unit,
    onFinished: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        /*val textTitle = when (state) {
            is DetectedWearableState.DeviceDetected -> "Dispositivo detectado"
            DetectedWearableState.EmptyDevices -> "No se encontraron dispositivos"
            DetectedWearableState.ScanningDevices -> "Buscando dispositivos"
            is DetectedWearableState.AwaitingWatch -> "Vinculando Reloj"
            is DetectedWearableState.Disconnected -> "Bluetooth desconectado"
            DetectedWearableState.Idle -> ""
            is DetectedWearableState.Success -> "¡Conexión Exitosa!"
            is DetectedWearableState.DeviceConnected -> "Dispositivo conectado"
        }
        Text(
            text = textTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))*/
        when (state) {

            DetectedWearableState.Idle -> InitialPermissionContent(
                textButton = "Buscar dispositivos",
                onSetLauncherPermission = onSearchDevices
            )

            DetectedWearableState.EmptyDevices -> LoadEmptyDevicesScreen(onSearchDevices = onSearchDevices)
            DetectedWearableState.ScanningDevices -> LoadScanningDevicesScreen()
            is DetectedWearableState.Disconnected -> LoadDisconnectedScreen(reason = state.reason)
            is DetectedWearableState.DeviceDetected -> LoadDeviceDetectedScreen(
                device = state.device,
                onConnect = onConnect
            )


            is DetectedWearableState.AwaitingWatch -> LoadAwaitingWatchScreen(
                device = state.device,
                message = state.message
            )


            is DetectedWearableState.Success -> LoadSuccessParingDeviceScreen(
                device = state.device,
                onFinish = onFinished
            )

            is DetectedWearableState.DeviceConnected -> LoadConnectedDeviceScreen(
                device = state.device,
                onDisassociateDevice = onDisassociateDevice
            )
        }

    }
}

@Composable
fun LoadDisconnectedScreen(reason: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BluetoothDisabled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = reason,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

    }

}

@Composable
fun LoadSuccessParingDeviceScreen(device: DetectedWearable, onFinish: () -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BluetoothConnected,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dispositivo conectado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "La app del reloj se ha comunicado con el celular correctamente. Todo está listo para sincronizar tus sesiones de patinaje.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoadCardDeviceDetected(
            device = device,
            onConnect = null
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "SALIR")
        }
    }


}

@Composable
fun LoadConnectedDeviceScreen(
    device: DetectedWearable,
    onDisassociateDevice: (DetectedWearable) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BluetoothConnected,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dispositivo conectado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoadCardDeviceDetected(
            device = device,
            textButton = "Desvincular",
            onConnect = {
                onDisassociateDevice(it)
            }
        )
    }

}

@Composable
fun LoadAwaitingWatchScreen(device: DetectedWearable, message: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                strokeWidth = 4.dp
            )
            Icon(
                imageVector = Icons.Outlined.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Conectando...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoadCardDeviceDetected(
            device = device,
            onConnect = null
        )
    }
}

@Composable
fun LoadEmptyDevicesScreen(onSearchDevices: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.WatchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No se encontraron dispositivos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Asegúrate de que el reloj esté encendido, con el Bluetooth activo y cerca del teléfono.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSearchDevices,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Buscar dispositivos")
        }
    }

}

@Composable
fun LoadScanningDevicesScreen() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                strokeWidth = 4.dp
            )
            Icon(
                imageVector = Icons.Outlined.BluetoothSearching,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Buscando relojes cercanos...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun LoadDeviceDetectedScreen(
    device: DetectedWearable,
    textButton: String = "Conectar",
    onConnect: ((DetectedWearable) -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dispositivo detectado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoadCardDeviceDetected(device = device, textButton = textButton, onConnect = onConnect)


    }

}

@Composable
fun LoadCardDeviceDetected(
    device: DetectedWearable,
    textButton: String = "Conectar",
    onConnect: ((DetectedWearable) -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Watch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.getTypeWearable(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onConnect != null) {

                Button(onClick = { onConnect(device) }) {
                    Text(textButton)
                }
            }
        }
    }
}

@Composable
private fun InitialPermissionContent(
    textButton: String = "Conceder permisos de Bluetooth",
    onSetLauncherPermission: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Watch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Vincular Smartwatch",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Conecta tu reloj Wear OS o Smartband para monitorear tu frecuencia cardíaca, registrar impactos y sincronizar tus métricas durante tus sesiones de skate.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSetLauncherPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = textButton)
        }
    }
}

