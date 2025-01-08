package com.resmass.frac2lz

import androidx.compose.ui.graphics.Color
import java.io.Serializable
import kotlin.math.abs

class Color32(var red: Double, var green: Double, var blue: Double, var alpha: Float = 1.0f) : Serializable {
    constructor() : this(0.0, 0.0, 0.0)

    companion object {
//        fun bgraFromRgb(red: Double, green: Double, blue: Double) : Int{
//            return (0xff + ((red * 255).toByte() * 0x100) + ((green * 255).toByte() * 0x10000) + ((blue * 255).toByte() * 0x1000000))
//        }

        fun fromColor(color: Color): Color32 {
            return Color32(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble(), color.alpha)
        }
    }

    fun toColor(): Color {
        return Color(abs(red).toFloat(), abs(green).toFloat(), abs(blue).toFloat(), alpha)
    }

    fun toByteValue(value: Double): Byte {
        return (value * 255).toInt().toByte()
    }
}