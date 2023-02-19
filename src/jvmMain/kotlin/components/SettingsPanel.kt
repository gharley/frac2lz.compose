package components

import EventBus
import FractalParameters
import action.*
import androidx.compose.foundation.background
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
fun SettingsPanel(params: FractalParameters) {
    Surface(Modifier.padding(20.dp).width(400.dp)) {
        val settings: UISettings = remember { UISettings() }
        var trigger by remember { mutableStateOf(0) }

        fun broadcastSettings() {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            trigger++
        }

        Column(Modifier.fillMaxSize()) {
            val rowModifier = Modifier.fillMaxWidth().wrapContentHeight().padding(10.dp, 0.dp)
            val rowAlignment = Alignment.CenterVertically

            trigger  // Causes recompose

            Card(Modifier.padding(3.dp, 3.dp).background(MaterialTheme.colorScheme.background)) {
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
                            thumb = {
                                SliderThumb(
                                    positions = SliderPositions(
                                        settings.refreshRate.toFloat(),
                                        floatArrayOf(0f)
                                    )
                                )
                            }
                        )
                    }
                }
            }

            Card(Modifier.padding(3.dp, 10.dp).background(MaterialTheme.colorScheme.background)) {
                Spacer(Modifier.padding(0.dp, 5.dp))
                var width by remember { mutableStateOf(params.width.toInt().toString()) }
                var height by remember { mutableStateOf(params.height.toInt().toString()) }

                EventBus.listen(FractalParameters::class.java).subscribe {
                    width = it.width.toInt().toString()
                    height = it.height.toInt().toString()
                }

                fun checkSize(): Boolean {
                    var result = false

                    if (width.isNotEmpty() && height.isNotEmpty()) {
                        if (width.toInt() > 0 && height.toInt() > 0) result = true
                    }

                    return result
                }

                TextField(
                    width,
                    { width = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Image Width:", color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    maxLines = 1
                )

                TextField(
                    height,
                    { height = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Image Height:", color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    maxLines = 1
                )

                Button(
                    onClick = { EventBus.publish(FractalSizeEvent(width.toDouble(), height.toDouble())) },
                    Modifier.align(Alignment.CenterHorizontally).absolutePadding(top = 10.dp),
                    enabled = checkSize()
                ) {
                    Text("Update image size")
                }
                Spacer(Modifier.padding(0.dp, 5.dp))
            }
            Row(rowModifier.weight(1f), verticalAlignment = rowAlignment) {
            }
        }
    }
}