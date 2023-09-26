package uds.hci.gaze_grasper.ui.viewmodels

import android.Manifest
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Saves a list of requested but not granted permissions, to later show a PermissionDialog for each.
 * Also stores whether all permissions have been granted, which controls the display of the MissingPermissionScreen.
 */
class PermissionsViewModel : ViewModel() {
    val permissionDialogQueue = mutableStateListOf<String>()
    var allPermissionsGranted by mutableStateOf(false)

    fun dismissDialog() {
        Log.i(
            "PermissionsHandler",
            "Removing ${permissionDialogQueue[permissionDialogQueue.size - 1]} from permission queue"
        )
        permissionDialogQueue.removeLast()
    }

    fun onPermissionResult(permission: String, isDeclined: Boolean) {
        val permissionToAdd = if (permission == Manifest.permission.BLUETOOTH_SCAN
            || permission == Manifest.permission.BLUETOOTH_CONNECT
        ) "bluetooth" else permission

        if (isDeclined && !permissionDialogQueue.contains(permissionToAdd)) {
            Log.i("PermissionsHandler", "Adding $permissionToAdd to permission queue")
            permissionDialogQueue.add(0, permissionToAdd)
        }
    }
}
