package uds.hci.gaze_grasper.presentation.components

import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import uds.hci.gaze_grasper.presentation.BluetoothUiState

//Function, which creates the video background by using each frame. It creates the Frame
//as Bitmap by using the sended ByteArray and use it as Video Background
@Composable
fun BluetoothVideoBackground(state: BluetoothUiState) {
    val video = state.video[state.video.size - 1]
    val videoFrame = video.buffer
    val imageBitmap = processVideoFrame(videoFrame) // Funktion zur Verarbeitung des Videoframes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        // Das Video als Hintergrund anzeigen
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val imageView = ImageView(context)
                imageView.setImageBitmap(imageBitmap.asAndroidBitmap())
                imageView
            }
        )
    }
}

// Function which decode the Byte Array into a Bitmap. Returns a Bitmap for the
// Video Background.
fun processVideoFrame(videoFrame: ByteArray): ImageBitmap {
    // Implementiere die Verarbeitung des Videoframes und die Rückgabe als ImageBitmap
    val bitmap = BitmapFactory.decodeByteArray(videoFrame, 0, videoFrame.size)
    return bitmap.asImageBitmap()
}
