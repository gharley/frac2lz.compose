import action.GetProperties
import action.HaveProperties
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs

data class HSV(var hue: Float = 0f, var saturation: Float = 0f, var value: Float = 0f)

fun rgbToHsv(color: Color): HSV {
    val red = color.red // 255
    val green = color.green // 255
    val blue = color.blue // 255
    val colorMax = max((max(red, green)), blue)
    val colorMin = min((max(red, green)), blue)
    val delta = colorMax - colorMin
    val result = HSV()

    result.value = colorMax
    result.saturation = if (delta == 0f) 0f else delta / colorMax
    result.hue = if (delta == 0f) 0f else when (colorMax) {
        red -> (green - blue) / delta % 6f
        green -> (blue - red) / delta + 2f
        blue -> (red - green) / delta + 4f
        else -> 0f
    } * 60f
    result.hue = abs(result.hue) % 360

    return result
}

@Composable
fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
