package uds.hci.gaze_grasper.presentation

import uds.hci.gaze_grasper.domain.chat.BluetoothDevice
import uds.hci.gaze_grasper.domain.chat.BluetoothMessage

/**
 * It handles the states of the informations in terms of Bluetooth. Will be used for the UI
 */
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList()
)
