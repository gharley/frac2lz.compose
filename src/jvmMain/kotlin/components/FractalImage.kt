package components

import Palette
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import state.FractalParameters
import javax.swing.BoxLayout

@Composable
fun FractalImage(params: FractalParameters, palette: Palette) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        shadowElevation = 5.dp
    ) {
        val parameters = remember { params }
        val fractalHeight = parameters.height
        val fractalWidth = parameters.width

        val zoomBox = remember { ZoomBox() }
        val swingImage = remember { SwingImage }

        swingImage.zoomBox = zoomBox
        swingImage.palette = palette
        swingImage.background = MaterialTheme.colorScheme.background

        SwingPanel(modifier = Modifier
            .size(width = fractalWidth.dp, height = fractalHeight.dp)
            .background(color = Color.Gray),
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