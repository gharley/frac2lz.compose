package components

import EventBus
import action.UIAction
import action.UIEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Properties

data class UISettings(
    var colorFromFractal: Boolean = false,
    var useSecondarySmoothing: Boolean = false,
    var refineRange: Int = 0,
    var refreshRate: Int = 50
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel() {
    Surface(Modifier.background(Color.LightGray).padding(20.dp).width(400.dp)) {
        val settings: UISettings = remember { UISettings() }
        var trigger by remember { mutableStateOf(0) }

        fun broadcastSettings() {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            trigger++
        }

        Column(Modifier.fillMaxSize().padding(3.dp)) {
            trigger  // Causes recompose

            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {

                Column {
                    Text("Color from Fractal:")
                }
                Column {
                    Checkbox(
                        settings.colorFromFractal,
                        { settings.colorFromFractal = it; broadcastSettings() }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {

                Column {
                    Text("Limit Color Repetition:")
                }
                Column {
                    Checkbox(
                        settings.useSecondarySmoothing,
                        { settings.useSecondarySmoothing = it; broadcastSettings() },
                        enabled = settings.colorFromFractal
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Refresh Rate:")
                }
                Column {
                    Slider(
                        settings.refreshRate.toFloat(),
                        onValueChange = { settings.refreshRate = it.toInt(); trigger++ },
                        valueRange = (1f..100f),
                        steps = 100,
                        onValueChangeFinished = { broadcastSettings() },
//                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }
    }
}