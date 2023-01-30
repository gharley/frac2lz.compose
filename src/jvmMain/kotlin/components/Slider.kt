package components

import androidx.compose.material.Slider
import androidx.compose.runtime.*

@Composable
fun PaletteSlider(min: Float, max: Float, default: Float, onChangeComplete: (Float) -> Unit) {
    var currentValue by remember { mutableStateOf(default) }

    Slider(
        currentValue,
        valueRange = (min..max),
        steps = (max - min).toInt(),
        onValueChange = { currentValue = it },
        onValueChangeFinished = { onChangeComplete(currentValue) }
    )
}