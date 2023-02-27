package components

import EventBus
import Fractal
import Palette
import ToolTip
import action.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import hslToRgb
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import rgbToHsl
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PaletteCanvas(pal: Palette, fractal: Fractal) {
    var palette by remember { mutableStateOf(pal) }
    val markerMap = remember { TreeMap<Int, PaletteMarker>() }
    var trigger by remember { mutableStateOf(0) }
    var subscribed by remember { mutableStateOf(false) }
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
                val divisor = (range + 1).toFloat()
                val startHSV = Color.rgbToHsl(startColor)
                val endHSV = Color.rgbToHsl(endColor)
                val hueInc = (endHSV.hue - startHSV.hue) / divisor
                val saturationInc = (endHSV.saturation - startHSV.saturation) / divisor
                val luminanceInc = (endHSV.luminance - startHSV.luminance) / divisor

                for (idx in 1 until range) {
                    val colorIndex = startMarker!!.index + idx
                    val hue = abs(startHSV.hue + hueInc * idx) % 360f
                    val saturation = abs(startHSV.saturation + saturationInc * idx) % 100f
                    val luminance = abs(startHSV.luminance + luminanceInc * idx) % 100f

                    palette.colors[colorIndex] = Color.hslToRgb(hue, saturation, luminance)
                }

                startMarker = marker
            }
        }

        EventBus.publish(PaletteEvent(PaletteAction.CHANGED))
    }

    if (!subscribed) {
        subscribed = true

        EventBus.listen(NewPaletteEvent::class.java).subscribe {
            palette = it.palette
            removeMarkers()
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
            } catch (_: Exception) {
            }
        }

//        EventBus.listen(CalculateEvent::class.java).subscribe { event ->
//            if (event.action == CalculateAction.COMPLETE) {
//                val colorsUsed = palette.colorsUsed(fractal.iterations)
//
//                removeMarkers()
//                colorsUsed.distinct().forEach { if (it != -1) setMarker(it) }
//            }
//        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaletteBar() {
    val iconScale = 3.0f
    val placement = TooltipPlacement.ComponentRect(Alignment.TopStart, Alignment.TopEnd)

    Surface(modifier = Modifier.fillMaxWidth().padding(10.dp), shadowElevation = 5.dp) {
        Row(Modifier.height(48.dp).padding(horizontal = 5.dp)) {
            ToolTip("Generates a random color palette.", placement = placement) {
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
            }
            ToolTip(
                "Uses a custom algorithm to generate a 'smooth' palette. Output will always be the same spread over palette size.",
                placement = placement
            ) {
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
            }
            ToolTip("Generates the default grayscale palette and sets color range to 1.", placement = placement) {
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
            ToolTip(
                "Allows creation of custom palette by interpolating between selected colors. Click on 2 or more color bars above, then click the interpolate button.",
                placement = placement
            ) {
                Column {
                    var enableButton by remember { mutableStateOf(false) }
                    var subscribed by remember { mutableStateOf(false) }

                    if (!subscribed) {
                        subscribed = true

                        EventBus.listen(PaletteEvent::class.java).subscribe {
                            if (it.action == PaletteAction.MARKERS_CHANGED) {
                                val enable = (it.data as Int > 1)

                                if (enableButton != enable) enableButton = enable
                            }
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
}
