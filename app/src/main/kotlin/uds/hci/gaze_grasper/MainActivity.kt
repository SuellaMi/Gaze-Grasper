package uds.hci.gaze_grasper

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import uds.hci.gaze_grasper.domain.gaze.BlocksManager
import uds.hci.gaze_grasper.domain.gaze.GazeTrackerManager
import uds.hci.gaze_grasper.dto.gaze.PixyBlock
import uds.hci.gaze_grasper.ui.components.BluetoothConnectScreen
import uds.hci.gaze_grasper.ui.viewmodels.BluetoothViewModel
import uds.hci.gaze_grasper.ui.components.BluetoothVideoBackground
import uds.hci.gaze_grasper.ui.components.GazeTrackingScreen
import uds.hci.gaze_grasper.ui.components.PermissionsHandler
import uds.hci.gaze_grasper.ui.theme.GazeGrasperTheme

/**
 * Main Class where the UI aspects and general information were handled.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Main function which handles first the UI aspects via viewmodel and components. gets device screen
    // by default as main menu structure if isn't connected with bluetooth device. otherwise Chatscreen with chatMessages.
    // If it tries connecting (launches server) it shows progress bar.
    // it handles also the permissions first
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set app to fullscreen (landscape orientation set via manifest)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        val screenWidth = windowManager.currentWindowMetrics.bounds.width()
        val screenHeight = windowManager.currentWindowMetrics.bounds.height()

        val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        val blocksManager = BlocksManager(screenWidth to screenHeight, toast)
        val gazeTrackerManager = GazeTrackerManager(applicationContext, blocksManager)

        // TODO: remove mock-PixyBlock once they get received over Bluetooth
        blocksManager.addBlocks(
            listOf(
                PixyBlock(0, 266, 120, 100, 50, 0, 0, 0)
            )
        )

        // TODO: fix ui, wire up bluetooth
        setContent {
            GazeGrasperTheme {
                PermissionsHandler(::shouldShowRequestPermissionRationale)

                val viewModel = hiltViewModel<BluetoothViewModel>()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(key1 = state.errorMessage) {
                    state.errorMessage?.let { message ->
                        Toast.makeText(
                            applicationContext,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if (state.isConnected) {
                        Toast.makeText(
                            applicationContext,
                            "You're connected!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                when {
                    state.isConnecting -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(text = "Connecting...")
                        }
                    }

                    state.isConnected -> {
                        BluetoothVideoBackground(state)
                    }

                    else -> {
                        BluetoothConnectScreen(
                            state = state,
                            onStartScan = viewModel::startScan,
                            onStopScan = viewModel::stopScan,
                            onDeviceClick = viewModel::connectToDevice,
                            onStartServer = viewModel::waitForIncomingConnections
                        )
                    }
                }

                GazeTrackingScreen(gazeTrackerManager = gazeTrackerManager, blocksManager = blocksManager)
            }
        }
    }
}
