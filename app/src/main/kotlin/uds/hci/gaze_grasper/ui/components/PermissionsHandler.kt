package uds.hci.gaze_grasper.ui.components

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import uds.hci.gaze_grasper.ui.viewmodels.PermissionsViewModel

private val permissionsToRequest = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT
)

/**
 * Handles requesting permissions on application start up.
 * @param shouldShowRequestPermissionRationale pointer to activity method of the same name
 */
@Composable
fun PermissionsHandler(shouldShowRequestPermissionRationale: (String) -> Boolean) {
    val context = LocalContext.current
    val viewModel = viewModel<PermissionsViewModel>()

    // Provides access to Bluetooth adapter data such as MAC address, name, list of paired devices, etc.
    val bluetoothAdapter = remember {
        context.getSystemService(BluetoothManager::class.java).adapter
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { }
    )

    // Launches Android's permission request dialogs
    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { map ->
            permissionsToRequest.forEach { permission ->
                Log.i("PermissionsHandler", "Should add $permission: ${map[permission] == false}")
                viewModel.onPermissionResult(permission, map[permission] == false)
            }

            if (bluetoothAdapter?.isEnabled == false && map[Manifest.permission.BLUETOOTH_CONNECT] == true) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }

            if (map.values.all { it }) {
                viewModel.allPermissionsGranted = true
            }
        }
    )

    // Requests permission (if not already given) on activity start
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                Log.i("PermissionsHandler", "Launching permission launcher")
                multiplePermissionResultLauncher.launch(permissionsToRequest)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!viewModel.allPermissionsGranted) {
        MissingPermissionScreen { multiplePermissionResultLauncher.launch(permissionsToRequest) }
    }

    // Displays a PermissionDialog for each declined permission
    viewModel.permissionDialogQueue.forEach { permission ->
        PermissionDialog(
            permission = permission,
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                if (permission == "bluetooth") Manifest.permission.BLUETOOTH_SCAN else permission
            ),
            onDismiss = viewModel::dismissDialog,
            onRequestAgain = {
                viewModel.dismissDialog()
                multiplePermissionResultLauncher.launch(
                    if (permission == "bluetooth") arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) else arrayOf(permission)
                )
            },
            onGoToSettings = { openAppSettings(context) }
        )
    }
}

@Composable
private fun MissingPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Not all permissions, which are required for this app to function, have been granted yet.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant permissions")
        }
    }
}

/**
 * Opens the system settings screen for this app.
 * @param context current activity context
 */
private fun openAppSettings(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).also(context::startActivity)
}

/**
 * Informs the user why the [permission], which they declined, is required.
 * Offers to request it again after first decline, and to open settings after second decline.
 */
@Composable
private fun PermissionDialog(
    permission: String,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onRequestAgain: () -> Unit,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionName = when (permission) {
        Manifest.permission.CAMERA -> "camera"
        "bluetooth" -> "nearby devices"
        else -> permission
    }

    val permissionReasoning = when (permission) {
        Manifest.permission.CAMERA -> "The camera permission is required for gaze tracking functionality " +
                "and is mandatory."

        "bluetooth" -> "The nearby devices permission is required for Bluetooth communication with the arm " +
                "controller and Pixy camera."

        else -> permission
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission required") },
        text = {
            Text(
                if (!isPermanentlyDeclined) {
                    permissionReasoning
                } else {
                    "The $permissionName permission has been declined, but is required for this app to work. " +
                            "Please grant the $permissionName permission in the app settings."
                }
            )
        },
        confirmButton = {
            Button(onClick = {
                if (!isPermanentlyDeclined) {
                    onRequestAgain()
                } else {
                    onGoToSettings()
                    onDismiss()
                }
            }) {
                Text(if (!isPermanentlyDeclined) "Grant permission" else "Open settings")
            }
        },
        modifier = modifier
    )
}
