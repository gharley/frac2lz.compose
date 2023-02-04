package components

import EventBus
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.Point2D
import javax.swing.JPanel
import kotlin.math.abs

data class ZoomBoxEvent(val zoomBox: ZoomBox)
data class ImageClickEvent(val x: Double, val y: Double)

class ZoomBox(private val image: JPanel) : Canvas() {
    private var zoomXmin = 0
    private var zoomXmax = 0
    private var zoomYmin = 0
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
        image.addMouseListener()
    }

//        fun adjustPoint(it: MouseEvent): Point2D {
//            return parent.screenToLocal(it.screenX, it.screenY)
//        }

//        image.setOnMousePressed {
//            if (it.isPrimaryButtonDown) {
//                this@ZoomBox.isMouseTransparent = true
//
//                val adjustedPoint = adjustPoint(it)
//
//                zoomXmin.value = adjustedPoint.x
//                zoomYmin.value = adjustedPoint.y
//
//                zoomWidth.value = 10.0
//                zoomHeight.value = 10.0
//
//                it.consume()
//            }
//        }
//
//        image.setOnMouseDragged {
//            if (dragStarted) {
//                fixEndpoints(adjustPoint(it))
//            } else {
//                dragStarted = true
//                this@ZoomBox.isVisible = true
//            }
//
//            it.consume()
//        }

//        image.setOnMouseReleased {
//            if (dragStarted) {
//                dragStarted = false
//                this@ZoomBox.isMouseTransparent = false
//            } else {
//                this@ZoomBox.isVisible = false
//
//                val localPoint = adjustPoint(it)
//                EventBus.publish(ImageClickEvent(localPoint.x, localPoint.y))
//            }
//
//            it.consume()
//        }
//
//        Platform.runLater {
//            stage.scene.addEventFilter(KeyEvent.KEY_PRESSED) {
//                if (it.code == KeyCode.ESCAPE) {
//                    this@ZoomBox.isVisible = false
//                } else if (it.code == KeyCode.ENTER) {
//                    if (this@ZoomBox.isVisible) {
//                        this@ZoomBox.isVisible = false
//                        EventBus.publish(ZoomBoxEvent(this))
//                    }
//                }
//
//                it.consume()
//            }
//        }
}

private fun fixEndpoints(it: Point2D) {
//        if (it.x < zoomXmin.value) {
//            zoomXmax.value = zoomXmin.value
//            zoomXmin.value = it.x
//        } else zoomXmax.value = it.x
//
//        if (it.y < zoomYmin.value) {
//            zoomYmax.value = zoomYmin.value
//            zoomYmin.value = it.y
//        } else zoomYmax.value = it.y
//
//        zoomWidth.value = abs(zoomXmax.value - zoomXmin.value)
//
//        if (maintainAspect) zoomHeight.value = zoomWidth.value * (image.height / image.width)
//        else zoomHeight.value = abs(zoomYmax.value - zoomYmin.value)
//    }
}