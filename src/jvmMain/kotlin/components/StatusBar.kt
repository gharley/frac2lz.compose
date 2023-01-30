package components

import EventBus
import Palette
import action.FractalEvent
import action.PaletteAction
import action.PaletteEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import state.FractalParameters

@Composable
fun StatusBar(pal: Palette) {
    val palette = remember { pal }

    var maxIterations by remember { mutableStateOf(0L) }
    var usedIterations by remember { mutableStateOf(0L) }
    var centerX by remember { mutableStateOf(0.0) }
    var centerY by remember { mutableStateOf(0.0) }
    var magnify by remember { mutableStateOf(0.0) }
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var colorRange by remember { mutableStateOf(palette.colorRange) }
    var size by remember { mutableStateOf(palette.size) }

    EventBus.listen(FractalEvent::class.java).subscribe {
        if (it.data.iterations > usedIterations) usedIterations = it.data.iterations
    }

    EventBus.listen(FractalParameters::class.java).subscribe {
        maxIterations = it.maxIterations
        centerX = it.centerX
        centerY = it.centerY
        magnify = it.magnify
        width = it.width.toInt()
        height = it.height.toInt()
    }

    fun updatePalette() {
        colorRange = palette.colorRange
        size = palette.size
    }

    EventBus.listen(PaletteEvent::class.java).subscribe {
        if (it.action == PaletteAction.CHANGED) updatePalette()
    }

    var scale = 1f

    Column(
        Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Divider()
        BoxWithConstraints(Modifier.wrapContentWidth()) {
//            val parentWidth = constraints.maxWidth.toFloat()
//            scale = maxWidth.value / parentWidth

            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                addTextField("Iterations -> Allowed: ", maxIterations.toString(), scale = scale)
                addTextField("Used: ", usedIterations.toString(), scale = scale)
                addTextField("RE Center: ", centerX.toString(), scale = scale)
                addTextField("IM Center: ", centerY.toString(), scale = scale)
                addTextField("Zoom: ", magnify.toString(), scale = scale)
                addTextField("Width: ", width.toString(), scale = scale)
                addTextField("Height: ", height.toString(), scale = scale)
                addTextField("Color Range: ", colorRange.toString(), scale = scale)
                addTextField("Palette Size: ", size.toString(), false, scale = scale)
            }
        }
    }
}

@Composable
fun ClipText(text: String, modifier: Modifier, scale: Float, fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        text,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        style = LocalTextStyle.current.copy(
            fontSize = LocalTextStyle.current.fontSize * scale
        ),
        fontWeight = fontWeight,
        modifier = modifier.wrapContentWidth(unbounded = true),
    )

}

@Composable
fun addTextField(labelText: String, valueText: String, addBorder: Boolean = true, scale: Float = 1f) {
    Column {
        Row(Modifier.wrapContentWidth()) {
            ClipText(
                labelText,
                Modifier.padding(horizontal = 5.dp, vertical = 10.dp),
                scale,
                fontWeight = FontWeight.Bold
            )
            ClipText(
                valueText,
                Modifier
                    .padding(end = 5.dp, top = 10.dp, bottom = 10.dp)
                    .drawBehind {
                        try {
                            check(addBorder)
                            val x = size.width + 3.dp.toPx()
                            drawLine(
                                Color.Black,
                                Offset(x, 0f),
                                Offset(x, size.height)
                            )
                        } catch (_: Exception) {
                        }
                    },
                scale
            )
        }
    }
}

