package components

import Palette
import action.FractalEvent
import androidx.compose.ui.graphics.toArgb
import state.FractalParameters
import java.awt.Canvas
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.MemoryImageSource
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import javax.swing.JComponent
import javax.swing.JPanel

class SwingImage(params: FractalParameters, palette: Palette) : Canvas() {
    private val height = params.height.toInt()
    private val width = params.width.toInt()
    private var pixels = IntArray(width * height)
    private val source = MemoryImageSource(width, height, pixels, 0, width)
    private var image = createImage(source)

    init {
//        add(ZoomBox(this))

        EventBus.listen(FractalEvent::class.java).subscribe {
            val column = it.column
            val color = palette.color(it.data).toArgb()

            pixels[it.row * width + column] = color.rotateLeft(8)

            if (it.endOfRow) {
                source.newPixels(0, it.row, width, 1)
                update(graphics)
            }
        }
    }

    override fun paint(g: Graphics?) {
        val graphics = g as Graphics2D

        graphics.drawImage(image, null, null)
    }

    fun prepareForCalc(fractalWidth: Double, fractalHeight: Double, clear: Boolean = true) {
        pixels.fill(0xffd3d3d3.toInt())
        source.newPixels(0, 0, width, height)
//        with(canvas) {
//            if (clear) graphicsContext2D.clearRect(0.0, 0.0, width, height)
//
//            translateX = 0.0
//            translateY = 0.0
//            scaleX = 1.0
//            scaleY = 1.0
//
//            val parentBounds = parent.layoutBounds
//
//            = fractalWidth.toInt()
//            height = fractalHeight.toInt()
//
//            val scale: Double =
//                when {
//                    fractalWidth > fractalHeight -> when {
//                        parentBounds.width > parentBounds.height -> min(
//                            parentBounds.width / fractalWidth,
//                            parentBounds.height / fractalHeight
//                        )
//
//                        else -> parentBounds.width / fractalWidth
//                    }
//
//                    fractalWidth < fractalHeight -> when {
//                        parentBounds.width > parentBounds.height -> parentBounds.height / fractalHeight
//                        else -> 1.0
//                    }
//
//                    else -> min(parentBounds.width / fractalWidth, parentBounds.height / fractalHeight)
//                }
//
//            scaleX = scale
//            scaleY = scale
//            translateX = -boundsInParent.minX
//            translateY = -boundsInParent.minY
//
//            this@SwingImage.buffer = ByteArray((fractalWidth * 4).toInt())
//        }
    }

//        fun saveToImageFile(file: File, fileType: String = "png") {
//            val writableImage = WritableImage(canvas.width.toInt(), canvas.height.toInt())
//            val snapshotParams = SnapshotParameters()
//
//            snapshotParams.transform = Transform.scale(1.0 / canvas.scaleX, 1.0 / canvas.scaleY)
//            canvas.snapshot(snapshotParams, writableImage)
//            val renderedImage: java.awt.image.RenderedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null)
//            javax.imageio.ImageIO.write(renderedImage, fileType, file)
//        }
}