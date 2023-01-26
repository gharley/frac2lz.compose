package components

import androidx.compose.ui.graphics.Color
import org.jetbrains.skia.Point

class PaletteMarker(private val index: Int = 0, private val height: Float = 0f, val fillColor: Color = Color.Black){
    var points: Array<Point> = arrayOf()
    private var offset: Float = 0f

    fun setPoints(stripeWidth: Int){
        offset = (index * stripeWidth + stripeWidth / 2).toFloat()

        points = arrayOf(
            Point(offset, height),
            Point((-height / 2) + offset, 0f),
            Point((height / 2) + offset, 0f)
        )
    }
}