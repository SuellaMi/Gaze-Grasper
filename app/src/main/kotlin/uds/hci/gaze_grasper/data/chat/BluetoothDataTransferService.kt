package uds.hci.gaze_grasper.data.chat

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import uds.hci.gaze_grasper.domain.chat.BluetoothMessage
import uds.hci.gaze_grasper.domain.chat.BluetoothVideo
import java.io.IOException

/**
 * Error Message which says that data reading failed
 */
class TransferFailedException : IOException("Reading incoming data failed")

/**
 * Handles the functionality of sending and receiving messages in the class
 */
class BluetoothDataTransferService(
    // Active connection to an other device
    private val socket: BluetoothSocket
) {

    // Takes a look whether it gets messages.
    // Returns a flow of those Messages
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw TransferFailedException()
                }

                emit(
                    buffer.decodeToString(
                        endIndex = byteCount
                    ).toBluetoothMessage(
                        isFromLocalUser = false
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Takes the information for the Video or Frames as ByteArray. Returns a Flow of necessary information of it.
    fun listenForIncomingVideoMessages(): Flow<BluetoothVideo> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw TransferFailedException()
                }

                val buffer2 = ByteArray(1024)
                buffer2[buffer2.size - 1] = byteCount.toByte()
                emit(
                    toBluetoothVideo(
                        1024,
                        buffer2,
                        byteCount
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Function that send Messages to the other device.
    // takes the message for sending and returns a boolean, whether sending succeeded.
    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }

    fun sendMessageConstant(bytes: ByteArray){
        try {
            socket.outputStream.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()

        }
    }
}
