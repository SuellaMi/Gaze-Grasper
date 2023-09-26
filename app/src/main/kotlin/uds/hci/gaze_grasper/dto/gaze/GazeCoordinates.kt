package uds.hci.gaze_grasper.dto.gaze

data class GazeCoordinates(val x: Float, val y: Float) {
    override fun equals(other: Any?): Boolean {
        if (other !is GazeCoordinates) {
            return false
        }

        if ((x.isNaN() || y.isNaN()) && (other.x.isNaN() || other.y.isNaN())) {
            return true
        }

        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        return 31 * x.hashCode() + y.hashCode()
    }

    fun isNaN() = x.isNaN() || y.isNaN()
}
