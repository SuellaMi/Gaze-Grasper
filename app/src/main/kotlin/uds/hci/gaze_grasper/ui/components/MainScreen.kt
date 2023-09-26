package uds.hci.gaze_grasper.ui.components

import android.Manifest
import androidx.compose.runtime.Composable
import uds.hci.gaze_grasper.domain.chat.BluetoothDevice
import uds.hci.gaze_grasper.ui.BluetoothUiState

/**
 * Part of the CameraFeature. It handles the aspect whether Permission to the Camera is given.
 * If its the case then go to the mainmenu. Otherwise, ask for permission
 */
@Composable
fun MainScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    MainContent(
        hasPermission = true,
        onRequestPermission = { },
        state,
        onStartScan,
        onStopScan,
        onStartServer,
        onDeviceClick
    )
}

// Handles the aspect whether permission is given or not.
@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    if (hasPermission) {
        CameraScreen(state, onStartScan, onStopScan, onStartServer, onDeviceClick)
    } else {
        NoPermissionScreen(onRequestPermission)
    }
}
