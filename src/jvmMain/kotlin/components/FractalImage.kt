package components

import Palette
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import state.FractalParameters
import javax.swing.BoxLayout
import javax.swing.JPanel

@Composable
fun FractalImage(params: FractalParameters, palette: Palette) {
    val parameters = remember { params }
    val fractalHeight = parameters.height
    val fractalWidth = parameters.width
    val swingImage = remember { SwingImage(params, palette) }

    Surface(Modifier.fillMaxSize(), elevation = 5.dp) {
        SwingPanel(modifier = Modifier
            .size(width = fractalWidth.dp, height = fractalHeight.dp)
            .background(color = Color.Gray),
            factory = {
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    add(swingImage)
                }
            },
            update = {
                swingImage.prepareForCalc(fractalWidth, fractalHeight)
            }
        )
//            val scaleValue =
//                when {
//                    size.width > size.height -> when {
//                        parentBounds.width > parentBounds.height -> min(
//                            parentBounds.width / fractalWidth.toFloat(),
//                            parentBounds.height / fractalHeight.toFloat()
//                        )
//
//                        else -> parentBounds.width / fractalWidth.toFloat()
//                    }
//
//                    fractalWidth < fractalHeight -> when {
//                        parentBounds.width > parentBounds.height -> parentBounds.height / fractalHeight.toFloat()
//                        else -> 1.0F
//                    }
//
//                    else -> min(
//                        parentBounds.width / fractalWidth.toFloat(),
//                        parentBounds.height / fractalHeight.toFloat()
//                    )
//                }
//
//            scale(scaleValue) {}

    }
}