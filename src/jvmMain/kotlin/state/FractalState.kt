package state

import action.FractalEvent
import action.FractalPointData
import androidx.compose.runtime.*
import java.io.Serializable

data class FractalBounds(var top: Double = -2.1, var right: Double = 1.0, var bottom: Double = 2.1, var left: Double = -1.0)
data class FractalParameters(
    var width: Double, var height: Double, var centerX: Double, var centerY: Double,
    var maxIterations: Long, val bounds: FractalBounds = FractalBounds(),
    var magnify: Double = 1.0
) : Serializable
class FractalState{
    var bounds by mutableStateOf(FractalBounds())
    var pointData by mutableStateOf(FractalPointData())
    var parameters by mutableStateOf(FractalParameters(1920.0, 1080.0, 0.0, 0.0, 0L))
    var event by mutableStateOf(FractalEvent())
    var usedIterations by mutableStateOf(0L)

    fun updateCalculation(newEvent: FractalEvent){
        if (newEvent.data.iterations > usedIterations) usedIterations = newEvent.data.iterations
        event = newEvent.copy()
    }

    fun updateMaxIterations(iterations: Long){
        val newParameters = parameters.copy()
        newParameters.maxIterations = iterations
        parameters = newParameters
    }

    fun updateUsedIterations(used: Long){
        usedIterations = used
    }

    fun updateParameters(newParameters: FractalParameters){
        parameters = newParameters.copy()
        updateUsedIterations(0)
    }
}

@Composable
fun rememberFractalState() = remember {
    FractalState()
}
