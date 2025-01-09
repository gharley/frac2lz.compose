package components

import EventBus
import FractalParameters
import Palette
import ToolTip
import action.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import setProperty

data class UISettings(
    var colorFromFractal: Boolean = false,
    var useSecondarySmoothing: Boolean = false,
    var refineRange: Int = 0,
    var refreshRate: Int = 50
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(params: FractalParameters, palette: Palette, autoRefresh: Boolean) {
    Surface(Modifier.padding(5.dp).width(400.dp), color = Color.Transparent, elevation = 5.dp) {
        val settings: UISettings = remember {
            UISettings(
                palette.getColorFromFractal,
                palette.useSecondarySmoothing,
                palette.refineRange
            )
        }
        var refresh by remember { mutableStateOf(autoRefresh) }
        var trigger by remember { mutableStateOf(0) }

        fun checkRefresh() {
            if (refresh) {
                EventBus.publish(CalculateEvent(CalculateAction.REFRESH))
            }
        }

        fun broadcastSettings(check: Boolean = true) {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            if (check) checkRefresh()
            trigger++
        }

        Column(Modifier.fillMaxHeight()) {
            val cardModifier = Modifier.background(MaterialTheme.colorScheme.background)
            val rowModifier = Modifier.fillMaxWidth().wrapContentHeight().padding(10.dp, 0.dp).weight(1f)
            val rowAlignment = Alignment.CenterVertically

            trigger  // Causes recompose

//            Card {
                ToolTip("Select color based on the Complex number representing each pixel.") {
                    Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                        Column(Modifier.weight(2f)) {
                            Text("Color from Fractal Data:", color = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f)) {
                            Checkbox(
                                settings.colorFromFractal,
                                {
                                    settings.colorFromFractal = it
                                    broadcastSettings()
                                }
                            )
                        }
                    }
                }
                ToolTip("Further refines the color from fractal process. Indicates how many additional calculations to perform on each pixel.") {
                    Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                        Column(Modifier.weight(1f)) {
                            Text("Refine Range:", color = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(2f)) {
                            Slider(
                                settings.refineRange.toFloat(),
                                onValueChange = { settings.refineRange = it.toInt(); trigger++ },
                                valueRange = (0f..5f),
                                steps = 5,
                                enabled = settings.colorFromFractal,
                                onValueChangeFinished = {
                                    broadcastSettings()
                                },
                                thumb = {
                                    SliderThumb(it)
                                }
                            )
                        }
                    }
                }
                ToolTip("Apply an algorithm to further smooth color transitions. Often results in monochrome colors.") {
                    Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                        Column(Modifier.weight(2f)) {
                            Text("Apply additional smoothing:", color = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f)) {
                            Checkbox(
                                settings.useSecondarySmoothing,
                                {
                                    settings.useSecondarySmoothing = it
                                    broadcastSettings()
                                },
                                enabled = settings.colorFromFractal
                            )
                        }
                    }
                }
                ToolTip("Controls the rate at which the image is drawn. Far left updates every row, far right delays update until image is completely calculated. May not have much effect if long calculations are involved.") {
                    Row(modifier = rowModifier, verticalAlignment = rowAlignment) {
                        Column(Modifier.weight(1f)) {
                            Text("Refresh Rate:", color = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(2f)) {
                            Slider(
                                settings.refreshRate.toFloat(),
                                onValueChange = { settings.refreshRate = it.toInt(); trigger++ },
                                valueRange = (1f..100f),
                                steps = 100,
                                onValueChangeFinished = { broadcastSettings(false) },
                                thumb = {
                                    SliderThumb(it)
                                }
                            )
                        }
                    }
                }
//            }

            ToolTip("Allows changing the actual dimensions and aspect ratio of the calculated image. After clicking the update button, either recalculate or calculate base fractal.") {
//                Card(cardModifier.padding(3.dp, 10.dp)) {
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

                        EventBus.listen(NewPaletteEvent::class.java).subscribe {
                            settings.colorFromFractal = it.palette.getColorFromFractal
                            settings.refineRange = it.palette.refineRange
                            settings.useSecondarySmoothing = it.palette.useSecondarySmoothing
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
                        Modifier.fillMaxWidth().weight(1f),
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
                        onClick = {
                            EventBus.publish(FractalSizeEvent(width.toDouble(), height.toDouble()))
                            checkRefresh()
                        },
                        Modifier.align(Alignment.CenterHorizontally).absolutePadding(top = 10.dp),
                        enabled = checkSize()
                    ) {
                        Text("Update image size")
                    }
                    Spacer(Modifier.padding(0.dp, 5.dp))
                }
//            }
            ToolTip("Automatically update the image when parameters are changed.") {
                Row(rowModifier, verticalAlignment = rowAlignment) {
                    Column(Modifier.weight(2f)) {
                        Text("Automatic refresh:", color = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f)) {
                        Checkbox(
                            refresh,
                            { refresh = it; setProperty("autoRefresh", it) },
                        )
                    }
                }
            }
        }
    }
}