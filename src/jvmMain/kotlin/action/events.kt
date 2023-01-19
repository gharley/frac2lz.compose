package action

import Complex
import Palette

interface ActionEvent {
    val action: Any
}

class CalculateEvent(override val action: CalculateAction) : ActionEvent
class FileEvent(override val action: FileAction) : ActionEvent
class PaletteEvent(override val action: PaletteAction) : ActionEvent
class UIEvent(override val action: UIAction, val data: Any) : ActionEvent

data class NewPaletteEvent(val palette: Palette)

data class FractalEvent(val row: Int = 0, val column: Int = 0, val data: FractalPointData = FractalPointData(), var endOfRow: Boolean = false)
data class FractalIterationEvent(val allowed: Long, val used: Long)
data class FractalPointData(val iterations: Long = 0, val z: Complex = Complex(0.0, 0.0), val zStart: Complex = Complex(0.0, 0.0))
