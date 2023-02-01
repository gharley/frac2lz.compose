package components

import EventBus
import java.awt.Canvas
import java.awt.Rectangle
import java.awt.geom.Point2D
import kotlin.math.abs

data class ZoomBoxEvent(val zoomBox: ZoomBox)
data class ImageClickEvent(val x: Double, val y: Double)

class ZoomBox(private val image: Canvas) : Rectangle() {
    private val zoomXmin = 0.0
    private val zoomXmax = 0.0
    private val zoomYmin = 0.0
    private val zoomYmax = 0.0
    private val zoomWidth = 0.0
    private val zoomHeight = 0.0

    private var dragStarted: Boolean = false
    private var moveStarted: Boolean = false
    private var moveOffset = Point2D.Double(0.0, 0.0)

    private var maintainAspect: Boolean = true

    init {
//        isManaged = false
//        isVisible = false
//        isMouseTransparent = true
//        strokeWidth = 2.0
//        stroke = Color.RED
//        fill = Color.TRANSPARENT
//
//        xProperty().bindBidirectional(zoomXmin)
//        yProperty().bindBidirectional(zoomYmin)
//        widthProperty().bindBidirectional(zoomWidth)
//        heightProperty().bindBidirectional(zoomHeight)
//
//        setOnMousePressed {
//            if (it.isPrimaryButtonDown && isVisible) {
//                moveOffset = Point2D(it.x - zoomXmin.value, it.y - zoomYmin.value)
//
//                moveStarted = true
//                it.consume()
//            }
//        }
//
//        setOnMouseReleased {
//            moveStarted = false
//        }
//
//        setOnMouseDragged {
//            if (moveStarted) {
//                zoomXmin.value = it.x - moveOffset.x
//                zoomYmin.value = it.y - moveOffset.y
//                it.consume()
//            }
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