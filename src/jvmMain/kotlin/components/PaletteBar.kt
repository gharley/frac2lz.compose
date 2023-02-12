package components

import EventBus
import Fractal
import Palette
import action.ImageClickEvent
import action.NewPaletteEvent
import action.PaletteAction
import action.PaletteEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PaletteCanvas(pal: Palette, fractal: Fractal) {
    var palette by remember { mutableStateOf(pal) }
    val markerMap = TreeMap<Int, PaletteMarker>()
    var trigger by remember { mutableStateOf(0) }
    var index: Int
    var stripeWidth = 0
    var height = 0f
    var width: Float

    fun publishChanges() {
        EventBus.publish(PaletteEvent(PaletteAction.MARKERS_CHANGED, markerMap.count()))
    }

    fun paletteIndex(x: Int): Int {
        return x / stripeWidth
    }

    fun removeMarker(index: Int) {
        if (markerMap.containsKey(index)) {
            markerMap.remove(index)
            trigger++
            publishChanges()
        }
    }

    fun removeMarkers() {
        markerMap.clear()
        trigger++
        publishChanges()
    }

    fun setMarker(index: Int) {
        if (markerMap.containsKey(index)) {
            removeMarker(index)
        } else if (index < palette.size) {
            val fillColor = palette.colors[index]
            val marker = PaletteMarker(index, height / 2, fillColor)

            markerMap[index] = marker
            trigger++
            publishChanges()
        }
    }

    fun setMarkerFromIterations(iterations: Long) {
        if (iterations == -1L) return
        setMarker(palette.indexFromIterations(iterations))
    }

    fun interpolate() {
        if (markerMap.count() < 2) return

        var startMarker: PaletteMarker? = null

        markerMap.forEach { (_, marker) ->
            if (startMarker == null) startMarker = marker
            else {
                val startColor = palette.colors[startMarker!!.index]
                val endColor = palette.colors[marker.index]
                val range = marker.index - startMarker!!.index
                val startHSV = rgbToHsv(startColor)
                val endHSV = rgbToHsv(endColor)
                val hueInc = (endHSV.hue - startHSV.hue) / range.toFloat()
                val saturationInc = (endHSV.saturation - startHSV.saturation) / range.toFloat()
                val valueInc = (endHSV.value - startHSV.value) / range.toFloat()

                for (idx in 1 until range) {
                    val colorIndex = startMarker!!.index + idx
                    val hue = (startHSV.hue + hueInc * idx) % 360f
                    val saturation = (startHSV.saturation + saturationInc * idx)
                    val value = (startHSV.value + valueInc * idx)

                    palette.colors[colorIndex] = Color.hsv(hue, saturation, value, 1f)
                }

                startMarker = marker
            }
        }

        EventBus.publish(PaletteEvent(PaletteAction.CHANGED))
    }

    EventBus.listen(NewPaletteEvent::class.java).subscribe {
        palette = it.palette
    }

    EventBus.listen(PaletteEvent::class.java).subscribe {
        if (it.action == PaletteAction.CHANGED) removeMarkers()
        else if (it.action == PaletteAction.INTERPOLATE) interpolate()
    }

    EventBus.listen(ImageClickEvent::class.java).subscribe {
        val scale = (it.image as SwingImage).scale
        val x = (it.x / scale).toInt()
        val y = (it.y / scale).toInt()

        try {
            val iterations = fractal.iterations[y][x]
            setMarkerFromIterations(iterations)
        }catch (_: Exception){}
    }

    fun onMouseClicked(it: PointerEvent) {
        val positionX = it.changes.last().position.x

        it.awtEventOrNull?.consume()

        index = paletteIndex(positionX.toInt())
        setMarker(index)
    }

    Canvas(Modifier
        .height(48.dp)
        .fillMaxWidth()
        .background(Color.Black)
        .onPointerEvent(PointerEventType.Release) { onMouseClicked(it) }) {
        height = size.height
        width = size.width
        trigger  // Don't remove, tricks app into recomposing

        stripeWidth = (width / palette.size).toInt()

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

    Surface(modifier = Modifier.fillMaxWidth().padding(10.dp), shadowElevation = 5.dp) {
        Row(Modifier.height(48.dp).padding(horizontal = 5.dp)) {
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.RANDOM)) },
                ) {
                    Icon(
                        painterResource("random32.png"),
                        "Random Palette",
                        Modifier.scale(iconScale),
                        tint = Color.Unspecified,
                    )
                }
            }
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.SMOOTH)) },
                ) {
                    Icon(
                        painterResource("smooth32.png"),
                        "Smooth Palette",
                        Modifier.scale(iconScale),
                        tint = Color.Unspecified
                    )
                }
            }
            Column {
                IconButton(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.DEFAULT)) },
                ) {
                    Icon(
                        painterResource("default32.png"),
                        "Default Palette",
                        Modifier.scale(iconScale),
                        tint = Color.Unspecified
                    )
                }
            }
//            Column {
//                IconButton(onClick = {}) {
//                    Icon(
//                        painterResource("animate32.png"),
//                        "Animate Palette",
//                        Modifier.scale(iconScale),
//                        tint = Color.Unspecified
//                    )
//                }
//            }
            Column {
                var enableButton by remember { mutableStateOf(false) }

                EventBus.listen(PaletteEvent::class.java).subscribe {
                    if (it.action == PaletteAction.MARKERS_CHANGED) {
                        val enable = (it.data as Int > 1)

                        if (enableButton != enable) enableButton = enable
                    }
                }

                Button(
                    onClick = { EventBus.publish(PaletteEvent(PaletteAction.INTERPOLATE)) },
                    enabled = enableButton
                ) {
                    Text("Interpolate between markers")
                }
            }
        }
    }
}
