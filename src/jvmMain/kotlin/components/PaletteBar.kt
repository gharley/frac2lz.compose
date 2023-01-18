package components

import EventBus
import Palette
import action.PaletteAction
import action.PaletteEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PaletteCanvas(pal: Palette) {
    val palette: Palette = remember { pal }

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
                    color = color
                )
            }
        }
    }
}

@Composable
fun PaletteBar(width: Dp, pal: Palette) {
    val palette: Palette = remember { pal }

//    val resources = LocalAppResources.current.resources
    val iconScale = 4.0f

    Surface(modifier = Modifier.width(width), elevation = 5.dp) {
        Row(Modifier.height(48.dp)) {
            Column {
                IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.RANDOM)) }, Modifier.scale(iconScale)) {
                    Icon(
                        painterResource("random32.png"),
                        "Random Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.SMOOTH)) }, Modifier.scale(iconScale)) {
                    Icon(
                        painterResource("smooth32.png"),
                        "Smooth Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.DEFAULT)) }, Modifier.scale(iconScale)) {
                    Icon(
                        painterResource("default32.png"),
                        "Default Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(onClick = {}, Modifier.scale(iconScale)) {
                    Icon(
                        painterResource("animate32.png"),
                        "Animate Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column { PaletteCanvas(palette) }
        }
    }
}
