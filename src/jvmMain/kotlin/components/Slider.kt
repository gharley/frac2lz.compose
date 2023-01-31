package components

import Palette
import action.PaletteSliderType
import androidx.compose.material.Slider
import androidx.compose.runtime.*

@Composable
fun PaletteSlider(min: Float, max: Float, type: PaletteSliderType, pal: Palette, onChangeComplete: (Int) -> Unit) {
    val palette by remember { mutableStateOf(pal) }
    val default = if (type == PaletteSliderType.SIZE) palette.size else palette.colorRange
    var currentValue by remember { mutableStateOf(default.toFloat()) }

    Slider(
        currentValue,
        valueRange = (min..max),
        steps = (max - min).toInt(),
        onValueChange = { currentValue = it },
        onValueChangeFinished = { onChangeComplete(currentValue.toInt()) }
    )
}