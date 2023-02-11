package components

import EventBus
import action.ImageClickEvent
import action.ZoomBoxEvent
import java.awt.*
import java.awt.event.*
import java.awt.geom.Point2D
import javax.swing.BorderFactory
import javax.swing.JPanel
import kotlin.math.abs


class ZoomBox : JPanel() {
    private var minX = 0.0
    private var maxX = 0.0
    private var minY = 0.0
    private var maxY = 0.0
    private var zoomWidth = 0.0
    private var zoomHeight = 0.0

    private var dragStarted: Boolean = false
    private var moveStarted: Boolean = false
    private var moveOffset = Point2D.Double(0.0, 0.0)

    private var maintainAspect: Boolean = true

    fun adjustPoint(it: MouseEvent): Point2D {
        return Point2D.Double(it.x.toDouble(), it.y.toDouble())
    }

    inner class ParentKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent?) {
            e!!.consume()
        }

        override fun keyPressed(e: KeyEvent?) {
            e!!.consume()

            if (e.keyChar.code == KeyEvent.VK_ESCAPE) {
                isVisible = false
            } else if (e.keyChar.code == KeyEvent.VK_ENTER) {
                if (isVisible) {
                    isVisible = false
                    EventBus.publish(ZoomBoxEvent(this@ZoomBox))
                }
            }

            this@ZoomBox.setBounds()
        }

        override fun keyReleased(e: KeyEvent?) {
            e!!.consume()
        }
    }

    inner class ParentMouseListener : MouseListener {
        override fun mousePressed(e: MouseEvent?) {
            e!!.consume()

            if (e.button == MouseEvent.BUTTON1 && this@ZoomBox.isEnabled) {
                val adjustedPoint = adjustPoint(e)

                minX = adjustedPoint.x
                minY = adjustedPoint.y

                zoomWidth = 10.0
                zoomHeight = 10.0
                isVisible = true
                this@ZoomBox.setBounds()
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            e!!.consume()

            if (e.button == MouseEvent.BUTTON1 && this@ZoomBox.isEnabled) {
                if (dragStarted) {
                    dragStarted = false

                    this@ZoomBox.parent.requestFocus()
                    this@ZoomBox.setBounds()
                } else {
                    isVisible = false

                    val localPoint = adjustPoint(e)

                    EventBus.publish(ImageClickEvent(localPoint.x, localPoint.y, parent as JPanel))
                }
            }
        }

        override fun mouseClicked(e: MouseEvent?) {
            e!!.consume()
        }

        override fun mouseEntered(e: MouseEvent?) {
            e!!.consume()
        }

        override fun mouseExited(e: MouseEvent?) {
            e!!.consume()
        }
    }

    inner class ParentMouseMoveListener : MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            e!!.consume()

            if (this@ZoomBox.isEnabled) {
                if (dragStarted) {
                    fixEndpoints(adjustPoint(e))
                } else {
                    dragStarted = true
                    isVisible = true
                }
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
            e!!.consume()
        }
    }

    fun setBounds() {
        maximumSize = Dimension(zoomWidth.toInt(), zoomHeight.toInt())
        bounds = Rectangle(minX.toInt(), minY.toInt(), zoomWidth.toInt(), zoomHeight.toInt())
        repaint()
    }

    override fun addNotify() {
        super.addNotify()

        parent.addMouseListener(ParentMouseListener())
        parent.addMouseMotionListener(ParentMouseMoveListener())
        parent.addKeyListener(ParentKeyListener())
    }

    override fun paintComponent(g: Graphics?) {
        if (g == null) return

        val graphics = g as Graphics2D

        graphics.fillRect(minX.toInt(), minY.toInt(), zoomWidth.toInt(), zoomHeight.toInt())
    }

    override fun processMouseEvent(e: MouseEvent?) {
        super.processMouseEvent(e)

        when (e!!.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (isVisible) {
                    moveOffset = Point2D.Double(e.x.toDouble(), e.y.toDouble())
                    moveStarted = true
                }
            }

            MouseEvent.MOUSE_RELEASED -> {
                moveStarted = false
            }
        }

        e.consume()
    }

    init {
        val borderWidth = 3

        border = BorderFactory.createMatteBorder(borderWidth, borderWidth, borderWidth, borderWidth, Color.RED)

        isEnabled = false
        isVisible = false
        isOpaque = true
        isDoubleBuffered = true

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (moveStarted) {
                    e!!.consume()

                    if (abs(e.x.toDouble() - moveOffset.x) >= 5 || abs(e.y.toDouble() - moveOffset.y) >= 5) {
                        minX += (e.x - moveOffset.x)
                        minY += (e.y - moveOffset.y)
                    }

                    setBounds()
                }
            }
        })

        enableEvents(MouseEvent.MOUSE_EVENT_MASK)
    }

    private fun fixEndpoints(it: Point2D) {
        if (it.x < minX) {
            maxX = minX
            minX = it.x
        } else maxX = it.x

        if (it.y < minY) {
            maxY = minY
            minY = it.y
        } else maxY = it.y

        zoomWidth = abs(maxX - minX)

        zoomHeight = if (maintainAspect) zoomWidth * (parent.height.toDouble() / parent.width.toDouble())
        else abs(maxY - minY)

        setBounds()
    }
}
