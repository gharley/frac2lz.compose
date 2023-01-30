package components

import Palette
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import state.FractalParameters
import javax.swing.BoxLayout

@Composable
fun FractalImage(params: FractalParameters, pal: Palette) {
    Surface(Modifier.fillMaxSize(), elevation = 5.dp) {
        val parameters = remember { params }
        val palette by remember { mutableStateOf(pal) }
        val fractalHeight = parameters.height
        val fractalWidth = parameters.width

        val swingImage by remember { mutableStateOf(SwingImage(params, palette)) }

        SwingPanel(modifier = Modifier
            .size(width = fractalWidth.dp, height = fractalHeight.dp)
            .background(color = Color.Gray),
            factory = {
                swingImage.apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                }
            },
            update = {
                swingImage.palette = palette
            }
        )
    }
}