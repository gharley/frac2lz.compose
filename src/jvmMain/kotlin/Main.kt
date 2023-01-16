import action.*

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.PaletteBar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import kotlin.system.exitProcess

@Composable
@Preview
fun App() {
    val appName = "Frac2lz"
    val appTitle = remember { mutableStateOf(appName) }

    val propFile = "./conf/frac2lz.properties"
    val properties = remember { Properties() }

    fun loadProperties() {
        val file = File(propFile)

        if (file.isFile) {
            val stream = FileInputStream(file)

            properties.load(stream)
        }
        var palette = Palette()
        palette.size = 128
    }

    EventBus.listen(AppTitle::class.java).subscribe {
        appTitle.value = appName
        if (it.title.isNotEmpty()) {
            appTitle.value += " - " + it.title
        }
    }

    EventBus.listen(GetProperties::class.java).subscribe {
        EventBus.publish(HaveProperties(properties))
    }

    EventBus.listen(SetProperty::class.java).subscribe {
        properties[it.key] = it.value
    }

    EventBus.listen(Any::class.java).subscribe {
        if (it is ActionEvent){
            EventBus.publish(SetProperty(it.action.toString(), "true"))
        }
    }

    fun exitApplication() {
        val file = File(propFile)

        if (!file.isFile) {
            file.createNewFile()
        }

        properties.store(FileOutputStream(propFile), "Frac2lz Properties")
        exitProcess(0)
    }

    Window(
        onCloseRequest = { exitApplication() },
        title = appTitle.value,
        icon = painterResource("frac2lz128.png")
    ) {
        loadProperties()
        var text by remember { mutableStateOf("Hello, World!") }

        MaterialTheme {
            MenuBar() {
                Menu("File") {
                    Item("Open Fractal", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_FRACTAL)) })
                    Item("Open JSON file", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_JSON)) })
                    Separator()
                    Item("Open Palette", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_PALETTE)) })
                    Separator()
                    Item("Save Fractal", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_FRACTAL)) })
                    Item("Save JSON file", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_JSON)) })
                    Separator()
                    Item("Save Palette", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_PALETTE)) })
                    Separator()
                    Item("Export to image file", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_IMAGE)) })
                    Separator()
                    Item("Exit", onClick = { exitProcess(0) })
                }
                Menu("Fractal") {
                    Item("Calculate Base Fractal", onClick = {EventBus.publish(CalculateEvent(CalculateAction.CALCULATE_BASE)) })
                    Separator()
                    Item("Recalculate", onClick = {EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE)) })
                    Item("Refine", onClick = {EventBus.publish(CalculateEvent(CalculateAction.REFINE)) })
                    Separator()
                    Item("Refresh Image", onClick = {EventBus.publish(CalculateEvent(CalculateAction.REFRESH)) })
                    Separator()
                    Item("Show Histogram", onClick = {EventBus.publish(CalculateEvent(CalculateAction.SHOW_HISTOGRAM)) })
                }
            }
            Column{
                Button(onClick = {
                    text = "Hello, Desktop!"
                    EventBus.listen(HaveProperties::class.java).subscribe {
                        it.props["does so"] = "work"
                    }
                    EventBus.publish(AppTitle("working"))
                    EventBus.publish(SetProperty("working", "true"))
                    EventBus.publish(GetProperties())
                }) {
                    Text(text)
                }
                Row {
                    IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.RANDOM)) },
                        content = { Image(painterResource("random32.png"), "", contentScale = ContentScale.FillBounds) }
                    )
                    IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.SMOOTH)) },
                        content = { Image(painterResource("smooth32.png"), "") }
                    )
                    IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.DEFAULT)) },
                        content = { Image(painterResource("default32.png"), "Grayscale Palette") }
                    )
                    IconButton(onClick = { EventBus.publish(PaletteEvent(PaletteAction.ANIMATE)) },
                        content = { Image(painterResource("animate32.png"), "") }
                    )
                }
                PaletteBar(3840.dp, Palette(), onPaletteTypeChange = {})
            }
        }
    }
}

fun main() = application {
    App()
}
