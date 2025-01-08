package components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb

class PaletteMarker(val index: Int = 0, private val height: Float = 0f, fillColor: Color){
    val color = Color(fillColor.toArgb() xor 0xffffff)

    fun getPath(stripeWidth: Int):Path{
        val offset = (index * stripeWidth + stripeWidth / 2).toFloat()
        val path = Path()

        path.moveTo(offset,height)
        path.lineTo((-stripeWidth / 2) + offset, 0f)
        path.lineTo((stripeWidth / 2) + offset, 0f)
        path.close()

        return path
    }
}