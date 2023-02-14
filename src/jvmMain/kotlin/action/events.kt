package action

import Complex
import Palette
import components.ZoomBox
import javax.swing.JPanel

interface ActionEvent {
    val action: Any
}

class CalculateEvent(override val action: CalculateAction) : ActionEvent
class FileEvent(override val action: FileAction, val data: String = "") : ActionEvent
class PaletteEvent(override val action: PaletteAction, val data: Any = 0) : ActionEvent
class UIEvent(override val action: UIAction, val data: Any = 0) : ActionEvent

data class NewPaletteEvent(val palette: Palette)

data class FractalEvent(
    val row: Int = 0,
    val column: Int = 0,
    val data: FractalPointData = FractalPointData(),
    var endOfRow: Boolean = false
)

data class FractalIterationEvent(val allowed: Long, val used: Long)

data class FractalSizeEvent(val width: Double, val height: Double)

data class FractalPointData(
    val iterations: Long = 0,
    val maxIterations: Long = 0,
    val z: Complex = Complex(0.0, 0.0),
    val zStart: Complex = Complex(0.0, 0.0)
)

data class ZoomBoxEvent(val zoomBox: ZoomBox)

data class ImageClickEvent(val x: Double, val y: Double, val image: JPanel)
