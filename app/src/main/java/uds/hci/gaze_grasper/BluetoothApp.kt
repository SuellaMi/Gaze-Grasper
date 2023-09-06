package uds.hci.gaze_grasper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//Necessary to provide the bluetoothcontroller in viewmodel setup
@HiltAndroidApp
class BluetoothApp : Application()
