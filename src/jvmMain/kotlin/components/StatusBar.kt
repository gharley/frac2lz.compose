package components

import EventBus
import Palette
import action.FractalEvent
import action.NewPaletteEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusBar(pal: Palette) {
    val palette = remember { pal }
    var maxIterations by remember { mutableStateOf(0L) }
    var usedIterations by remember { mutableStateOf(0L) }
    var centerX by remember { mutableStateOf(0f) }
    var centerY by remember { mutableStateOf(0f) }
    var magnify by remember { mutableStateOf(0f) }
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var colorRange = remember { palette.colorRange }
    var size = remember { palette.size }

    EventBus.listen(FractalEvent::class.java).subscribe {
        maxIterations = it.data.maxIterations
        usedIterations = it.data.iterations
    }
//
//    EventBus.listen(NewPaletteEvent::class.java).subscribe {
//        colorRange = it.palette.colorRange
//        size = it.palette.size
//    }

    Column {
        Divider()
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            addTextField("Iterations -> Allowed: ", maxIterations.toString())
            addTextField("Used: ", usedIterations.toString())
            addTextField("RE Center: ", centerX.toString())
            addTextField("IM Center: ", centerY.toString())
            addTextField("Zoom: ", magnify.toString())
            addTextField("Width: ", width.toString())
            addTextField("Height: ", height.toString())
            addTextField("Color Range: ", colorRange.toString())
            addTextField("Palette Size: ", size.toString(), false)
        }
    }
}

@Composable
fun addTextField(labelText: String, boundProperty: String, addBorder: Boolean = true) {
    Surface {
        Row {
            Text(labelText, Modifier.padding(horizontal = 5.dp, vertical = 10.dp))
            Text(
                boundProperty,
                Modifier
                    .padding(end = 5.dp, top = 10.dp, bottom = 10.dp)
                    .drawBehind {
                        try {
                            check(addBorder)
                            val x = size.width + 5.dp.toPx()
                            drawLine(
                                Color.Black,
                                Offset(x, 0f),
                                Offset(x, size.height)
                            )
                        } catch (ex: Exception) {
                        }
                    }
            )
        }
    }
}

