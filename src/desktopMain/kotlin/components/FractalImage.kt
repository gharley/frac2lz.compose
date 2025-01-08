package components

import FractalParameters
import Palette
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import javax.swing.BoxLayout

@Composable
fun FractalImage(params: FractalParameters, palette: Palette) {
    Surface(
        elevation = 5.dp
    ) {
        val parameters = remember { params }
        val fractalHeight = parameters.height
        val fractalWidth = parameters.width

        val swingImage = remember { SwingImage }

        swingImage.palette = palette
        swingImage.background = java.awt.Color(MaterialTheme.colors.background.toArgb())

        SwingPanel(modifier = Modifier.fillMaxSize(),
            background = MaterialTheme.colors.background,
            factory = {
                swingImage.apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    imgHeight = fractalHeight.toInt()
                    imgWidth = fractalWidth.toInt()
                }
            },
            update = {
                swingImage.palette = palette
            }
        )
    }
}