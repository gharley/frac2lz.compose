import action.FractalEvent
import action.FractalIterationEvent
import action.FractalPointData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import state.FractalBounds
import state.FractalParameters
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.json.Json
import javax.json.JsonBuilderFactory
import javax.json.JsonObject
import kotlin.math.max

const val zInit = -10.0

fun array2dOfDouble(sizeOuter: Int, sizeInner: Int): Array<DoubleArray> =
    Array(sizeOuter) { DoubleArray(sizeInner) { zInit } }

fun array2dOfLong(sizeOuter: Int, sizeInner: Int): Array<LongArray> = Array(sizeOuter) { LongArray(sizeInner) { -1L } }

abstract class Fractal : Serializable {
    private var cancelCalc = false

    abstract var params: FractalParameters

    var name: String = ""

    private var version: String = "1.2"
    private var aspectAdjustX: Double = 1.0
    private var aspectAdjustY: Double = 1.0
    private val boundsHeight: Double
        get() = params.bounds.bottom - params.bounds.top
    private val boundsWidth: Double
        get() = params.bounds.right - params.bounds.left
    private val imageHeight: Int
        get() = params.height.toInt()
    private val imageWidth: Int
        get() = params.width.toInt()
    private val incX: Double
        get() = (maxX - minX) / params.width
    private val incY: Double
        get() = ((maxY - minY)) / params.height
    private val magnify: Double
        get() = params.magnify
    var maxIterationsActual: Long = 0L
    private val minX: Double
        get() = params.centerX + params.bounds.left * magnify / aspectAdjustX
    private val maxX: Double
        get() = params.centerX + params.bounds.right * magnify / aspectAdjustX
    private val minY: Double
        get() = params.centerY + params.bounds.top * magnify / aspectAdjustY
    private val maxY: Double
        get() = params.centerY + params.bounds.bottom * magnify / aspectAdjustY

    private val startImaginary: (Int) -> Double =
        { maxY - it * incY }  // Screen y-axis is the opposite of Cartesian y-axis
    private val startReal: (Int) -> Double = { minX + it * incX }

    private var iterations: Array<LongArray> = array2dOfLong(100, 100)
    private var reals = array2dOfDouble(100, 100)
    private var imaginarys = array2dOfDouble(100, 100)

    open suspend fun calcAll() {
        setup()

        iterations = array2dOfLong(imageHeight, imageWidth)
        reals = array2dOfDouble(imageHeight, imageWidth)
        imaginarys = array2dOfDouble(imageHeight, imageWidth)

//            MainController.showWaitCursor()

        calcLoop@ for (row in 0 until imageHeight) {
            val startImaginary = startImaginary(row)

            for (column in 0 until imageWidth) {
                if (cancelCalc) break@calcLoop

                val startReal = startReal(column)

                with(calcOne(Complex(startReal, startImaginary), params.maxIterations)) {
                    this@Fractal.iterations[row][column] = iterations
                    reals[row][column] = z.real
                    imaginarys[row][column] = z.imaginary
                }

                fireCalcUpdate(row, column)
            }
        }

//            MainController.showDefaultCursor()

        fireIterationUpdate()
    }

    fun cancelCalculation() {
        cancelCalc = true
    }

    open fun fractalData(row: Int, column: Int): FractalPointData {
        return FractalPointData(
            iterations[row][column], params.maxIterations,
            Complex(reals[row][column], imaginarys[row][column]), Complex(startReal(column), startImaginary(row))
        )
    }

    open fun hasZValues(): Boolean {
        return reals.size == iterations.size && reals[0].size == iterations[0].size && reals[0][0] != zInit
    }

    private var refreshInProgress = false

    open suspend fun refresh() {
        if (refreshInProgress || !hasZValues()) return
        refreshInProgress = true

//            MainController.showWaitCursor()

        refreshLoop@ for (row in iterations.indices) {
            for (column in iterations[0].indices) {
                if (cancelCalc || reals[row][column] == zInit) break@refreshLoop

                fireCalcUpdate(row, column)
            }
        }

//            MainController.showDefaultCursor()

        refreshInProgress = false
    }

    open suspend fun refineSet() {
        params.maxIterations += 5000
        setup()

//            MainController.showWaitCursor()

        refineLoop@ for (row in iterations.indices) {
            val startImaginary = maxY - row * incY  // Screen y-axis is the opposite of Cartesian y-axis

            for (column in iterations[0].indices) {
                if (iterations[row][column] == -1L) {
                    if (cancelCalc) break@refineLoop

                    val startReal = minX + column * incX

                    with(calcOne(Complex(startReal, startImaginary), params.maxIterations)) {
                        this@Fractal.iterations[row][column] = iterations

                        // In case we load an image w/o Z Values
                        if (reals.size > row && reals[0].size > column) {
                            reals[row][column] = z.real
                            imaginarys[row][column] = z.imaginary
                        }
                    }
                }

                fireCalcUpdate(row, column)
            }
        }

//            MainController.showDefaultCursor()

        fireIterationUpdate()
    }

    private suspend fun fireCalcUpdate(row: Int, column: Int) = coroutineScope {
        val isEndOfRow = column == iterations[0].size - 1

            EventBus.publish(FractalEvent(row, column, fractalData(row, column), isEndOfRow))
//            if (isEndOfRow) fireIterationUpdate()
    }

    private suspend fun fireIterationUpdate() = coroutineScope{
        EventBus.publish(FractalIterationEvent(params.maxIterations, maxIterationsActual))
    }

    private fun fireParameterUpdate() {
        EventBus.publish(params)
    }

    open fun setDefaultParameters() {}

    open fun setup() {
        cancelCalc = false
        maxIterationsActual = 0L

        val imageAspect = params.height / params.width
        val fractalAspect = boundsHeight / boundsWidth

        if (fractalAspect > imageAspect) {
            aspectAdjustX = imageAspect / fractalAspect
            aspectAdjustY = 1.0
        } else {
            aspectAdjustX = 1.0
            aspectAdjustY = fractalAspect / imageAspect
        }

        runBlocking {
            fireIterationUpdate()
            launch { fireParameterUpdate() }
        }
    }

    abstract fun calcOne(start: Complex, maxIterations: Long): FractalPointData

    open fun fromJson(data: JsonObject) {
        name = data.getString("name")
        when (name) {
            "Mandelbrot" -> {
                version = data.getString("version")
                val fracParams: JsonObject = data.getJsonObject("params")
                val boundsObject: JsonObject = fracParams.getJsonObject("bounds")

                with(params) {
                    width = fracParams.getJsonNumber("width").doubleValue()
                    height = fracParams.getJsonNumber("height").doubleValue()
                    centerX = fracParams.getJsonNumber("centerX").doubleValue()
                    centerY = fracParams.getJsonNumber("centerY").doubleValue()
                    magnify = fracParams.getJsonNumber("magnify").doubleValue()
                    bounds.top = boundsObject.getJsonNumber("top").doubleValue()
                    bounds.right = boundsObject.getJsonNumber("right").doubleValue()
                    bounds.bottom = boundsObject.getJsonNumber("bottom").doubleValue()
                    bounds.left = boundsObject.getJsonNumber("left").doubleValue()
                    maxIterations = fracParams.getJsonNumber("maxIterations").longValue()
                }
            }

            else -> {}
        }

        runBlocking{ launch{ fireParameterUpdate() } }
    }

    open fun toJson(): JsonObject {
        val factory: JsonBuilderFactory = Json.createBuilderFactory(null)

        return factory.createObjectBuilder()
            .add("name", name)
            .add("version", "1.2")
            .add(
                "params", factory.createObjectBuilder()
                    .add("width", params.width)
                    .add("height", params.height)
                    .add("centerX", params.centerX)
                    .add("centerY", params.centerY)
                    .add("magnify", params.magnify)
                    .add(
                        "bounds", factory.createObjectBuilder()
                            .add("top", params.bounds.top)
                            .add("right", params.bounds.right)
                            .add("bottom", params.bounds.bottom)
                            .add("left", params.bounds.left)
                    )
                    .add("maxIterations", params.maxIterations)
            )
            .build()
    }

//    open fun zoomTo(zoomBox: Rectangle, image: Canvas) {
//        val zoomTopLeft = image.parentToLocal(zoomBox.x, zoomBox.y)
//        val zoomWidth = zoomBox.width / image.scaleX
//        val zoomHeight = zoomBox.height / image.scaleY
//
//        // The following values allow for asymmetrical bounds e.g. the Mandelbrot x-axis is usually -2.0 to 1.0
//        val fractalWidth = abs(params.bounds.right - params.bounds.left)
//        val fractalHeight = abs(params.bounds.bottom - params.bounds.top)
//        val centerX = minX + (zoomTopLeft.x + zoomWidth / fractalWidth * abs(params.bounds.left)) * incX
//        // Screen y-axis is the opposite of Cartesian y-axis
//        val centerY = maxY - (zoomTopLeft.y + zoomHeight / fractalHeight * abs(params.bounds.bottom)) * incY
//
//        val magnify = zoomWidth * incX / fractalWidth
//
//        params.centerX = centerX
//        params.centerY = centerY
//        params.magnify = magnify
//    }

    open fun readObject(stream: ObjectInputStream) {
        name = stream.readUTF()
        version = stream.readUTF()
        maxIterationsActual = stream.readLong()
        @Suppress("UNCHECKED_CAST")
        params = stream.readObject() as FractalParameters
        @Suppress("UNCHECKED_CAST")
        iterations = stream.readObject() as Array<LongArray>

        @Suppress("UNCHECKED_CAST")
        reals = if (version > "1.0") stream.readObject() as Array<DoubleArray> else array2dOfDouble(
            iterations.size,
            iterations[0].size
        )
        @Suppress("UNCHECKED_CAST")
        imaginarys = if (version > "1.0") stream.readObject() as Array<DoubleArray> else array2dOfDouble(
            iterations.size,
            iterations[0].size
        )

        runBlocking{ launch{ fireParameterUpdate() } }
    }

    open fun writeObject(stream: ObjectOutputStream) {
        stream.writeUTF(name)
        stream.writeUTF("1.1")
        stream.writeLong(maxIterationsActual)
        stream.writeObject(params)
        stream.writeObject(iterations)
        stream.writeObject(reals)
        stream.writeObject(imaginarys)
    }
}

private val defaultParams =
    FractalParameters(3840.0, 2160.0, 0.0, 0.0, 100L, FractalBounds(), 1.0)

open class Mandelbrot(override var params: FractalParameters = defaultParams.copy()) : Fractal(), Serializable {

    init {
        name = "Mandelbrot"
    }

    override fun calcOne(start: Complex, maxIterations: Long): FractalPointData {
        tailrec fun iterate(z: Complex, iterations: Long): FractalPointData {
            return when {
                iterations == maxIterations -> FractalPointData(-1L, params.maxIterations, z, start)
                z.sidesSquared() >= 4.0 -> {
                    maxIterationsActual = max(maxIterationsActual, iterations)
                    val result = FractalPointData(iterations, params.maxIterations, z, start)
                    EventBus.publish(result)
                    result
                }

                else -> iterate((z * z) + start, iterations + 1)
            }
        }

        return iterate(start, 0)
    }

    override fun setDefaultParameters() {
        super.setDefaultParameters()
        params = defaultParams.copy()
    }
}
