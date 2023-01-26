package components

import EventBus
import Palette
import action.NewPaletteEvent
import action.PaletteAction
import action.PaletteEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PaletteCanvas(pal: Palette) {
    val palette = remember { pal }
    val markerMap = mutableMapOf<Int, PaletteMarker>()
    var trigger by remember { mutableStateOf(0) }
    var index = 0
    var stripeWidth = 0
    var height = 0f
    var width = 0f

    fun paletteIndex(x: Int): Int {
        return x / stripeWidth
    }

    fun removeMarker(index: Int) {
        if (markerMap.containsKey(index)) {
            markerMap.remove(index)
            trigger += 1
        }
    }

    fun removeMarkers() {
        markerMap.clear()
        trigger += 1
    }

    fun setMarker(index: Int) {
        if (markerMap.containsKey(index)) {
            removeMarker(index)
        } else if (index < palette.size) {
            val fillColor = palette.colors[index]
            val marker = PaletteMarker(index, height / 2, fillColor)

//                with(marker) {
//                    layoutX = (index * stripeWidth + stripeWidth / 2).toDouble()
//                    layoutY = 0.0 //canvas.height / 2
//
//                    this@ColorBar.children.add(this)
//                }

            markerMap[index] = marker
            trigger += 1
        }

//            EventBus.publish(this)
    }

    EventBus.listen(PaletteEvent::class.java).subscribe {
        if (it.action == PaletteAction.CHANGED) removeMarkers()
    }

    fun onMouseClicked(it: PointerEvent) {
        val positionX = it.changes.last().position.x

        val setColor = {
//            palette.colors[index] = Color32.fromColor(colorChooser!!.value)
//            palette.colors[index].alpha = 1.0f

            removeMarker(index)
            setMarker(index)
        }

//        if (colorChooser == null) {
//            colorChooser = ColorPicker()
//            colorChooser!!.setOnAction { setColor() }
//        }

        index = paletteIndex(positionX.toInt())

//        if (it.button == MouseButton.SECONDARY) {
//            colorChooser!!.value = palette.colors[index]
//            colorChooser!!.show()
//        }

        setMarker(index)
    }

    Canvas(Modifier.height(48.dp).fillMaxWidth().onPointerEvent(PointerEventType.Release) { onMouseClicked(it) }) {
        height = size.height
        width = size.width
        val test = trigger  // Don't remove, tricks app into recomposing

        stripeWidth = (width / palette.size * palette.colorRange).toInt()

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

        val paint = Paint()
        val stroke = Paint()

        paint.mode = PaintMode.FILL
        stroke.mode = PaintMode.STROKE
        stroke.strokeWidth = 10f
        stroke.color = Color.Black.toArgb()

        markerMap.forEach { entry ->
            val marker = entry.value
            marker.setPoints(stripeWidth)

            drawIntoCanvas {
                paint.color = (marker.fillColor.toArgb() xor 0xffffff)
                it.nativeCanvas.drawPolygon(marker.points, paint = paint)
                it.nativeCanvas.drawPolygon(marker.points, paint = stroke)
            }
        }
    }
}

@Composable
fun PaletteBar() {
    val iconScale = 3.0f

    Surface(modifier = Modifier.fillMaxWidth(), elevation = 5.dp) {
        Row(Modifier.height(48.dp)) {
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.RANDOM)) },
                    Modifier.scale(iconScale)
                ) {
                    Icon(
                        painterResource("random32.png"),
                        "Random Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.SMOOTH)) },
                    Modifier.scale(iconScale)
                ) {
                    Icon(
                        painterResource("smooth32.png"),
                        "Smooth Palette",
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.DEFAULT)) },
                    Modifier.scale(iconScale)
                ) {
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
        }
    }
}
