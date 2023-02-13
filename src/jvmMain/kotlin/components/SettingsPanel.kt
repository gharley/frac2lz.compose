package components

import EventBus
import action.UIAction
import action.UIEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class UISettings(
    var colorFromFractal: Boolean = false,
    var useSecondarySmoothing: Boolean = false,
    var refineRange: Int = 0,
    var refreshRate: Int = 50
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel() {
    Surface(Modifier.padding(20.dp).width(400.dp)) {
        val settings: UISettings = remember { UISettings() }
        var trigger by remember { mutableStateOf(0) }

        fun broadcastSettings() {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            trigger++
        }

        Column(Modifier.fillMaxSize().padding(3.dp)) {
            val rowModifier = Modifier.fillMaxWidth().wrapContentHeight()
            val rowAlignment = Alignment.CenterVertically

            trigger  // Causes recompose

            Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                Column {
                    Text("Color from Fractal Data:", color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Checkbox(
                        settings.colorFromFractal,
                        { settings.colorFromFractal = it; broadcastSettings() }
                    )
                }
            }
            Row(modifier = rowModifier, verticalAlignment = rowAlignment) {

                Column {
                    Text("Apply additional smoothing:", color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Checkbox(
                        settings.useSecondarySmoothing,
                        { settings.useSecondarySmoothing = it; broadcastSettings() },
                        enabled = settings.colorFromFractal
                    )
                }
            }
            Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                Column {
                    Text("Refresh Rate:", color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Slider(
                        settings.refreshRate.toFloat(),
                        onValueChange = { settings.refreshRate = it.toInt(); trigger++ },
                        valueRange = (1f..100f),
                        steps = 100,
                        onValueChangeFinished = { broadcastSettings() },
                    )
                }
            }
            Row(rowModifier, verticalAlignment = rowAlignment) {
                Column {
                    Text("Width:", color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    var width by remember { mutableStateOf("") }

                    TextField(width, { width = it })
                }
            }
            Row(rowModifier, verticalAlignment = rowAlignment) {
                Column {
                    Text("Height:", color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    var height by remember { mutableStateOf("") }

                    TextField(height, { height = it })
                }
            }
        }
    }
}