package uds.hci.gaze_grasper.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice

/**
 * Represents a certain bluetooth device
 */
data class BluetoothDevice(
    val name: String?,
    val address: String
)
