package uds.hci.gaze_grasper.dto.gaze

/**
 * Data class representing the `Block` struct from the [Pixy API](https://docs.pixycam.com/wiki/doku.php?id=wiki:v2:ccc_api).
 * Each such block represents one object recognized by the Pixy camera.
 */
data class PixyBlock(
    /**
     * Signature number or color-code number
     */
    val signature: Int,
    /**
     * x location of the center of the block. Is between 0 and 315
     */
    val x: Int,
    /**
     * y location of the center of the block. Is between 0 and 208
     */
    val y: Int,
    /**
     * width of the block. Is between 0 and 316
     */
    val width: Int,
    /**
     * height of the block. Is between 0 and 208
     */
    val height: Int,
    /**
     * If [signature] is a color-code, this represents the angle of that color (between -180° and 180°).
     * 0 otherwise
     */
    val angle: Int,
    /**
     * Tracking index of the block
     */
    val index: Int,
    /**
     * Number of frames this block was tracked for. Is between 0 and 255
     */
    val age: Int
)
