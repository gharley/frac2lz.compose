package components

import Palette
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import state.FractalState
import kotlin.math.min

@Composable
fun FractalImage(fractalState: FractalState, palette: Palette) {
    var parentBounds = remember { Size.Zero }
    val fractalHeight = fractalState.parameters.height
    val fractalWidth = fractalState.parameters.width

    Surface(Modifier.onGloballyPositioned { coords -> parentBounds = coords.size.toSize() }, elevation = 5.dp) {
        Canvas(
            Modifier.size(width = fractalState.parameters.width.dp, height = fractalState.parameters.height.dp)
                .background(color = Color.Gray).onGloballyPositioned { coords -> parentBounds = coords.size.toSize() }
        ) {
            var scaleValue =
                when {
                    size.width > size.height -> when {
                        parentBounds.width > parentBounds.height -> min(parentBounds.width / fractalWidth.toFloat(), parentBounds.height / fractalHeight.toFloat())
                        else -> parentBounds.width / fractalWidth.toFloat()
                    }
                    fractalWidth < fractalHeight -> when {
                        parentBounds.width > parentBounds.height -> parentBounds.height / fractalHeight.toFloat()
                        else -> 1.0F
                    }
                    else -> min(parentBounds.width / fractalWidth.toFloat(), parentBounds.height / fractalHeight.toFloat())
                }

            scale(scaleValue){}

//            val pixels = IntArray(fractalState.parameters.width.toInt())
//            val bitmap = Bitmap()
//
//            pixels[fractalState.event.column] = palette.color(fractalState.event.data).value.toInt()
//
//            if (fractalState.event.endOfRow) {
//                drawIntoCanvas {
//                    bitmap
//                    it.nativeCanvas.writePixels(bitmap, 0, fractalState.event.row)
//                    it.nativeCanvas
//                }
//            }
//
//            if (rowList.isNotEmpty()) {
////                lock.lock()
//                try {
//                    val writer = graphicsContext2D.pixelWriter
//
//                    rowList.forEach { (k, v) ->
//                        writer.setPixels(0, k, size.width.toInt(), 1, PixelFormat.getByteBgraInstance(), v, 0, v.size)
//                    }
//
//                    rowList.clear()
//                } finally {
////                    lock.unlock()
//                }
//            }
//        }
        }
    }
}