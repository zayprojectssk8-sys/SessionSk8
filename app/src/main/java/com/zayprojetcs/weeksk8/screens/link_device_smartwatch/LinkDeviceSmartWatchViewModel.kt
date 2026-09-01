package com.zayprojetcs.weeksk8.screens.link_device_smartwatch

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageEvent
import com.zayprojetcs.weeksk8.core.data_store.DataStoreAppManager
import com.zayprojetcs.weeksk8.core.helper.BluetoothReceiverHelper
import com.zayprojetcs.weeksk8.core.helper.NodeClientAppHelper
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.LinkDeviceSmartWatchUiState
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.model.DetectedWearableState
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.ui_state.model.LinkDeviceSmartWatchUiStateModel
import com.zayprojetcs.weeksk8.utils.getRequiredBluetoothPermissionsGranted
import com.zayprojetcs.weeksk8.utils.isBluetoothOff
import com.zaysk8.core.utils.MESSAGE_PATH_WEAR_TO_PHONE_COMMUNICATION_ESTABLISHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LinkDeviceSmartWatchViewModel(application: Application) : AndroidViewModel(application) {
    val nodeClientAppHelper by lazy { NodeClientAppHelper(application.applicationContext) }

    val dataStoreAppManager by lazy { DataStoreAppManager(application) }
    val bluetoothReceiverHelper by lazy { BluetoothReceiverHelper(application) }

    private val _linkDeviceSmartWatchUiState = MutableStateFlow(LinkDeviceSmartWatchUiStateModel())
    val linkDeviceSmartWatchUiState: StateFlow<LinkDeviceSmartWatchUiStateModel> =
        _linkDeviceSmartWatchUiState.asStateFlow()

    init {
        validateDeviceConnect()
        observeBluetoothChanges()
    }

    private fun observeBluetoothChanges() {
        bluetoothReceiverHelper.observeBluetoothState()
            .onEach { isConnected ->
                // Salir si ya hay un dispositivo seleccionado/conectado
                if (_linkDeviceSmartWatchUiState.value.currentDevice != null) return@onEach
                if (!application.getRequiredBluetoothPermissionsGranted()) return@onEach

                if (isConnected) {
                    Log.wtf(
                        this::class.java.simpleName,
                        "detectedWearableState: BluetoothAdapter STATE_ON"
                    )
                    if (linkDeviceSmartWatchUiState.value.detectedWearableState is DetectedWearableState.Disconnected) {
                        _linkDeviceSmartWatchUiState.update { itUpdate ->
                            itUpdate.copy(
                                detectedWearableState = DetectedWearableState.Idle
                            )
                        }
                    }
                } else {
                    _linkDeviceSmartWatchUiState.update { itUpdate ->
                        itUpdate.copy(
                            detectedWearableState = DetectedWearableState.Disconnected("El Bluetooth del celular se ha desactivado."),
                            startScannerDevices = false,
                        )
                    }
                }
            }
            .launchIn(viewModelScope) // Se encarga de recolectar el Flow mientras el ViewModel viva
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

    fun loadEvent(event: LinkDeviceSmartWatchUiState) {
        when (event) {
            is LinkDeviceSmartWatchUiState.SetPermissionBluetooth -> event.setPermissionBluetooth()
            LinkDeviceSmartWatchUiState.DismissDialog -> dismissDialog()
            is LinkDeviceSmartWatchUiState.OnDeviceFound -> event.onDeviceFound()
            LinkDeviceSmartWatchUiState.OnUserApprovedConnection -> onUserApprovedConnection()
            LinkDeviceSmartWatchUiState.StartListeningForWatch -> startListeningForWatch()
            //LinkDeviceSmartWatchUiState.StopListening -> stopListening()
            LinkDeviceSmartWatchUiState.ValidateWearableDetector -> validateWearableDetector()
            is LinkDeviceSmartWatchUiState.OnDissociateDevice -> event.onDissociateDevice()
            is LinkDeviceSmartWatchUiState.SetScannerDevices -> event.setScannerDevices()
        }
    }

    private fun LinkDeviceSmartWatchUiState.SetScannerDevices.setScannerDevices() {
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                startScannerDevices = isScanner
            )
        }
    }

    private fun LinkDeviceSmartWatchUiState.OnDissociateDevice.onDissociateDevice() {
        viewModelScope.launch {

            nodeClientAppHelper.sendConnectionUnpair(deviceWearable?.nodeId!!)
            /*val isUnpaired = nodeClientAppHelper.unpairWearable(deviceWearable?.nodeId!!)
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
            }*/
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
            _linkDeviceSmartWatchUiState.update { itUpdate ->
                itUpdate.copy(
                    wearables = nodeClientAppHelper.getBondedWearables()
                )
            }
        }
    }


    private fun startListeningForWatch() {
        observeMessages()
        observeCapabilities()
    }

    private fun observeMessages() {
        nodeClientAppHelper.observeMessages()
            .onEach { messageEvent ->
                onMessageReceivedHelper(messageEvent)
            }
            .launchIn(viewModelScope) // Garantiza cancelación automática cuando el ViewModel muere
    }

    private fun observeCapabilities() {
        nodeClientAppHelper.observeCapabilityChanges()
            .onEach { capabilityInfo ->
                // Actualizar lista de reloj(es) disponible(s)
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
            .launchIn(viewModelScope)
    }


    // Paso 1: Dispositivo detectado
    private fun LinkDeviceSmartWatchUiState.OnDeviceFound.onDeviceFound() {
        val stateDetectedWearable =
            if (deviceWearable != null) DetectedWearableState.DeviceDetected(deviceWearable) else DetectedWearableState.EmptyDevices
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
                nodeClientAppHelper.automaticOpenWearApp(device.nodeId!!)
            }
        } else {
            device.nodeId?.let { nodeId ->
                nodeClientAppHelper.promptInstallOnWear(nodeId)
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

    private fun dismissDialog() {
        _linkDeviceSmartWatchUiState.update { itUpdate ->
            itUpdate.copy(
                detectedWearableState = DetectedWearableState.Idle
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
    }


    // Paso 3: Listener en tiempo real. Se gatilla cuando el reloj responde
    fun onMessageReceivedHelper(messageEvent: MessageEvent) {

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
}
