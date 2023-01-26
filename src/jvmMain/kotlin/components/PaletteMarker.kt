package components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.Point
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.Polygon

class PaletteMarker(private val index: Int, private val height: Float, val fillColor: Color){
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