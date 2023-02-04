package components

import java.awt.*
import java.awt.event.*
import java.awt.geom.Point2D
import javax.swing.JPanel
import kotlin.math.abs

data class ZoomBoxEvent(val zoomBox: ZoomBox)
data class ImageClickEvent(val x: Double, val y: Double)

class ZoomBox(parent: JPanel) : Canvas() {
    internal var zoomXmin = 0
    private var zoomXmax = 0
    var zoomYmin = 0
    private var zoomYmax = 0
    private var zoomWidth = 0
    private var zoomHeight = 0

    private var dragStarted: Boolean = false
    private var moveStarted: Boolean = false
    private var moveOffset = Point2D.Double(0.0, 0.0)

    private var maintainAspect: Boolean = true
    private val strokeWidth = 2.0f
    private val strokeColor = Color.RED
    private val stroke = BasicStroke(strokeWidth)

    inner class ParentKeyListener() : KeyListener {
        override fun keyTyped(e: KeyEvent?) {}

        override fun keyPressed(e: KeyEvent?) {
            if (e!!.keyChar.code == KeyEvent.VK_ESCAPE) {
                isVisible = false
            } else if (e.keyChar.code == KeyEvent.VK_ENTER) {
                if (isVisible) {
                    isVisible = false
                    EventBus.publish(ZoomBoxEvent(this@ZoomBox))
                }
            }

            e.consume()
            this@ZoomBox.repaint()
        }

        override fun keyReleased(e: KeyEvent?) {}
    }

    inner class ParentMouseListener() : MouseListener {
//        fun adjustPoint(it: MouseEvent): Point2D {
//            return parent.screenToLocal(it.screenX, it.screenY)
//        }

        override fun mousePressed(e: MouseEvent?) {
            if (e!!.button == MouseEvent.BUTTON1) {
//                this@ZoomBox.isMouseTransparent = true

//                val adjustedPoint = adjustPoint(e)

                zoomXmin = e.x
                zoomYmin = e.y

                zoomWidth = 10
                zoomHeight = 10
                isVisible = true

                e.consume()
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (dragStarted) {
                dragStarted = false
//                this@ZoomBox.isMouseTransparent = false
            } else {
                isVisible = false

                val localPoint = e!! // adjustPoint(it)
                EventBus.publish(ImageClickEvent(localPoint.x.toDouble(), localPoint.y.toDouble()))
            }

            e!!.consume()
            this@ZoomBox.repaint()
        }

        override fun mouseClicked(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }

    inner class ParentMouseMoveListener() : MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            if (dragStarted) {
                fixEndpoints(Point2D.Double(e!!.x.toDouble(), e.y.toDouble()))
//                fixEndpoints(adjustPoint(it))
            } else {
                dragStarted = true
                isVisible = true
            }

            e!!.consume()
            this@ZoomBox.repaint()
        }

        override fun mouseMoved(e: MouseEvent?) {}
    }

    override fun update(g: Graphics?) {
        super.update(g)
    }

    override fun paint(g: Graphics?) {
        if (g == null) return

        val graphics = g as Graphics2D
        graphics.color = strokeColor
        graphics.stroke = stroke
        graphics.background = null

        graphics.drawRect(zoomXmin, zoomYmin, zoomWidth, zoomHeight)
    }

    override fun processMouseEvent(e: MouseEvent?) {
        super.processMouseEvent(e)

        when (e!!.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (isVisible) {
                    moveOffset = Point2D.Double((e.x - zoomXmin).toDouble(), (e.y - zoomYmin).toDouble())

                    moveStarted = true
                    e.consume()
                }
            }

            MouseEvent.MOUSE_RELEASED -> {
                moveStarted = false
            }

            MouseEvent.MOUSE_DRAGGED -> {
                if (moveStarted) {
                    zoomXmin = (e.x - moveOffset.x).toInt()
                    zoomYmin = (e.y - moveOffset.y).toInt()
                    e.consume()
                }
            }
        }
    }

    init {
        enableEvents(MouseEvent.MOUSE_EVENT_MASK)
//        isManaged = false
        isVisible = false
//        isMouseTransparent = true
//
//        xProperty().bindBidirectional(zoomXmin)
//        yProperty().bindBidirectional(zoomYmin)
//        widthProperty().bindBidirectional(zoomWidth)
//        heightProperty().bindBidirectional(zoomHeight)
//
//        setOnMousePressed {
//        }
//
//        setOnMouseReleased {
//        }
//
//        setOnMouseDragged {
//        }
        parent.addMouseListener(ParentMouseListener())
        parent.addMouseMotionListener(ParentMouseMoveListener())
        parent.addKeyListener(ParentKeyListener())
    }

//        image.setOnMousePressed {
//        }
//
//        image.setOnMouseDragged {
//        }

//        image.setOnMouseReleased {
//        }
//
//        Platform.runLater {
//            stage.scene.addEventFilter(KeyEvent.KEY_PRESSED) {
//            }
//        }

    private fun fixEndpoints(it: Point2D) {
        if (it.x.toInt() < zoomXmin) {
            zoomXmax = zoomXmin
            zoomXmin = it.x.toInt()
        } else zoomXmax = it.x.toInt()

        if (it.y < zoomYmin) {
            zoomYmax = zoomYmin
            zoomYmin = it.y.toInt()
        } else zoomYmax = it.y.toInt()

        zoomWidth = abs(zoomXmax - zoomXmin)

        if (maintainAspect) zoomHeight = zoomWidth * (parent.height / parent.width)
        else zoomHeight = abs(zoomYmax - zoomYmin)
    }
}
