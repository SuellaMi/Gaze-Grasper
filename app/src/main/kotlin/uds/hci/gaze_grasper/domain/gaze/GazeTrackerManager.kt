package uds.hci.gaze_grasper.domain.gaze

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import camp.visual.gazetracker.GazeTracker
import camp.visual.gazetracker.callback.CalibrationCallback
import camp.visual.gazetracker.callback.GazeCallback
import camp.visual.gazetracker.callback.InitializationCallback
import camp.visual.gazetracker.callback.StatusCallback
import camp.visual.gazetracker.callback.UserStatusCallback
import camp.visual.gazetracker.constant.StatusErrorType
import camp.visual.gazetracker.constant.UserStatusOption
import camp.visual.gazetracker.filter.OneEuroFilterManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uds.hci.gaze_grasper.dto.gaze.GazeCoordinates
import kotlin.math.roundToInt

enum class GazeTrackerState {
    OFF, INITIALIZING, TRACKING, CALIBRATING;
}

/**
 * Manages interaction with the [SeeSo gaze tracker](https://docs.seeso.io/).
 */
class GazeTrackerManager(private val context: Context, private val blocksManager: BlocksManager) {
    private var gazeTracker: GazeTracker? = null
    var gazeTrackerState by mutableStateOf(GazeTrackerState.OFF)
        private set

    var gazeCoords by mutableStateOf(GazeCoordinates(0f, 0f))
        private set
    private var nanTimeout: Job? = null

    var calibrationCoords by mutableStateOf(0 to 0)
        private set
    var calibrationProgress by mutableFloatStateOf(0f)
        private set

    private var blinkStartTime: Long = 0
    private var endOfLastBlink: Long = 0
    var blinkCount by mutableIntStateOf(0)
        private set
    private var gazedBlockAtBlinkStart = -1

    private companion object {
        private const val LICENSE_KEY = "dev_dtz76wr6u9zwymhum3lprlvnq5y4lhuqljfuk5sf"
        private val gazeTrackingFilter = OneEuroFilterManager(2)
    }

    fun initialize() {
        gazeTrackerState = GazeTrackerState.INITIALIZING
        val userOptions = UserStatusOption()
        userOptions.useBlink()
        GazeTracker.initGazeTracker(context, LICENSE_KEY, initializationCallback, userOptions)
    }

    private fun startTracking() {
        // val cameraPosition = CameraPosition("", 1, 1)
        gazeTracker?.setCallbacks(statusCallback, gazeCallback, calibrationCallback, userStatusCallback)
        gazeTracker?.startTracking()
        gazeTrackerState = GazeTrackerState.TRACKING
    }

    fun startCalibration() {
        Log.i("GazeTracker", "Calibration started")
        gazeTrackerState = GazeTrackerState.CALIBRATING
        gazeTracker?.startCalibration()
    }

    private val initializationCallback = InitializationCallback { tracker, errorType ->
        if (tracker != null) {
            gazeTracker = tracker
            Log.i("GazeTracker", "GazeTracker initialized")
            startTracking()
        } else {
            Log.e("GazeTracker", "Couldn't initialize gaze tracker due to $errorType")
        }
    }

    private val statusCallback = object : StatusCallback {
        override fun onStarted() {
            Log.i("GazeTracker", "Tracking started")
            startCalibration()
        }

        override fun onStopped(errorType: StatusErrorType?) {
            Log.w("GazeTracker", "Tracking stopped due to $errorType")
        }
    }

    /**
     * Called every gaze tracker tick (about every 50ms).
     */
    @OptIn(DelicateCoroutinesApi::class)
    private val gazeCallback = GazeCallback { gazeInfo ->
        if (gazeTrackerState != GazeTrackerState.TRACKING) {
            return@GazeCallback
        }

        // Get new gaze coordinates
        val newGazeCoords = if (!gazeTrackingFilter.filterValues(gazeInfo.timestamp, gazeInfo.x, gazeInfo.y)) {
            GazeCoordinates(Float.NaN, Float.NaN)
        } else {
            GazeCoordinates(gazeTrackingFilter.filteredValues[0], gazeTrackingFilter.filteredValues[1])
        }

        // Early return if unchanged
        if (newGazeCoords == gazeCoords) {
            return@GazeCallback
        }

        // Set new gaze coordinates
        gazeCoords = newGazeCoords
        // Log.d("GazeTracker", "Gaze coord x: ${gazeInfo.x}, y: ${gazeInfo.y}")
        // Log.d(
        //     "GazeTracker",
        //     "Gaze filtered coord x: ${gazeTrackingFilter.filteredValues[0]}, " +
        //             "y: ${gazeTrackingFilter.filteredValues[1]}"
        // )
        blocksManager.onGaze(newGazeCoords)

        // Set timeout if NaN
        if (nanTimeout != null) {
            nanTimeout?.cancel()
            nanTimeout = null
        }

        if (newGazeCoords.isNaN() && gazeTrackerState == GazeTrackerState.TRACKING) {
            Log.w("GazeTracker", "Setting new NaN timeout")
            nanTimeout = GlobalScope.launch(Dispatchers.Default) {
                delay(4000)
                Log.w("GazeTracker", "Recalibrating due to NaN timeout")
                startCalibration()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val calibrationCallback = object : CalibrationCallback {
        override fun onCalibrationProgress(progress: Float) {
            if (progress == calibrationProgress) {
                return
            }
            calibrationProgress = progress
        }

        override fun onCalibrationNextPoint(x: Float, y: Float) {
            Log.i("GazeTracker", "Calibration next point x: $x, y: $y")
            calibrationCoords = x.roundToInt() to y.roundToInt()

            GlobalScope.launch(Dispatchers.Default) {
                delay(1000)
                gazeTracker?.startCollectSamples()
                Log.i("GazeTracker", "Started collecting gaze samples for x: $x, y: $y")
            }
        }

        override fun onCalibrationFinished(calibrationData: DoubleArray?) {
            Log.i("GazeTracker", "Calibration finished")
            gazeTrackerState = GazeTrackerState.TRACKING
        }
    }

    private val userStatusCallback = object : UserStatusCallback {
        override fun onAttention(timestampBegin: Long, timestampEnd: Long, score: Float) {
            // not needed
        }

        override fun onDrowsiness(timestamp: Long, isDrowsiness: Boolean, intensity: Float) {
            // not needed
        }

        /**
         * Called every gaze tracker tick (about every 50ms).
         * @param isBlink whether both eyes are closed at current [timestamp]
         */
        override fun onBlink(
            timestamp: Long,
            isBlinkLeft: Boolean,
            isBlinkRight: Boolean,
            isBlink: Boolean,
            leftOpenness: Float,
            rightOpenness: Float
        ) {
            Log.d("GazeTracker", "isBlink: $isBlink timestamp: $timestamp")

            if (gazeTrackerState != GazeTrackerState.TRACKING) {
                if (blinkStartTime != 0L || endOfLastBlink != 0L || blinkCount != 0) {
                    blinkStartTime = 0
                    endOfLastBlink = 0
                    blinkCount = 0
                }
                return
            }

            if (isBlink) {
                if (blinkStartTime == 0L) {
                    Log.i("GazeTracker", "Starting blink")
                    blinkStartTime = timestamp

                    if (blinkCount == 0) {
                        gazedBlockAtBlinkStart = blocksManager.gazedBlockId
                    }
                }
                return
            }

            // Reset blink sequence if no blinks for a while
            if (endOfLastBlink != 0L && (timestamp - endOfLastBlink) > 800) {
                endOfLastBlink = 0
                blinkCount = 0
                gazedBlockAtBlinkStart = -1
            }

            if (blinkStartTime > 0) {
                Log.i("GazeTracker", "Ending blink")

                if ((timestamp - blinkStartTime) in 100..700) {
                    Log.i("GazeTracker", "Got a real blink")
                    checkBlinkSequence(timestamp)
                }

                blinkStartTime = 0
            }
        }
    }

    private fun checkBlinkSequence(timestamp: Long) {
        if (endOfLastBlink != 0L && (timestamp - endOfLastBlink) >= 800) {
            return
        }

        blinkCount++
        endOfLastBlink = timestamp

        if (blinkCount == 3) {
            Log.w("GazeTracker", "Got blink sequence!")
            blinkCount = 0
            endOfLastBlink = 0
            blocksManager.onBlockSelection(gazedBlockAtBlinkStart)
            gazedBlockAtBlinkStart = -1
        }
    }
}
