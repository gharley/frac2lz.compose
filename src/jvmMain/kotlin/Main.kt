import action.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.PaletteBar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
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
        if (it is ActionEvent) {
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
            MainMenu()
            Column {
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
                PaletteBar(3840.dp, Palette())
            }
        }
    }
}

fun main() = application {
    App()
}
