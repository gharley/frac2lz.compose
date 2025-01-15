import action.*
import components.SwingImage
import kotlinx.coroutines.*
import java.awt.geom.Point2D
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.json.*
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.max

const val zInit = -10.0

data class FractalBounds(
    var top: Double = -2.1,
    var right: Double = 1.0,
    var bottom: Double = 2.1,
    var left: Double = -1.0
)

data class FractalParameters(
    var width: Double, var height: Double, var centerX: Double, var centerY: Double,
    var maxIterations: Long, val bounds: FractalBounds = FractalBounds(),
    var magnify: Double = 1.0, var juliaReal: Double = 0.0, var juliaImaginary: Double = 0.0
)

fun array2dOfDouble(sizeOuter: Int, sizeInner: Int): Array<DoubleArray> =
    Array(sizeOuter) { DoubleArray(sizeInner) { zInit } }

fun array2dOfLong(sizeOuter: Int, sizeInner: Int): Array<LongArray> = Array(sizeOuter) { LongArray(sizeInner) { -1L } }

abstract class Fractal {
    private var cancelCalc = false

    private val defaultParams =
        FractalParameters(3840.0, 2160.0, 0.0, 0.0, 100L, FractalBounds(), 1.0)

    open var params = defaultParams.copy()

    var name: String = ""

    private val currentVersion = "1.3"
    private var version = currentVersion
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
    internal var juliaSeed: Complex = Complex(0.0, 0.0)
        get() = Complex(params.juliaReal, params.juliaImaginary)

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

    var iterations: Array<LongArray> = array2dOfLong(100, 100)
    private var reals = array2dOfDouble(100, 100)
    private var imaginarys = array2dOfDouble(100, 100)

    private suspend fun fireComplete() = coroutineScope {
        EventBus.publish(CalculateEvent(CalculateAction.COMPLETE))
    }

    private suspend fun fireCalcUpdate(row: Int, column: Int) = coroutineScope {
        val isEndOfRow = column == iterations[0].size - 1

        EventBus.publish(FractalEvent(row, column, fractalData(row, column), isEndOfRow))
        if (isEndOfRow) fireIterationUpdate()
    }

    private suspend fun fireIterationUpdate() = coroutineScope {
        EventBus.publish(FractalIterationEvent(params.maxIterations, maxIterationsActual))
    }

    private fun fireParameterUpdate() {
        EventBus.publish(params)
    }

    private fun startMandelbrot(row: Int, column: Int): Complex {
        return Complex(startReal(column), startImaginary(row))
    }

    private fun startJulia(row: Int, column: Int): Complex {
        return juliaSeed + startMandelbrot(row, column)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startCalc(isJulia: Boolean = false) {
        GlobalScope.launch {
            juliaSeed = Complex(0.0, 0.0)
            if (isJulia) calcAll(getStart = ::startJulia)
            else {
                calcAll()
            }

            fireComplete()
        }
    }

    private fun baseCalc() = runBlocking {
        setDefaultParameters()
        startCalc()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun refineImage() {
        GlobalScope.launch {
            refineSet()
            fireComplete()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun refreshImage() {
        GlobalScope.launch {
            refresh()
            fireComplete()
        }
    }

    init {
        EventBus.listen(CalculateEvent::class.java).subscribe {
            when (it.action) {
                CalculateAction.CALCULATE_BASE -> baseCalc()
                CalculateAction.CALCULATE_JULIA -> startCalc(true)
                CalculateAction.RECALCULATE -> startCalc(juliaSeed != Complex(0.0, 0.0))
                CalculateAction.REFINE -> refineImage()
                CalculateAction.REFRESH -> refreshImage()
                else -> {}
            }
        }

        EventBus.listen(FractalSizeEvent::class.java).subscribe {
            setSize(it.width, it.height)
        }

        EventBus.listen(ZoomBoxEvent::class.java).subscribe {
            zoomTo(it.zoomBox)
            EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE))
        }

        EventBus.listen(ImageClickEvent::class.java).subscribe {
            if (!it.shift) return@subscribe

            val scale = (it.image as SwingImage).scale
            val column = (it.x / scale).toInt()
            val row = (it.y / scale).toInt()

            try {
                val centerXAdjust = ((it.image.width / 2).toInt() - it.x)
                val centerYAdjust = ((it.image.height / 2).toInt() - it.y)

                params.juliaReal = startReal(column)
                params.juliaImaginary = startImaginary(row)
                params.centerX = -params.juliaReal - centerXAdjust * incX
                params.centerY = -params.juliaImaginary - centerYAdjust * incY

                startCalc(true)
            } catch (_: Exception) {
            }
        }

    }

    open suspend fun calcAll(getStart: (row: Int, column: Int) -> Complex = this::startMandelbrot) {
        setup()

        iterations = array2dOfLong(imageHeight, imageWidth)
        reals = array2dOfDouble(imageHeight, imageWidth)
        imaginarys = array2dOfDouble(imageHeight, imageWidth)

//            MainController.showWaitCursor()

        calcLoop@ for (row in 0 until imageHeight) {
            for (column in 0 until imageWidth) {
                if (cancelCalc) break@calcLoop

                with(calcOne(getStart(row, column), params.maxIterations)) {
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
//
//    fun cancelCalculation() {
//        cancelCalc = true
//    }

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
        params.maxIterations += 500
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

    open fun setDefaultParameters() {
        params = defaultParams.copy()
    }

    open fun setSize(width: Double, height: Double) {
        params.apply {
            this.width = width
            this.height = height
        }

        defaultParams.apply {
            this.width = width
            this.height = height
        }

        setup()
    }

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
            "Mandelbrot", "Julia" -> {
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

                    if (version >= "1.3") {
                        juliaReal = fracParams.getJsonNumber("juliaReal").doubleValue()
                        juliaImaginary = fracParams.getJsonNumber("juliaImaginary").doubleValue()
                    }
                }
            }

            else -> {}
        }

        runBlocking { launch { fireParameterUpdate() } }
    }

    open fun toJson(): JsonObject {
        val factory: JsonBuilderFactory = Json.createBuilderFactory(null)

        return factory.createObjectBuilder()
            .add("name", name)
            .add("version", currentVersion)
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
                    .add("juliaReal", juliaSeed.real)
                    .add("juliaImaginary", juliaSeed.imaginary)
            )

            .build()
    }

    open fun zoomTo(zoomBox: JPanel) {
        val image = zoomBox.parent as SwingImage
        val scale = image.scale
        val zoomTopLeft = Point2D.Double(zoomBox.bounds.x.toDouble() / scale, zoomBox.bounds.y.toDouble() / scale)
        val zoomWidth = zoomBox.width / scale
        val zoomHeight = zoomBox.height / scale

        // The following values allow for asymmetrical bounds e.g. the Mandelbrot x-axis is usually -2.0 to 1.0
        val fractalWidth = abs(params.bounds.right - params.bounds.left)
        val fractalHeight = abs(params.bounds.bottom - params.bounds.top)
        val centerX = minX + (zoomTopLeft.x + zoomWidth / fractalWidth * abs(params.bounds.left)) * incX
        // Screen y-axis is the opposite of Cartesian y-axis
        val centerY = maxY - (zoomTopLeft.y + zoomHeight / fractalHeight * abs(params.bounds.bottom)) * incY

        val magnify = zoomWidth * incX / fractalWidth * scale

        params.centerX = centerX
        params.centerY = centerY
        params.magnify = magnify
    }

    open fun readObject(stream: ObjectInputStream) {
        val reader: JsonReader = Json.createReader(stream)

        fromJson(reader.readObject())

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

        runBlocking { launch { fireParameterUpdate() } }
    }

    open fun writeObject(stream: ObjectOutputStream) {
        val writer: JsonWriter = Json.createWriter(stream)

        writer.writeObject(toJson())

        stream.writeObject(iterations)
        stream.writeObject(reals)
        stream.writeObject(imaginarys)
    }
}

open class Mandelbrot : Fractal() {

    init {
        name = "Mandelbrot"
    }

    override fun calcOne(start: Complex, maxIterations: Long): FractalPointData {
        val seed = juliaSeed
        val isJulia = !(seed.real == 0.0 && seed.imaginary == 0.0)

        tailrec fun iterate(z: Complex, iterations: Long): FractalPointData {
            return when {
                iterations == maxIterations -> FractalPointData(-1L, maxIterations, z, start)
                z.sidesSquared() >= 4.0 -> {
                    maxIterationsActual = max(maxIterationsActual, iterations)
                    val result = FractalPointData(iterations, params.maxIterations, z, start)
                    EventBus.publish(result)
                    result
                }
                isJulia -> {
                    iterate((z * z) + seed, iterations + 1)
                }
                else ->  iterate((z * z) + start, iterations + 1)
            }
        }

        return iterate(start, 0)
    }
}
