package components

import EventBus
import Palette
import action.*
import androidx.compose.ui.graphics.toArgb
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.MemoryImageSource
import java.io.File
import java.lang.Double.min
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.reflect.KProperty

open class SwingImageClass() :
    JPanel() {
    fun createSwingImage() {
        if (imgHeight != 0 && imgWidth != 0) {
            pixels = IntArray(imgWidth * imgHeight)
            source = MemoryImageSource(imgWidth, imgHeight, pixels, 0, imgWidth)
            image = createImage(source)
            source.setAnimated(true)
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

    var palette = Palette()
    var imgHeight: Int by Delegate(1)
    var imgWidth: Int by Delegate(1)

    private var pixels = IntArray(imgWidth * imgHeight)
    private var source = MemoryImageSource(imgWidth, imgHeight, pixels, 0, imgWidth)
    private var image = createImage(source)
    private val imageTransform = AffineTransform()
    private var refreshRate = UISettings().refreshRate

    init {
//        add(ZoomBox(this))

        EventBus.listen(FractalEvent::class.java).subscribe {
            val color = palette.color(it.data).toArgb()

            if (it.row == 0 && it.column == 0) {
                prepareForCalc()
            }

            pixels[it.row * imgWidth + it.column] = color

            if (it.endOfRow) {
                source.newPixels(0, it.row, imgWidth, 1)
                if ((refreshRate < 100 && it.row % refreshRate == 0) || it.row == imgHeight - 1) update(graphics)
            }
        }

        EventBus.listen(UIEvent::class.java).subscribe{
            if (it.action == UIAction.SETTINGS){
                refreshRate = (it.data as UISettings).refreshRate
            }
        }

        EventBus.listen(FileEvent::class.java).subscribe {
            if (it.action == FileAction.WRITE_IMAGE) {
                saveToImageFile(it.data)
            }
        }
    }

    override fun paint(g: Graphics?) {
        if (g == null) return

        val graphics = g as Graphics2D

        scale()
        graphics.drawImage(image, imageTransform, null)
    }

    private fun prepareForCalc(clear: Boolean = true) {
//        pixels = IntArray(width * height)
//        source = MemoryImageSource(width, height, pixels, 0, width)
//        image = createImage(source)

        if (clear) {
            pixels.fill(0xffefefef.toInt())
            prepareImage(image, null)
            source.newPixels(0, 0, imgWidth, imgHeight)
        }

        scale()
    }

    private fun scale() {
        val bounds = getBounds(null)
        val height = imgHeight.toDouble()
        val width = imgWidth.toDouble()

        imageTransform.setToIdentity()
        val scale: Double =
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

object SwingImage : SwingImageClass() {}