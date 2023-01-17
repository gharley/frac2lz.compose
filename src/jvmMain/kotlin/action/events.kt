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

data class FractalEvent(val row: Int, val column: Int, val data: FractalPointData, var endOfRow: Boolean = false)
data class FractalIterationEvent(val allowed: Long, val used: Long)
data class FractalPointData(val iterations: Long, val maxIterations: Long, val z: Complex, val zStart: Complex)
