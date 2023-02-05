package components

import java.awt.*
import java.awt.event.*
import java.awt.geom.Point2D
import javax.swing.JPanel
import kotlin.math.abs

data class ZoomBoxEvent(val zoomBox: ZoomBox)
data class ImageClickEvent(val x: Double, val y: Double)

class ZoomBox(parent: JPanel) : Component() {
    internal var zoomXmin = 0.0
    private var zoomXmax = 0.0
    var zoomYmin = 0.0
    private var zoomYmax = 0.0
    private var zoomWidth = 0.0
    private var zoomHeight = 0.0

    private var dragStarted: Boolean = false
    private var moveStarted: Boolean = false
    private var moveOffset = Point2D.Double(0.0, 0.0)

    private var maintainAspect: Boolean = true
    private val strokeWidth = 2.0f
    private val strokeColor = Color.RED
    private val stroke = BasicStroke(strokeWidth)

    inner class ParentKeyListener() : KeyListener {
        override fun keyTyped(e: KeyEvent?) {e!!.consume()}

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
            this@ZoomBox.setBounds()
        }

        override fun keyReleased(e: KeyEvent?) {e!!.consume()}
    }

    fun adjustPoint(it: MouseEvent): Point2D {
        return Point2D.Double(it.x.toDouble(), it.y.toDouble())
//            return parent.screenToLocal(it.screenX, it.screenY)
    }

    inner class ParentMouseListener() : MouseListener {
        override fun mousePressed(e: MouseEvent?) {
            if (e!!.button == MouseEvent.BUTTON1) {
                val adjustedPoint = adjustPoint(e)

                zoomXmin = adjustedPoint.x
                zoomYmin = adjustedPoint.y

                zoomWidth = 10.0
                zoomHeight = 10.0
                isVisible = true
                this@ZoomBox.setBounds()

                e.consume()
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (dragStarted) {
                dragStarted = false
            } else {
                isVisible = false

                val localPoint = adjustPoint(e!!)
                EventBus.publish(ImageClickEvent(localPoint.x, localPoint.y))
            }

            e!!.consume()
            this@ZoomBox.setBounds()
        }

        override fun mouseClicked(e: MouseEvent?) {e!!.consume()}
        override fun mouseEntered(e: MouseEvent?) {e!!.consume()}
        override fun mouseExited(e: MouseEvent?) {e!!.consume()}
    }

    inner class ParentMouseMoveListener() : MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            if (dragStarted) {
                fixEndpoints(adjustPoint(e!!))
            } else {
                dragStarted = true
                isVisible = true
            }

            e!!.consume()
        }

        override fun mouseMoved(e: MouseEvent?) {e!!.consume()}
    }

    fun setBounds(){
        setBounds(zoomXmin.toInt(), zoomYmin.toInt(),zoomWidth.toInt(),zoomHeight.toInt())
//        invalidate()
//        parent.invalidate()
        paint(graphics)
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

        graphics.fillRect(zoomXmin.toInt(), zoomYmin.toInt(), zoomWidth.toInt(), zoomHeight.toInt())
    }

    override fun processMouseEvent(e: MouseEvent?) {
//        super.processMouseEvent(e)

        when (e!!.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (isVisible) {
                    moveOffset = Point2D.Double((e.x - zoomXmin), (e.y - zoomYmin))

                    moveStarted = true
                }
            }

            MouseEvent.MOUSE_RELEASED -> {
                moveStarted = false
            }

            MouseEvent.MOUSE_DRAGGED -> {
                if (moveStarted) {
                    zoomXmin = (e.x - moveOffset.x)
                    zoomYmin = (e.y - moveOffset.y)
                    setBounds()

                }
            }
        }

        e.consume()
    }

    init {
        isVisible = false

        enableEvents(MouseEvent.MOUSE_EVENT_MASK)
        parent.addMouseListener(ParentMouseListener())
        parent.addMouseMotionListener(ParentMouseMoveListener())
        parent.addKeyListener(ParentKeyListener())
    }

    private fun fixEndpoints(it: Point2D) {
        if (it.x < zoomXmin) {
            zoomXmax = zoomXmin
            zoomXmin = it.x
        } else zoomXmax = it.x

        if (it.y < zoomYmin) {
            zoomYmax = zoomYmin
            zoomYmin = it.y
        } else zoomYmax = it.y

        zoomWidth = abs(zoomXmax - zoomXmin)

        zoomHeight = if (maintainAspect) zoomWidth * (parent.height / parent.width)
        else abs(zoomYmax - zoomYmin)

        setBounds()
    }
}
