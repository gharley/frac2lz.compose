package components

import EventBus
import Palette
import action.*
import androidx.compose.ui.graphics.toArgb
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBuffer
import java.awt.image.DirectColorModel
import java.awt.image.IndexColorModel
import java.awt.image.MemoryImageSource
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
            if (clear){
                pixels = ByteArray(imgWidth * imgHeight)
                pixels.fill(0)
            }

            val indexColors = IntArray(colorModelSize)
            indexColors.fill(0)

            for (i in 0..palette!!.size % colorModelSize) {
                indexColors[i] = palette!!.color(FractalPointData(i.toLong())).toArgb()
            }

            val colorModel = IndexColorModel(8, colorModelSize, indexColors, 0, false, -1, DataBuffer.TYPE_BYTE)

            image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_INDEXED, colorModel)
            image.raster.setDataElements(0, 0, imgWidth, imgHeight, pixels)
            repaint()
        }
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

    private var pixels = ByteArray(imgWidth * imgHeight)
    private lateinit var image: BufferedImage
    private val imageTransform = AffineTransform()
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
            val color = (it.data.iterations % colorModelSize).toByte()

            if (it.row == 0 && it.column == 0) {
                prepareForCalc()
            }

            try {
                pixels[it.row * imgWidth + it.column] = color

                if (it.endOfRow) {
                    if ((refreshRate < 100 && it.row % refreshRate == 0) || it.row == imgHeight - 1) {
                        image.raster.setDataElements(0, 0, imgWidth, imgHeight, pixels)
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
            pixels = ByteArray(imgWidth * imgHeight)
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

object SwingImage : SwingImageClass() {
    private fun readResolve(): Any = SwingImage
}