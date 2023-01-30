package components

import Palette
import androidx.compose.material.Slider
import androidx.compose.runtime.*

@Composable
fun PaletteSlider(min: Float, max: Float, default: Float, pal: Palette, onChangeComplete: (Float) -> Unit) {
    val palette = remember { pal }
    var currentValue by remember { mutableStateOf(default) }

    Slider(
        currentValue,
        valueRange = (min..max),
        steps = (max - min).toInt(),
        onValueChange = { currentValue = it },
        onValueChangeFinished = { onChangeComplete(currentValue) }
    )
}