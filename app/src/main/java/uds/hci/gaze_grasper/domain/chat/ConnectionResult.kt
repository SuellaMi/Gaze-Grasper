package uds.hci.gaze_grasper.domain.chat


/**
 * It defines the different types of connection results.
 */
sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}
