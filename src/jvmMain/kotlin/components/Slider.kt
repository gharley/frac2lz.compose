package components

import EventBus
import Palette
import action.NewPaletteEvent
import action.PaletteSliderType
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteSlider(min: Float, max: Float, type: PaletteSliderType, palette: Palette, onChangeComplete: (Int) -> Unit) {
    val default = if (type == PaletteSliderType.SIZE) palette.size else palette.colorRange
    var currentValue by remember { mutableStateOf(default.toFloat()) }

    EventBus.listen(NewPaletteEvent::class.java).subscribe {
        currentValue = (if (type == PaletteSliderType.SIZE) it.palette.size else it.palette.colorRange).toFloat()
    }

    Slider(
        currentValue,
        valueRange = (min..max),
        steps = (max - min).toInt(),
        onValueChange = { currentValue = it },
        onValueChangeFinished = { onChangeComplete(currentValue.toInt()) },
    )
    Badge(Modifier.size(30.dp), MaterialTheme.colorScheme.primary) {
        Text(currentValue.toInt().toString(), fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderThumb() {
    Badge(Modifier.size(30.dp), MaterialTheme.colorScheme.primary) {
        Text("256", fontWeight = FontWeight.Bold, color = Color.White)
    }
}