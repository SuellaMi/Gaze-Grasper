package uds.hci.gaze_grasper.data.chat

import uds.hci.gaze_grasper.domain.chat.BluetoothMessage
import uds.hci.gaze_grasper.domain.chat.BluetoothVideo


/**
 * Takes bluetooth string bite array and convert it back to use it for the chat message
 */
fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {
    val name = substringBeforeLast("#")
    val message = substringAfter("#")
    return BluetoothMessage(
        message = message,
        senderName = name,
        isFromLocalUser = isFromLocalUser
    )
}

fun toBluetoothVideo(     bufferSize: Int,
                                 buffer: ByteArray,
                                 bytesRead: Int): BluetoothVideo {

    return BluetoothVideo(
         bufferSize=bufferSize,
     buffer=buffer,
     bytesRead=bytesRead
    )
}

/**
 * Converts message to biteArray, which we can use to send it later on the chat message.
 */
fun BluetoothMessage.toByteArray(): ByteArray {
    return "$senderName#$message".encodeToByteArray()
}
