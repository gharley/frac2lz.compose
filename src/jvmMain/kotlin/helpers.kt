import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.lang.Float.max
import java.lang.Float.min

data class HSL(var hue: Float = 0f, var saturation: Float = 0f, var luminance: Float = 0f)

// Thanks to camick.com for the conversion code
fun toHSL(color: Color): HSL {
    val red = color.red
    val green = color.green
    val blue = color.blue
    val colorMax = max((max(red, green)), blue)
    val colorMin = min((min(red, green)), blue)
    val delta = colorMax - colorMin
    val hue = when (colorMax) {
        colorMin -> 0f
        red -> (60 * (green - blue) / (delta) + 360) % 360
        green -> 60 * (blue - red) / (delta) + 120
        blue -> 60 * (red - green) / (delta) + 240
        else -> 0f
    }

    //  Calculate the Luminance
    val luminance = (colorMax + colorMin) / 2

    //  Calculate the Saturation
    val saturation = if (colorMax == colorMin) 0f
    else if (luminance <= .5f) (delta) / (colorMax + colorMin)
    else (delta) / (2 - delta)

    return HSL(hue, saturation * 100, luminance * 100)
}

fun hueToRGB(p: Float, q: Float, h: Float): Float {
    var hue = h

    if (hue < 0) hue += 1
    else if (hue > 1) hue -= 1

    if (6 * hue < 1) {
        return p + ((q - p) * 6 * h)
    }

    if (2 * hue < 1) {
        return q
    }

    if (3 * hue < 2) {
        return p + ((q - p) * 6 * ((2.0f / 3.0f) - h))
    }

    return p
}

fun toRGB(h: Float, s: Float, l: Float): Color {
    var hue = h
    var saturation = s
    var luminance = l

    if (saturation < 0.0f || saturation > 100.0f) {
        val message = "Color parameter outside of expected range - Saturation"
        throw IllegalArgumentException(message)
    }
    if (luminance < 0.0f || luminance > 100.0f) {
        val message = "Color parameter outside of expected range - Luminance"
        throw IllegalArgumentException(message)
    }

    //  Formula needs all values between 0 - 1.
    hue %= 360.0f
    hue /= 360f
    saturation /= 100f
    luminance /= 100f

    val q = if (luminance < 0.5) luminance * (1 + saturation) else luminance + saturation - saturation * luminance
    val p = 2 * luminance - q
    var red = max(0f, hueToRGB(p, q, hue + 1.0f / 3.0f))
    var green = max(0f, hueToRGB(p, q, hue))
    var blue = max(0f, hueToRGB(p, q, hue - 1.0f / 3.0f))

    red = min(red, 1.0f)
    green = min(green, 1.0f)
    blue = min(blue, 1.0f)

    return Color(red, green, blue)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolTip(text:String, content:@Composable () -> Unit){
    BoxWithTooltip(tooltip = {
        Surface(
            modifier = Modifier
                .shadow(5.dp)
                .background(MaterialTheme.colorScheme.onBackground)
                .border(2.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(10.dp).width(150.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }, delay = 500, content = content)
}