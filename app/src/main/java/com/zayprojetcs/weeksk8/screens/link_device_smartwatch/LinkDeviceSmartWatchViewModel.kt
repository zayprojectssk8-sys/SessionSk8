package com.zayprojetcs.weeksk8.screens.link_device_smartwatch

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.LinkDeviceSmartWatchUiState
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.model.LinkDeviceSmartWatchUiStateModel
import com.zayprojetcs.weeksk8.utils.WearableDetector
import com.zayprojetcs.weeksk8.utils.isBluetoothOff
import com.zaysk8.core.utils.CAPABILITY_CLIENT_NAME
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LinkDeviceSmartWatchViewModel(application: Application) : AndroidViewModel(application),
    MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {
    val wearableDetector by lazy { WearableDetector(application.applicationContext) }

    val dataStoreAppManager by lazy { DataStoreAppManager(application) }

    init {
        validateDeviceConnect()
    }

    private fun validateDeviceConnect() {
        viewModelScope.launch {
            val device = dataStoreAppManager.deviceConnectBluetooth.firstOrNull()
            device?.let { itDevice ->
                _linkDeviceSmartWatchUiState.update { itUpdate ->
                    itUpdate.copy(
                        detectedWearableState = DetectedWearableState.DeviceConnected(itDevice)
                    )
                }
            }
        }
    }

    private val _linkDeviceSmartWatchUiState = MutableStateFlow(LinkDeviceSmartWatchUiStateModel())
    val linkDeviceSmartWatchUiState: StateFlow<LinkDeviceSmartWatchUiStateModel> =
        _linkDeviceSmartWatchUiState.asStateFlow()
    /*val linkDeviceSmartWatchUiState: StateFlow<LinkDeviceSmartWatchUiStateModel> = combine(
        dataStore.deviceConnectBluetooth,
        _linkDeviceSmartWatchUiState
    ) { connectedDevice, linkDeviceSmartWatchUiState ->
        linkDeviceSmartWatchUiState.copy(
            detectedWearableState = if (connectedDevice != null)
                DetectedWearableState.DeviceConnected(connectedDevice)
            else linkDeviceSmartWatchUiState.detectedWearableState,
            connectDevice = connectedDevice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LinkDeviceSmartWatchUiStateModel()
    )*/

    private var messageClient: MessageClient? = null
    private var capabilityClient: CapabilityClient? = null


    fun loadEvent(event: LinkDeviceSmartWatchUiState) {
        when (event) {
            is LinkDeviceSmartWatchUiState.SetPermissionBluetooth -> event.setPermissionBluetooth()
            LinkDeviceSmartWatchUiState.DismissDialog -> dismissDialog()
            is LinkDeviceSmartWatchUiState.OnDeviceFound -> event.onDeviceFound()
            LinkDeviceSmartWatchUiState.OnUserApprovedConnection -> onUserApprovedConnection()
            LinkDeviceSmartWatchUiState.StartListeningForWatch -> startListeningForWatch()
            LinkDeviceSmartWatchUiState.StopListening -> stopListening()
            LinkDeviceSmartWatchUiState.ValidateWearableDetector -> validateWearableDetector()
            is LinkDeviceSmartWatchUiState.OnDissociateDevice -> event.onDissociateDevice()
        }
    }

    private fun LinkDeviceSmartWatchUiState.OnDissociateDevice.onDissociateDevice() {
        viewModelScope.launch {

            val isUnpaired = wearableDetector.unpairWearable(detectedWearable?.nodeId!!)
            Log.wtf(
                "javaClass.simpleName",
                "OnDissociateDevice: isUnpaired ${isUnpaired}"
            )
            if (isUnpaired) {
                // Éxito: Eliminar el reloj guardado en SharedPreferences/DataStore/Room
                dataStoreAppManager.clearDeviceConnectBluetooth()
                _linkDeviceSmartWatchUiState.update { itUpdate ->
                    itUpdate.copy(
                        detectedWearableState = DetectedWearableState.Idle
                    )
                }
            }
        }
    }

    private fun LinkDeviceSmartWatchUiState.SetPermissionBluetooth.setPermissionBluetooth() {
        Log.wtf(
            "javaClass.simpleName",
            "detectedWearableState: SetPermissionBluetooth ${isGranted}"
        )
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                isPermissionBluetoothGranted = isGranted
            )
        }
    }

    private fun validateWearableDetector() {
        if (application.isBluetoothOff()) {
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    detectedWearableState = DetectedWearableState.Disconnected("El Bluetooth del celular se ha desactivado.")
                )
            }
            return
        }
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                detectedWearableState = DetectedWearableState.ScanningDevices
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            delay(5000.milliseconds)

            if (ActivityCompat.checkSelfPermission(
                    getApplication(), Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                _linkDeviceSmartWatchUiState.update { itUpdate ->
                    itUpdate.copy(
                        wearables = wearableDetector.getBondedWearables()
                    )
                }
            }

        }
    }


    private fun startListeningForWatch() {

        messageClient = Wearable.getMessageClient(getApplication()).also {
            it.addListener(this)
        }
        capabilityClient = Wearable.getCapabilityClient(getApplication()).also {
            it.addListener(this, CAPABILITY_CLIENT_NAME)
        }

        // Registrar BroadcastReceiver (para cuando apagas el Bluetooth en el celular)
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        (getApplication() as? Context)?.registerReceiver(bluetoothStateReceiver, filter)
    }

    private fun stopListening() {
        messageClient?.removeListener(this)
        capabilityClient?.removeListener(this)
        try {
            (getApplication() as? Context)?.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver no estaba registrado
        }
    }

    // 3. Listener en tiempo real de cambios de capacidad (Conexión / Desconexión)
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        val isWatchConnected = capabilityInfo.nodes.isNotEmpty()

        if (!isWatchConnected) {
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    detectedWearableState = DetectedWearableState.Disconnected(
                        "El reloj Wear OS se ha desconectado o está fuera de rango."
                    )
                )
            }
        }
    }

    // Paso 1: Dispositivo detectado
    private fun LinkDeviceSmartWatchUiState.OnDeviceFound.onDeviceFound() {
        val stateDetectedWearable =
            if (detectedWearable != null) DetectedWearableState.DeviceDetected(detectedWearable) else DetectedWearableState.EmptyDevices
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                detectedWearableState = stateDetectedWearable
            )
        }
    }

    // Paso 2: Usuario presiona "Permitir / Conectar"
    private fun onUserApprovedConnection() {
        val detectedWearableState = linkDeviceSmartWatchUiState.value.detectedWearableState

        val currentState = detectedWearableState as? DetectedWearableState.DeviceDetected ?: return
        val device = currentState.device

        if (!device.isWearOs) {
            viewModelScope.launch {
                Log.wtf(javaClass.simpleName, "combinsssSSe  device $device")

                if (device.nodeId != null) {
                    // 1. Guardar la vinculación persistentemente
                    dataStoreAppManager.saveDeviceConnectBluetooth(device)
                }
            }

            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    detectedWearableState = DetectedWearableState.Success(device)
                )
            }

            return
        }

        Log.wtf(javaClass.simpleName, "CONECTEEED APP isAppInstalled: ${device}")
        if (device.isAppInstalled) {

            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    currentDevice = device,
                    detectedWearableState = DetectedWearableState.AwaitingWatch(
                        device = device,
                        message = "Conectando... Abre la app en tu reloj si no inicia automáticamente."
                    )
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                wearableDetector.automaticOpenWearApp(device.nodeId!!)
            }
        } else {
            device.nodeId?.let { nodeId ->
                wearableDetector.promptInstallOnWear(nodeId)
                _linkDeviceSmartWatchUiState.update { itUpdate ->
                    itUpdate.copy(
                        currentDevice = device,
                        detectedWearableState = DetectedWearableState.AwaitingWatch(
                            device = device,
                            message = "Instalación enviada al reloj. Complétala y abre la app en tu reloj para finalizar."
                        )
                    )
                }
            }
        }
    }

    // Paso 3: Listener en tiempo real. Se gatilla cuando el reloj responde
    override fun onMessageReceived(messageEvent: com.google.android.gms.wearable.MessageEvent) {
        Log.wtf(javaClass.simpleName, "onMessageReceived ${messageEvent.sourceNodeId}")
        if (messageEvent.path == MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED) {


            val currentNode =
                linkDeviceSmartWatchUiState.value.currentDevice // El dispositivo que estabas vinculando
            Log.wtf(javaClass.simpleName, "CONECTEEED APP currentNode: ${currentNode}")

            Log.wtf(javaClass.simpleName, "onMessageReceived currentNode $currentNode")

            currentNode ?: return

            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    detectedWearableState = DetectedWearableState.Success(currentNode)
                )
            }

            viewModelScope.launch {
                if (currentNode.nodeId != null) {
                    // 1. Guardar la vinculación persistentemente
                   dataStoreAppManager.saveDeviceConnectBluetooth(currentNode)
                }
            }


        }
    }

    private fun dismissDialog() {
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                detectedWearableState = DetectedWearableState.Idle
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // 1. Receiver para detectar cuando se apaga/enciende el Bluetooth en el celular
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.wtf(
                "javaClass.simpleName",
                "detectedWearableState: onReceive intent.action "
            )
            if (linkDeviceSmartWatchUiState.value.currentDevice != null) return

            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                when (state) {
                    BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> _linkDeviceSmartWatchUiState.update { itUpdate ->
                        itUpdate.copy(
                            detectedWearableState = DetectedWearableState.Disconnected("El Bluetooth del celular se ha desactivado."),
                            isPermissionBluetoothGranted = false
                        )
                    }

                    BluetoothAdapter.STATE_ON, BluetoothAdapter.STATE_TURNING_ON -> {
                        Log.wtf(
                            "javaClass.simpleName",
                            "detectedWearableState: BluetoothAdapter STATE_ON "
                        )
                        loadEvent(
                            LinkDeviceSmartWatchUiState.SetPermissionBluetooth(true)
                        )
                    }

                }
            }
        }
    }
}