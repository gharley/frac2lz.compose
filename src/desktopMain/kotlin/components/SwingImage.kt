package components

import EventBus
import Palette
import action.*
import androidx.compose.ui.graphics.toArgb
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.awt.image.IndexColorModel
import java.io.File
import java.lang.Double.min
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.reflect.KProperty

open class SwingImageClass :
    JPanel() {
    var scale = 0.0
    val colorModelSize = 256

    fun createSwingImage(clear: Boolean = true) {
        if (imgHeight != 0 && imgWidth != 0) {
            if (clear) {
                pixels = IntArray(imgWidth * imgHeight)
                pixels.fill(0)
            }

            val zStart = firstPoint.zStart
            val z = firstPoint.z
            val zInc = (lastPoint.zStart - zStart) / colorModelSize.toDouble()

            indexColors.fill(0)

            for (i in 0..palette!!.size % colorModelSize) {
                val inc = zStart + zInc * i.toDouble()
                val data = FractalPointData(i + 1L, firstPoint.maxIterations, zStart + inc, z + inc)

                indexColors[i] = palette!!.color(data).toArgb()
            }

            val colorModel = IndexColorModel(8, colorModelSize, indexColors, 0, false, -1, DataBuffer.TYPE_BYTE)

            image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_INDEXED, colorModel)
            image.raster.setDataElements(0, 0, imgWidth, imgHeight, pixels.toByteArray())
            repaint()
        }
    }

    private var colorOffset = 0;

    private fun rotatePalette() {
        val shifted = IntArray(colorModelSize)

        for (i in 0 until colorModelSize) {
            val sourceIndex = (i + colorOffset) % colorModelSize

            shifted[i] = indexColors[sourceIndex]
        }

        // Recreate the IndexColorModel with the shifted color bytes
        val colorModel = IndexColorModel(8, colorModelSize, shifted, 0, false, -1, DataBuffer.TYPE_BYTE)

        // Re-assign the color model to the existing image raster data
        // Note: TYPE_BYTE_INDEXED images bind their color model tightly,
        // so generating a new BufferedImage wrapper around the same raster or
        // updating the model is necessary depending on your exact pipeline.
        val raster = image.raster

        image = BufferedImage(colorModel, raster, false, null)

        colorOffset++
        colorOffset %= colorModelSize

        repaint()
    }
    private class Delegate<T>(private var value: T) {
        operator fun getValue(swingImageClass: SwingImageClass, property: KProperty<*>): T {
            return value
        }

        operator fun setValue(swingImageClass: SwingImageClass, property: KProperty<*>, t: T) {
            if (value == t) return
            value = t

            if (property.name == "imgHeight" || property.name == "imgWidth") {
                swingImageClass.createSwingImage()
            }
        }
    }

    var imgHeight: Int by Delegate(1)
    var imgWidth: Int by Delegate(1)

    var palette: Palette? = null
    var zoomBox: ZoomBox? = null
    var firstPoint = FractalPointData()
    var lastPoint = FractalPointData()
    private var pixels = IntArray(imgWidth * imgHeight)
    private lateinit var image: BufferedImage
    private val imageTransform = AffineTransform()
    private val indexColors = IntArray(colorModelSize)

    private var refreshRate = UISettings().refreshRate

    class MouseMoveListener : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent?) {
            EventBus.publish(UIEvent(UIAction.MOUSE_ENTER, "image"))
        }

        override fun mouseExited(e: MouseEvent?) {
            EventBus.publish(UIEvent(UIAction.MOUSE_EXIT, "image"))
        }
    }

    init {
        isDoubleBuffered = true
        isOpaque = false

        Timer().schedule(object : TimerTask() {
            override fun run() {
                image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_INDEXED)

                if (zoomBox == null) {
                    zoomBox = ZoomBox()
                    add(zoomBox)
                }
            }
        }, 100)

        EventBus.listen(FractalSizeEvent::class.java).subscribe {
            imgHeight = 0
            imgWidth = it.width.toInt()
            imgHeight = it.height.toInt()
            prepareForCalc(false)
        }

        EventBus.listen(FractalEvent::class.java).subscribe {
            val color = (it.data.iterations % colorModelSize).toInt()

            if (it.row == 0 && it.column == 0) {
                prepareForCalc()
                firstPoint = it.data
            }

            try {
                pixels[it.row * imgWidth + it.column] = color

                if (it.endOfRow) {
                    lastPoint = it.data

                    if ((refreshRate < 100 && it.row % refreshRate == 0) || it.row == imgHeight - 1) {
                        image.raster.setDataElements(0, 0, imgWidth, imgHeight, pixels.toByteArray())
                        repaint()
                    }
                }
            } catch (_: Exception) {
            }
        }

        EventBus.listen(CalculateEvent::class.java).subscribe {
            if (it.action == CalculateAction.COMPLETE)
                zoomBox!!.isEnabled = true
        }

        EventBus.listen(UIEvent::class.java).subscribe {
            if (it.action == UIAction.SETTINGS) {
                refreshRate = (it.data as UISettings).refreshRate
            }
        }

        EventBus.listen(FileEvent::class.java).subscribe {
            if (it.action == FileAction.WRITE_IMAGE) {
                saveToImageFile(it.data)
            }
        }

        EventBus.listen(PaletteEvent::class.java).subscribe {
            if (it.action == PaletteAction.ANIMATE) rotatePalette()
        }

        addMouseListener(MouseMoveListener())
    }

    override fun paint(g: Graphics?) {
        if (g == null) return

        val graphics = g as Graphics2D

        scaleImage()
        graphics.drawImage(image, imageTransform, null)
    }

    private fun prepareForCalc(clear: Boolean = true) {
        if (clear) {
            pixels = IntArray(imgWidth * imgHeight)
            pixels.fill(0)
        }

        prepareImage(image, null)
        repaint(0, 0, width, height)

        zoomBox!!.isEnabled = false
//        scaleImage()
    }

    private fun scaleImage() {
        val bounds = getBounds(null)
        val height = imgHeight.toDouble()
        val width = imgWidth.toDouble()

        imageTransform.setToIdentity()
        scale =
            when {
                width > height -> when {
                    bounds.width > bounds.height -> min(
                        bounds.width / width,
                        bounds.height / height
                    )

                    else -> bounds.width / width
                }

                width < height -> when {
                    bounds.width > bounds.height -> bounds.height / height
                    else -> 1.0
                }

                else -> min(bounds.width / width, bounds.height / height)
            }

        imageTransform.scale(scale, scale)
    }

    private fun saveToImageFile(filename: String) {
        try {
            val imageOut = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB)
            val g2d = imageOut.createGraphics()

            g2d.drawImage(image, 0, 0, null)
            g2d.dispose()

            ImageIO.write(imageOut, "png", File(filename))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun IntArray.toByteArray(): Any {
    return ByteArray(this.size) { i -> this[i].toByte() }
}

object SwingImage : SwingImageClass() {
    private fun readResolve(): Any = SwingImage
}