package uds.hci.gaze_grasper.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Combines all the relevant bluetooth logics.
 * contain the feature of scan and connect to devices and the server as well.
 */
interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    //all states of the scanned devices
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    //all states of the paired devices
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>

    //start the discovery in our environment
    fun startDiscovery()
    //stop the discovery in our environment
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    suspend fun trySendMessage(message: String): BluetoothMessage?

    fun closeConnection()

    //frees up all the memories and the resources to all devices
    fun release()
}