package uds.hci.gaze_grasper.domain.chat


/**
 * Data class, which contains the informations we need for our chat message.
 * isFromLocalUser: is a boolean that gives the information whether we send the message or the other user
 */
data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean
)
