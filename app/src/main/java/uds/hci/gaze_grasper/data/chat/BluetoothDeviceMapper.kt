package uds.hci.gaze_grasper.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import uds.hci.gaze_grasper.domain.chat.BluetoothDeviceDomain

/**
 * It maps the bluetooth devices how we want and needed. its a helper function for update paired devices. return the
 * Bluetooth device domain including the name and address.
 */
@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}