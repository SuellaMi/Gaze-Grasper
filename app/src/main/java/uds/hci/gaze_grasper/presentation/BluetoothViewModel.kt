package uds.hci.gaze_grasper.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uds.hci.gaze_grasper.domain.chat.BluetoothController
import uds.hci.gaze_grasper.domain.chat.BluetoothDeviceDomain
import uds.hci.gaze_grasper.domain.chat.ConnectionResult
import javax.inject.Inject

/**
 * This class takes the information of the bluetoothcontroller and handles for the UI.
 */
@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    // Takes the state of the devices and its informations (including paired and scanned Devices).
    // first line gives the initial state.
    // Next lines gives the update of the states.
    // Besides, it takes state about the chat message. if its connected then take state of messages. otherwise empty
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if (state.isConnected) state.messages else emptyList(),
            video=if (state.isConnected) state.video else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    // Saves the connection state.
    private var deviceConnectionJob: Job? = null

    // Initialise the current connection and error states.
    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }

    // Shows in UI that connects to a device. State will be saved in a job
    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    // Shows in UI the disconnection to device. Cancel Job.
    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false
            )
        }
    }

    // Launches Server and shows the waiting for other devices
    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    // Handles Sending message in our UI View model.
    // Takes the message as string
    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if (bluetoothMessage != null) {
                _state.update {
                    it.copy(
                        messages = it.messages + bluetoothMessage
                    )
                }
            }
        }
    }

    // Starts scan from bluetoothcontroller
    fun startScan() {
        bluetoothController.startDiscovery()
    }

    // Stops scan from bluetoothcontroller.
    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    // helper function.
    // Update the connection in the view model in our list.
    // Needs in the client as well as server scenario.
    // Besides, updates the messages (it transfer succeeded).
    // Returns a Job (which launches the observation).
    //Besides, update the status of the Videooutput later.
    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    _state.update {
                        it.copy(
                            messages = it.messages + result.message
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ConnectionResult.TransferVideoSucceeded -> {
                    _state.update {
                        it.copy(

                            video= listOf(result.video)
                        )
                    }
                }
            }
        }
            .catch {
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // releases all resources
    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}
