package uds.hci.gaze_grasper.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Combines all the relevant bluetooth logics.
 * contain the feature of scan and connect to devices and the server as well.
 */
interface BluetoothController {
    //all connection states
    val isConnected: StateFlow<Boolean>
    //all states of the scanned devices
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    //all states of the paired devices
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    //all error states
    val errors: SharedFlow<String>

    //start the discovery in our environment
    fun startDiscovery()
    //stop the discovery in our environment
    fun stopDiscovery()

    //starts the bluetooth server, where devices can connect with.
    // Returns a flow of Connectionresults (flow is a reactive data structure)
    fun startBluetoothServer(): Flow<ConnectionResult>

    //Function to connect to a device that have launched a server.
    //Returns a flow of connectionresults
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>


    // Function which actually send the message in the bluetoothcontroller.
    // Triggers the data transfer service
    suspend fun trySendMessage(message: String): BluetoothMessage?

    //Function that close the connection when someone disconnects
    fun closeConnection()

    //frees up all the memories and the resources to all devices
    fun release()
}