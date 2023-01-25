package components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Stroke

class PaletteMarker(val height: Double, private val fillColor: Color, val index: Int) :
    Polygon(){
    init {
        this.addPoint(0, height.toInt())
        this.addPoint((-height / 2).toInt(), 0)
        this.addPoint((height / 2).toInt(), 0)
    }

    fun draw(g2d: Graphics2D){
        g2d.background = java.awt.Color(fillColor.toArgb())
        g2d.color = java.awt.Color.BLACK
        g2d.drawPolygon(this)
    }
}