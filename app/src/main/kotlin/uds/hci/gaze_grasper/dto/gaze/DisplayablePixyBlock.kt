package uds.hci.gaze_grasper.dto.gaze

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

/**
 * Displayable version of the [PixyBlock] class.
 * The x, y, width, and height values in this class have been converted from Pixy space to device screen space.
 */
class DisplayablePixyBlock(pixyBlock: PixyBlock, resolution: Pair<Int, Int>) {
    private companion object {
        private const val PIXY_RESOLUTION_X = 316
        private const val PIXY_RESOLUTION_Y = 208
    }

    private val scaleMultiplier = resolution.first.toFloat() / PIXY_RESOLUTION_X

    val id = pixyBlock.index

    private val x = (pixyBlock.x * scaleMultiplier).roundToInt()
    private val y = (pixyBlock.y * scaleMultiplier).roundToInt()

    val width = (pixyBlock.width * scaleMultiplier).roundToInt()
    val height = (pixyBlock.height * scaleMultiplier).roundToInt()

    val xStart = x - (width / 2)
    private val xEnd = x + (width / 2)

    val yStart = y - (height / 2)
    private val yEnd = y + (height / 2)

    var isGazeWithin by mutableStateOf(false)
        private set

    fun onGaze(gazeCoords: GazeCoordinates) {
        if (gazeCoords.isNaN()) {
            return
        }

        isGazeWithin = gazeCoords.x.roundToInt() in xStart..xEnd
                && gazeCoords.y.roundToInt() in yStart..yEnd
    }
}
