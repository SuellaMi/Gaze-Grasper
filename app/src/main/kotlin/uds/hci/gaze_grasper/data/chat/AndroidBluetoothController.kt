package uds.hci.gaze_grasper.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uds.hci.gaze_grasper.domain.chat.BluetoothController
import uds.hci.gaze_grasper.domain.chat.BluetoothDeviceDomain
import uds.hci.gaze_grasper.domain.chat.BluetoothMessage
import uds.hci.gaze_grasper.domain.chat.ConnectionResult
import java.io.IOException
import java.util.*

/**
 * Implements bluetooth controller.
 * Combines all the relevant bluetooth logics.
 * contain the feature of scan and connect to devices and the server as well.
 */
@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {
    // Give a system service. A service provided from the android operating system.
    // initialised by lazy
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    // It is the hardware module. contains relevant functionalities such as mac adress, blutetooth name, but also
    // provide a list of scanned devices as an example.
    // initialised by lazy
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    // List of connection states. False by default.
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    // List of scanned devices. Empty by default
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    // List of paired devices. Empty by default
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    // List of error states thath is used.
    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    // value of the found device with the callback of the device that was found.
    // It adds the device to the list of the  found devices.
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    // Value which takes care about state changes of bluetoothconnections.
    // Instantiation of BluetoothStateReceiver. Updates boolean states.
    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    // Saves the value of socket for the server (and its status)
    private var currentServerSocket: BluetoothServerSocket? = null

    // Saves the socket for the client (and its status
    private var currentClientSocket: BluetoothSocket? = null

    // initialise the necessary aspects in the bluetooth controller.
    // Including updating the paired devices and the current
    // bluetooth state.
    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }


    // It starts to scan of bluetooth devices in the close environment and gives the founded to the update.
    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    // It stops the discovery of bluetooth devices in the close environment.
    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    // starts the bluetooth server, where devices can connect with.
    // Establish the connection to a bluetoothdevice.
    // From there it is possible to exchange data via Bluetooth Data TransferService.
    // Data Transfer Service exchange also Video Data.
    // Returns a flow of Connectionresults (flow is a reactive data structure)
    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(service.listenForIncomingMessages().map {
                        ConnectionResult.TransferSucceeded(it)
                    })

                    emitAll(service.listenForIncomingVideoMessages().map {
                        ConnectionResult.TransferVideoSucceeded(it)
                    })
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    // Function to connect to a device that have launched a server.
    // Establish the connection to a bluetoothdevice with a server.
    // From there it is possible to exchange data via Bluetooth Data TransferService.
    // Data Transfer Service exchange also Video Data.
    // Returns a flow of connectionresults
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }
                        )
                        emitAll(it.listenForIncomingVideoMessages().map { ConnectionResult.TransferVideoSucceeded(it) })
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    // Function which actually send the message in the bluetoothcontroller.
    // Triggers the data transfer service.
    // Takes the message we want to send and returns the successful sended Bluetooth message to show in UI later
    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if (dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )

        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    // Function that close the connection when someone disconnects
    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    // It clears everything from our bluetoothcontroller.
    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    // Updates always the paired devices which can be provided in the current moment
    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }


    // Helper function. It checks whether we have a certain permission or not.
    // Returns a boolean whether we have the permission or not
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // Definition of the UUID, which is needed for the connection (both devices need the same ID).
    // Will be used in StartBluetoothServer
    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}
