package action

import Complex
import Palette
import java.util.Properties

class AppTitle(val title: String)
class GetProperties
class HaveProperties(val props: Properties)
class SetProperty(val key: String, val value: String)

interface ActionEvent {
    val action: Any
}

class CalculateEvent(override val action: CalculateAction) : ActionEvent
class FileEvent(override val action: FileAction) : ActionEvent
class PaletteEvent(override val action: PaletteAction) : ActionEvent
class UIEvent(override val action: UIAction, val data: Any) : ActionEvent

data class NewPaletteEvent(val palette: Palette)

data class FractalPointData(val iterations: Long, val maxIterations: Long, val z: Complex, val zStart: Complex)
