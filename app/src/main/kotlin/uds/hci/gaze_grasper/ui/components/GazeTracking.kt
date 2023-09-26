package uds.hci.gaze_grasper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import uds.hci.gaze_grasper.domain.gaze.BlocksManager
import uds.hci.gaze_grasper.domain.gaze.GazeTrackerManager
import uds.hci.gaze_grasper.domain.gaze.GazeTrackerState
import uds.hci.gaze_grasper.dto.gaze.DisplayablePixyBlock
import uds.hci.gaze_grasper.dto.gaze.GazeCoordinates
import kotlin.math.roundToInt

@Composable
fun GazeTrackingScreen(gazeTrackerManager: GazeTrackerManager, blocksManager: BlocksManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text("Gaze tracker state: ${gazeTrackerManager.gazeTrackerState}")
        Text("Blink count: ${gazeTrackerManager.blinkCount}")
        Spacer(Modifier.height(8.dp))
        when (gazeTrackerManager.gazeTrackerState) {
            GazeTrackerState.OFF -> {
                Button(onClick = gazeTrackerManager::initialize) {
                    Text("Start gaze tracking")
                }
            }

            GazeTrackerState.TRACKING -> {
                Button(onClick = gazeTrackerManager::startCalibration) {
                    Text("Recalibrate")
                }
                Text(
                    "Gaze coordinate x: ${gazeTrackerManager.gazeCoords.x}, " +
                            "y: ${gazeTrackerManager.gazeCoords.y}"
                )
            }

            GazeTrackerState.CALIBRATING -> {
                Text(
                    "Calibration coordinate x: ${gazeTrackerManager.calibrationCoords.first}, " +
                            "y: ${gazeTrackerManager.calibrationCoords.second}"
                )
            }

            else -> null
        }
    }

    for (dpb in blocksManager.blocks) {
        PixyBlockBox(dpb = dpb, onBlockSelection = blocksManager::onBlockSelection)
    }

    if (gazeTrackerManager.gazeTrackerState == GazeTrackerState.CALIBRATING) {
        CalibrationIcon(gazeTrackerManager.calibrationCoords, gazeTrackerManager.calibrationProgress)
    }

    if (gazeTrackerManager.gazeTrackerState == GazeTrackerState.TRACKING) {
        GazeIcon(location = gazeTrackerManager.gazeCoords)
    }
}


@Composable
private fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
private fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
private fun PixyBlockBox(dpb: DisplayablePixyBlock, onBlockSelection: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset(dpb.xStart, dpb.yStart) }
            .size(dpb.width.pxToDp(), dpb.height.pxToDp())
            .clickable { onBlockSelection(dpb.id) }
            .border(4.dp, if (dpb.isGazeWithin) Color.Green else Color.Cyan)
            .padding(4.dp)
    ) {
        Text("id: ${dpb.id}")
    }
}

@Composable
private fun ExampleGazeTarget(location: Pair<Int, Int>, color: Color) {
    val size = 72.dp
    val sizeInPx = size.dpToPx().roundToInt()
    val halfSizeInPx = sizeInPx / 2
    val offset = IntOffset(location.first - halfSizeInPx, location.second - halfSizeInPx)

    Box(
        modifier = Modifier
            .absoluteOffset { offset }
            .size(size)
            .border(4.dp, color)
    )
}

@Composable
private fun GazeIcon(location: GazeCoordinates) {
    if (location.isNaN()) {
        return
    }

    val iconSize = 10.dp
    val halfSize = (iconSize.dpToPx() / 2).roundToInt()
    val offset = IntOffset(location.x.roundToInt() - halfSize, location.y.roundToInt() - halfSize)

    Box(
        modifier = Modifier
            .absoluteOffset { offset }
            .size(iconSize)
            .background(Color.Red, CircleShape)
    )
}

@Composable
private fun CalibrationIcon(location: Pair<Int, Int>, progress: Float) {
    val iconSize = 36.dp
    val halfSize = (iconSize.dpToPx() / 2).roundToInt()
    val offset = IntOffset(location.first - halfSize, location.second - halfSize)

    Box(modifier = Modifier.absoluteOffset { offset }) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(iconSize),
            color = Color.Red
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color.Red, CircleShape)
                .align(Alignment.Center)
        )
    }
}
