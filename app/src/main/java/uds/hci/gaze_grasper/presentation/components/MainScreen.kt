package uds.hci.gaze_grasper.presentation.components


import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import uds.hci.gaze_grasper.domain.chat.BluetoothDevice
import uds.hci.gaze_grasper.presentation.BluetoothUiState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
        /**
         *Part of the CameraFeature. It handles the aspect whether Permission to the Camera is given.
         * If its the case then go to the mainmenu. Otherwise, ask for permission
         */
fun MainScreen(state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit) {

    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = cameraPermissionState::launchPermissionRequest,
        state,
        onStartScan,
        onStopScan,
        onStartServer,
        onDeviceClick
    )
}

//Handles the aspect whether permission is given or not.
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
        CameraScreen(state, onStartScan,onStopScan,onStartServer,onDeviceClick)
    } else {
        NoPermissionScreen(onRequestPermission)
    }
}

/*@Preview
@Composable
private fun Preview_MainContent() {
    MainContent(
        hasPermission = true,
        onRequestPermission = {},
    )
}*/