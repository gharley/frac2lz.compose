package components

import EventBus
import Palette
import action.FractalEvent
import androidx.compose.ui.graphics.toArgb
import state.FractalParameters
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.MemoryImageSource
import java.lang.Double.min
import javax.swing.JPanel

class SwingImage(private val params: FractalParameters, palette: Palette, onChange: () -> Unit ) :
    JPanel() {
    private val height = params.height.toInt()
    private val width = params.width.toInt()
    private var pixels = IntArray(width * height)
    private val source = MemoryImageSource(width, height, pixels, 0, width)
    private var image = createImage(source)
    private val imageTransform = AffineTransform()

    private var currentRow = 0

    init {
        source.setAnimated(true)
//        add(ZoomBox(this))

        EventBus.listen(FractalEvent::class.java).subscribe {
            val color = palette.color(it.data).toArgb()

            if (it.row == 0 && it.column == 0){
                prepareForCalc()
            }

            pixels[it.row * width + it.column] = color

            if (it.endOfRow) {
                source.newPixels(0, it.row, width, 1)
                if (it.row == 0) update(graphics)
                currentRow = it.row
                onChange()
            }
        }
    }

    override fun paint(g: Graphics?) {
        val graphics = g as Graphics2D

        scale()
        graphics.drawImage(image, imageTransform, null)
    }

    fun update(){
        if (graphics != null) update(graphics)
    }

    fun prepareForCalc(clear: Boolean = true) {
        if (clear) {
            pixels.fill(0xff000000.toInt())
            prepareImage(image, null)
            source.newPixels(0, 0, width, height)
        }

        scale()
    }

    fun scale(){
        val bounds = getBounds(null)
        val height = params.height
        val width = params.width

        imageTransform.setToIdentity()
            val scale: Double =
                when {
                    width > height -> when {
                        bounds.width > bounds.height -> min(
                            bounds.width / width,
                            bounds.height / height
                        )

                        else ->bounds.width / width
                    }

                    width < height -> when {
                        bounds.width > bounds.height -> bounds.height / height
                        else -> 1.0
                    }

                    else -> min(bounds.width / width, bounds.height / height)
                }

            imageTransform.scale(scale, scale)
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