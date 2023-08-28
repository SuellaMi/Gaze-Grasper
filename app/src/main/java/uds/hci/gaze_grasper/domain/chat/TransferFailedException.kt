package uds.hci.gaze_grasper.domain.chat

import java.io.IOException

class TransferFailedException: IOException("Reading incoming data failed")