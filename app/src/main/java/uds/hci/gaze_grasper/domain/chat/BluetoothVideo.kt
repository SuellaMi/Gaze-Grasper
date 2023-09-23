package uds.hci.gaze_grasper.domain.chat

data class BluetoothVideo(
    val bufferSize: Int,
    val buffer: ByteArray,
    val bytesRead: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothVideo

        if (bufferSize != other.bufferSize) return false
        if (!buffer.contentEquals(other.buffer)) return false
        if (bytesRead != other.bytesRead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bufferSize
        result = 31 * result + buffer.contentHashCode()
        result = 31 * result + bytesRead
        return result
    }
}
