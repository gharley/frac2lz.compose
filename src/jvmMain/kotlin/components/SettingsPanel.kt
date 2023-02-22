package components

import EventBus
import FractalParameters
import Palette
import ToolTip
import action.FractalSizeEvent
import action.UIAction
import action.UIEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class UISettings(
    var colorFromFractal: Boolean = false,
    var useSecondarySmoothing: Boolean = false,
    var refineRange: Int = 0,
    var refreshRate: Int = 50
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsPanel(params: FractalParameters, palette: Palette) {
    Surface(Modifier.padding(5.dp).width(400.dp), color = Color.Transparent, shadowElevation = 5.dp) {
        val settings: UISettings = remember {
                UISettings(
                    palette.getColorFromFractal,
                    palette.useSecondarySmoothing,
                    palette.refineRange
                )
        }
        var trigger by remember { mutableStateOf(0) }

        fun broadcastSettings() {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            trigger++
        }

        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
            val cardModifier = Modifier.background(MaterialTheme.colorScheme.background)
            val rowModifier = Modifier.fillMaxWidth().wrapContentHeight().padding(10.dp, 0.dp)
            val rowAlignment = Alignment.CenterVertically

            trigger  // Causes recompose

            Card(cardModifier.padding(3.dp, 3.dp), colors = CardDefaults.cardColors()) {
                ToolTip("Select color based on the Complex number representing each pixel.") {
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
                }
                ToolTip("Further refines the color from fractal process. Indicates how many additional calculations to perform on each pixel.") {
                    Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                        Column {
                            Text("Refine Range:", color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Slider(
                                settings.refineRange.toFloat(),
                                onValueChange = { settings.refineRange = it.toInt(); trigger++ },
                                valueRange = (0f..5f),
                                steps = 5,
                                enabled = settings.colorFromFractal,
                                onValueChangeFinished = { broadcastSettings() },
                                thumb = {
                                    SliderThumb(
                                        positions = SliderPositions(
                                            settings.refineRange.toFloat(),
                                            floatArrayOf(0f)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                ToolTip("Apply an algorithm to further smooth color transitions. Often results in monochrome colors.") {
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
                }
                ToolTip("Controls the rate at which the image is drawn. Far left updates every row, far right delays update until image is completely calculated. May not have much effect if long calculations are involved.") {
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
            }

            ToolTip("Allows changing the actual dimensions and aspect ratio of the calculated image. After clicking the update button, either recalculate or calculate base fractal.") {
                Card(cardModifier.padding(3.dp, 10.dp)) {
                    Spacer(Modifier.padding(0.dp, 5.dp))
                    var width by remember { mutableStateOf(params.width.toInt().toString()) }
                    var height by remember { mutableStateOf(params.height.toInt().toString()) }
                    var subscribed by remember { mutableStateOf(false) }

                    if (!subscribed) {
                        subscribed = true

                        EventBus.listen(FractalParameters::class.java).subscribe {
                            width = it.width.toInt().toString()
                            height = it.height.toInt().toString()
                        }
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
            }
            Row(rowModifier.weight(1f), verticalAlignment = rowAlignment) {
            }
        }
    }
}