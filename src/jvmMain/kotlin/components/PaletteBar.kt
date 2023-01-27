package components

import EventBus
import Palette
import action.PaletteAction
import action.PaletteEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
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
import rgbToHsv

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PaletteCanvas(pal: Palette) {
    val palette = remember { pal }
    val markerMap = mutableMapOf<Int, PaletteMarker>()
    var trigger by remember { mutableStateOf(0) }
    var index: Int
    var stripeWidth = 0
    var height = 0f
    var width: Float

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

            markerMap[index] = marker
            trigger += 1
        }
    }

    fun interpolate() {
        if (markerMap.count() < 2) return

        var startMarker: PaletteMarker? = null

        markerMap.forEach { (_, marker) ->
            if (startMarker == null) startMarker = marker
            else {
                val startColor = palette.colors[startMarker!!.index]
                val endColor = palette.colors[marker.index]
                val range = marker.index - startMarker!!.index + 1
                val startHSV = rgbToHsv(startColor)
                val endHSV = rgbToHsv(endColor)
                val hueInc = (endHSV.hue - startHSV.hue) / range.toFloat()
                val saturationInc = (endHSV.saturation - startHSV.saturation) / range.toFloat()
                val valueInc = (endHSV.value - startHSV.value) / range.toFloat()

                for (idx in 0 until range) {
                    val colorIndex = startMarker!!.index + idx
                    val hue = (startHSV.hue + hueInc * idx)// % 360.0
                    val saturation = (startHSV.saturation + saturationInc * idx)
                    val value = (startHSV.value + valueInc * idx)

                    palette.colors[colorIndex] = Color.hsv(hue, saturation, value, 1f)
                }

                startMarker = marker
            }
        }

        EventBus.publish(PaletteEvent(PaletteAction.CHANGED))
    }

    EventBus.listen(PaletteEvent::class.java).subscribe {
        if (it.action == PaletteAction.CHANGED) removeMarkers()
        else if (it.action == PaletteAction.INTERPOLATE) interpolate()
    }

    fun onMouseClicked(it: PointerEvent) {
        val positionX = it.changes.last().position.x

        it.awtEventOrNull?.consume()

        index = paletteIndex(positionX.toInt())
        setMarker(index)
    }

    Canvas(Modifier.height(48.dp).fillMaxWidth().onPointerEvent(PointerEventType.Release) { onMouseClicked(it) }) {
        height = size.height
        width = size.width
        trigger  // Don't remove, tricks app into recomposing

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
            Column {
                Button(onClick = {EventBus.publish(PaletteEvent(PaletteAction.INTERPOLATE))}){
                    Text("Interpolate between markers")
                }
            }
        }
    }
}
