package uds.hci.gaze_grasper.domain.gaze

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import uds.hci.gaze_grasper.data.chat.AndroidBluetoothController
import uds.hci.gaze_grasper.dto.gaze.DisplayablePixyBlock
import uds.hci.gaze_grasper.dto.gaze.GazeCoordinates
import uds.hci.gaze_grasper.dto.gaze.PixyBlock

class BlocksManager(
    private val resolution: Pair<Int, Int>,
    private val toast: Toast,
    private val bluetoothController: AndroidBluetoothController
) {
    val blocks = mutableStateListOf<DisplayablePixyBlock>()
    var gazedBlockId = -1
        private set

    fun addBlocks(pixyBlocks: List<PixyBlock>) {
        blocks.clear()
        pixyBlocks.forEach { pixyBlock ->
            blocks.add(DisplayablePixyBlock(pixyBlock, resolution))
        }
    }

    fun onGaze(gazeCoords: GazeCoordinates) {
        gazedBlockId = -1
        blocks.forEach { block ->
            block.onGaze(gazeCoords)
            if (block.isGazeWithin) {
                gazedBlockId = block.id
            }
        }
    }

    fun onBlockSelection(id: Int) {
        if (id == -1) {
            return
        }

        Log.i("BlocksManager", "PixyBlock id:$id selected!")
        toast.setText("Pixy Block id:$id selected!")
        toast.show()

        bluetoothController.sendRawData(byteArrayOf(id.toByte()))
    }
}
