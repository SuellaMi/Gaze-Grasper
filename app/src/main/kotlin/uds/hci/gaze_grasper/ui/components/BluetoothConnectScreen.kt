package uds.hci.gaze_grasper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uds.hci.gaze_grasper.domain.chat.BluetoothDevice
import uds.hci.gaze_grasper.ui.BluetoothUiState

/**
 * Displays a list of paired and discovered devices,
 * as well as controls for toggling scanning and connecting to a discovered device.
 * Is displayed by the MainActivity when no device is connected.
 */
@Composable
fun BluetoothConnectScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    // Displays view from the device back camera:

    // val localContext = LocalContext.current
    // val lifeCycleOwner = LocalLifecycleOwner.current
    // val cameraController = remember { LifecycleCameraController(localContext) }
    // AndroidView(
    //     modifier = Modifier.fillMaxSize(),
    //     factory = { context ->
    //         PreviewView(context).apply {
    //             layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    //             setBackgroundColor(Color.BLACK)
    //             scaleType = PreviewView.ScaleType.FILL_START
    //         }.also { previewView ->
    //             previewView.controller = cameraController
    //             cameraController.bindToLifecycle(lifeCycleOwner)
    //         }
    //     })

    Column(modifier = Modifier.fillMaxSize()) {
        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
            }
            Button(onClick = onStartServer) {
                Text(text = "Start server")
            }
        }
    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(pairedDevices) { device ->
            Text(
                text = device.name ?: device.address,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            )
        }

        item {
            Text(
                text = "Scanned Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(scannedDevices) { device ->
            Text(
                text = device.name ?: device.address,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(device) }
                    .padding(16.dp)
            )
        }
    }
}
