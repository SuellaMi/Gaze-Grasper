package uds.hci.gaze_grasper.domain.chat

import java.io.IOException

/**
 * Error Message which says that data reading failed. Will be used in data transfer service
 */
class TransferFailedException : IOException("Reading incoming data failed")
