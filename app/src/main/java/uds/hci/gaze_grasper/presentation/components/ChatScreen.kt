package uds.hci.gaze_grasper.presentation.components

import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import uds.hci.gaze_grasper.presentation.BluetoothUiState

/**
 * The UI structure and Look of the whole chat screen. Including  the single chat messages,
 * a button to disconnect, text field and a button for sending messages
 *
 * Update:It consists only a video background, which takes the current video state to show
 * the frames of the external camera
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {

    val video=state.video.get(state.video.size-1)

    BluetoothVideoBackground(videoFrame = video.buffer)
    //old stuff
    /*val message = rememberSaveable {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Disconnect"
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.messages) { message ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatMessage(
                        message = message,
                        modifier = Modifier
                            .align(
                                if (message.isFromLocalUser) Alignment.End else Alignment.Start
                            )
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(text = "Message")
                }
            )
            IconButton(onClick = {
                onSendMessage(message.value)
                message.value = ""
                keyboardController?.hide()
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }*/
}
/*
@Composable
fun Pixy2CameraPreview(pixy2InputStream: InputStream) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                factory = { context ->
                    Pixy2CameraPreviewView(context, pixy2InputStream)
                }
            )
        }
    )
}

@Composable
private fun Pixy2CameraPreviewView(
    context: Context,
    pixy2InputStream: InputStream
) {
    val textureView = androidx.camera.view.TextureView(context)

    val lifecycle = ViewTreeLifecycleOwner.get(textureView)
    lifecycle.lifecycleScope.launch {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Hier startest du die Anzeige des Pixy 2 Kameravideo-Feeds
                startPixy2VideoDisplay(pixy2InputStream, textureView.surfaceTexture)
            }
        })
    }

    AndroidView(
        factory = { context ->
            textureView
        },
        modifier = Modifier.fillMaxSize()
    )
}*/


//Function, which creates the video background by using each frame. It creates the Frame
//as Bitmap by using the sended ByteArray and use it as Video Background
@Composable
fun BluetoothVideoBackground(videoFrame: ByteArray) {
    val imageBitmap = processVideoFrame(videoFrame) // Funktion zur Verarbeitung des Videoframes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.Black) // Hintergrundfarbe setzen
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