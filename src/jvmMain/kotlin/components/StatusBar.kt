package components

import EventBus
import Palette
import action.FractalEvent
import action.NewPaletteEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    var colorRange = remember { palette.colorRange }
    var size = remember { palette.size }

    EventBus.listen(FractalEvent::class.java).subscribe {
        maxIterations = it.data.maxIterations
        usedIterations = it.data.iterations
    }

    EventBus.listen(FractalParameters::class.java).subscribe{
        centerX = it.centerX
        centerY = it.centerY
        magnify = it.magnify
        width = it.width.toInt()
        height = it.height.toInt()
    }
//
//    EventBus.listen(NewPaletteEvent::class.java).subscribe {
//        colorRange = it.palette.colorRange
//        size = it.palette.size
//    }

    Column(Modifier.fillMaxWidth().padding(2.dp)) {
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
                        } catch (_: Exception) {
                        }
                    }
            )
        }
    }
}

