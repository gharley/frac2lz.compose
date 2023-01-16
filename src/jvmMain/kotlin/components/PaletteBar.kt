package components

import EventBus
import Palette
import action.NewPaletteEvent
import action.PaletteEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PaletteCanvas(pal: Palette) {
    var palette:Palette = remember { pal }

    EventBus.listen(NewPaletteEvent::class.java).subscribe(){
        palette = it.palette
    }

    Canvas(Modifier.height(48.dp).fillMaxWidth()) {
//    removeMarkers()
        val width = size.width
        val stripeWidth = (width / palette.size * palette.colorRange).toInt()

        for (colorIndex in 0 until palette.size) {
            val color = palette.colors[colorIndex]

            for (stripeIndex in 0 until stripeWidth) {
                drawLine(
                    start = Offset(x = (colorIndex * stripeWidth + stripeIndex).toFloat(), y = 0f),
                    end = Offset(x = (colorIndex * stripeWidth + stripeIndex).toFloat(), y = size.height),
                    color = color.toColor()
                )
            }
        }
    }
}

@Composable
fun PaletteBar(width: Dp, palette: Palette, onPaletteTypeChange: (paletteType: Palette.PaletteType) -> Unit) {
//    val resources = LocalAppResources.current.resources

    Surface(modifier = Modifier.width(width), elevation = 5.dp) {
        Row {
            Column {
                IconButton(onClick = { onPaletteTypeChange(Palette.PaletteType.GRAY_SCALE) }) {
                    Icon(painterResource("default32.png"), "Default Palette", tint = Color.Unspecified)
                }
            }
            Column {
                IconButton(onClick = { onPaletteTypeChange(Palette.PaletteType.RANDOM) }) {
                    Icon(painterResource("random32.png"), "Random Palette", tint = Color.Unspecified)
                }
            }
            Column {
                IconButton(onClick = { onPaletteTypeChange(Palette.PaletteType.SMOOTH) }) {
                    Icon(painterResource("smooth32.png"), "Smooth Palette", tint = Color.Unspecified)
                }
            }
            Column {
                IconButton(onClick = {}) {
                    Icon(painterResource("animate32.png"), "Animate Palette", tint = Color.Unspecified)
                }
            }
            Column { PaletteCanvas(palette) }
        }
    }
}
