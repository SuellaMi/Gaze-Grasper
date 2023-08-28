package uds.hci.gaze_grasper.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import uds.hci.gaze_grasper.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}