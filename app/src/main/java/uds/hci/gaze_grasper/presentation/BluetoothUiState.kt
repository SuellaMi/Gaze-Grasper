package uds.hci.gaze_grasper.presentation

import uds.hci.gaze_grasper.domain.chat.BluetoothDevice
import uds.hci.gaze_grasper.domain.chat.BluetoothMessage

/**
 * It handles the states of the informations in terms of Bluetooth. Will be used for the UI
 */
data class BluetoothUiState(
    // State of devices. Empty by default
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    // State of connection. False by default
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    // State of the Error messages. Null by default
    val errorMessage: String? = null,
    // State of messages. Empty by default
    val messages: List<BluetoothMessage> = emptyList()
)
