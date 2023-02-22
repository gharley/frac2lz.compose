package components

import EventBus
import Palette
import action.NewPaletteEvent
import action.PaletteSliderType
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderThumb(positions: SliderPositions) {
    Badge(Modifier.size(30.dp), MaterialTheme.colorScheme.primary) {
        Text(
            positions.positionFraction.toInt().toString(),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.background
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteSlider(min: Float, max: Float, type: PaletteSliderType, palette: Palette, onChangeComplete: (Int) -> Unit) {
    val default = if (type == PaletteSliderType.SIZE) palette.size else palette.colorRange
    var currentValue by remember { mutableStateOf(default.toFloat()) }
    var subscribed by remember { mutableStateOf(false) }

    if (!subscribed) {
        subscribed = true

        EventBus.listen(NewPaletteEvent::class.java).subscribe {
            currentValue = (if (type == PaletteSliderType.SIZE) it.palette.size else it.palette.colorRange).toFloat()
        }
    }

    Row {
        Slider(
            currentValue,
            onValueChange = { currentValue = it },
            Modifier.weight(1f),
            valueRange = (min..max),
            steps = (max - min).toInt(),
            onValueChangeFinished = { onChangeComplete(currentValue.toInt()) },
            thumb = { SliderThumb(positions = SliderPositions(currentValue, floatArrayOf(0f))) }
        )
    }
}
