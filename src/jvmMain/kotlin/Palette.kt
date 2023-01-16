import action.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sin
import kotlin.reflect.KProperty

class Palette(initSize: Int = 64) {
    private class Delegate<T>(private var value: T) {
        operator fun getValue(palette: Palette, property: KProperty<*>): T {
            return value
        }

        operator fun setValue(palette: Palette, property: KProperty<*>, t: T) {
            if (value == t) return
            value = t

            if (property.name == "size") {
                when (palette.paletteType) {
                    PaletteType.GRAY_SCALE -> palette.buildPalette(palette.grayScaleColor)
                    PaletteType.RANDOM -> palette.buildPalette(palette.randomColor)
                    PaletteType.SMOOTH -> palette.buildPalette(palette.smoothColor)
                }
            }
        }
    }

    enum class PaletteType {
        GRAY_SCALE, RANDOM, SMOOTH
    }

    //    private val rotationTimer: RotationTimer = RotationTimer()
    var paletteType = PaletteType.GRAY_SCALE
        private set

    internal var colors = Array(initSize) { Color32() }


    var size: Int by Delegate(initSize)
    var getColorFromFractal: Boolean by Delegate(false)
    var useSecondarySmoothing: Boolean by Delegate(false)
    var colorRange: Int by Delegate(1)
    var refineRange: Int by Delegate(0)

    private val grayScaleColor: (Int) -> Color32 = {
        val value = (it + 1) / size.toFloat()

        Color32(value, value, value)
    }

    private val randomColor: (Int) -> Color32 =
        { Color32(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat()) }

    private val smoothColor: (Int) -> Color32 = {
        Color32(
            ((sin(0.016 * it / size + 4) * 230 + 25) % 1.0).toFloat(),
            ((sin(0.013 * it / size + 2) * 230 + 25) % 1.0).toFloat(),
            ((sin(0.01 * it / size + 1) * 230 + 25) % 1.0).toFloat()
        )
    }

    init {
        EventBus.listen(UIEvent::class.java).subscribe {
            if (it.action == UIAction.CHANGE) fireUpdate()
        }

        EventBus.listen(PaletteEvent::class.java).subscribe {
            when (it.action) {
//            PaletteAction.ANIMATE -> startAnimation(fractalImage.canvas)
                PaletteAction.DEFAULT -> buildDefaultPalette()
                PaletteAction.RANDOM -> buildRandomPalette()
                PaletteAction.SMOOTH -> buildSmoothPalette()
                else -> {
                }
            }
        }

        size = initSize
        buildDefaultPalette()
    }

//    class RotationTimer : AnimationTimer() {
//        private val nanosecondsPerSecond: Long = 1e9.toLong()
//
//        private var effect = ColorAdjust()
//        private var interval: Long = (nanosecondsPerSecond * 0.25).toLong()
//        private var then: Long = 0
//
//        var image: Canvas? = null
//        var palette: Palette? = null
//
//        fun initialize(image: Canvas, palette: Palette) {
//            this.image = image
//            this.palette = palette
//            interval = (nanosecondsPerSecond / palette.size * 2)
//            effect = ColorAdjust()
//
//            if (palette.paletteType == PaletteType.GRAY_SCALE) {
//                effect.contrast = -1.0 / 120
//            } else {
//                effect.hue = -1.0 / 120//palette.size
//                effect.saturation = 1.0 / 120//palette.size
//            }
//        }
//
//        override fun handle(now: Long) {
//            if (now - then >= interval) {
//                image!!.graphicsContext2D.applyEffect(effect)
//            }
//        }
//    }

    private fun buildPalette(colorFunc: (Int) -> Color32) {
        colors = Array(size) { idx ->
            colorFunc(idx)
        }

        fireUpdate()
    }

    private fun buildDefaultPalette() {
        paletteType = PaletteType.GRAY_SCALE
        buildPalette(grayScaleColor)
    }

    private fun buildRandomPalette() {
        paletteType = PaletteType.RANDOM
        buildPalette(randomColor)
    }

    private fun buildSmoothPalette() {
        paletteType = PaletteType.SMOOTH
        buildPalette(smoothColor)
    }

    fun color(value: FractalPointData): Color32 {
        if (value.iterations == -1L) return Color32()

        if (getColorFromFractal && !(value.z.real == -10.0 || value.z.imaginary == -10.0)) return colorFromComplex(
            value
        )

        return colors[indexFromIterations(value.iterations)]
    }

    fun indexFromIterations(iterations: Long): Int {
        return ((iterations / colorRange) % colors.size).toInt()
    }

    // Thanks to Rod Stephens - http://csharphelper.com/blog/2014/07/draw-a-mandelbrot-set-fractal-with-smoothly-shaded-colors-in-c/
    private val logEscape = ln(2.0)

    private fun colorFromComplex(value: FractalPointData): Color32 {
        if (value.iterations == -1L) return Color32()

        var z = value.z
        for (idx in 0 until refineRange) {
            z = z * z + value.zStart
        }

        var mu = abs(value.iterations + 1 - ln(ln(z.hypotenuse())) / logEscape)

        if (useSecondarySmoothing) mu /= (value.maxIterations / colors.size.toDouble())

        var clr1 = mu.toInt()
        val t2 = mu - clr1
        val t1 = 1 - t2
        clr1 = (clr1 / colorRange) % colors.size
        val clr2 = (clr1 + 1) % colors.size

        val r = ((colors[clr1].red * t1 + colors[clr2].red * t2)).toFloat()
        val g = ((colors[clr1].green * t1 + colors[clr2].green * t2)).toFloat()
        val b = ((colors[clr1].blue * t1 + colors[clr2].blue * t2)).toFloat()

        return Color32(r, g, b)
    }

    private fun fireUpdate() {
        EventBus.publish(NewPaletteEvent(this))
    }

//    private var animating: Boolean = false
//
//    fun startAnimation(image: Canvas) {
//        if (animating) stopAnimation()
//        else {
//            rotationTimer.initialize(image, this)
//            rotationTimer.start()
//            animating = true
//        }
//    }
//
//    fun stopAnimation() {
//        rotationTimer.stop()
//        animating = false
//    }

    fun readObject(stream: ObjectInputStream) {
        try {
            size = stream.readInt()
            colorRange = stream.readInt()
            @Suppress("UNCHECKED_CAST")
            colors = stream.readObject() as Array<Color32>

            fireUpdate()
        } catch (_: Exception) {
        }
    }

    fun writeObject(stream: ObjectOutputStream) {
        stream.writeInt(size)
        stream.writeInt(colorRange)
        stream.writeObject(colors)
    }
}

