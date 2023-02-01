package components

import EventBus
import Palette
import action.NewPaletteEvent
import action.PaletteSliderType
import androidx.compose.material.Slider
import androidx.compose.runtime.*

@Composable
fun PaletteSlider(min: Float, max: Float, type: PaletteSliderType, palette: Palette, onChangeComplete: (Int) -> Unit) {
    val default = if (type == PaletteSliderType.SIZE) palette.size else palette.colorRange
    var currentValue by remember{ mutableStateOf(default.toFloat()) }

    EventBus.listen(NewPaletteEvent::class.java).subscribe{
        currentValue = (if (type == PaletteSliderType.SIZE) it.palette.size else it.palette.colorRange).toFloat()
    }

    Slider(
        currentValue,
        valueRange = (min..max),
        steps = (max - min).toInt(),
        onValueChange = { currentValue = it },
        onValueChangeFinished = { onChangeComplete(currentValue.toInt()) }
    )
}