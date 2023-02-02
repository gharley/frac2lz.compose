import action.ActionEvent
import action.GetProperties
import action.HaveProperties
import action.SetProperty
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.system.exitProcess

@Composable
@Preview
fun App() {
    val propFile = "./conf/frac2lz.properties"
    val properties = remember { Properties() }

    fun loadProperties() {
        val file = File(propFile)

        if (file.isFile) {
            val stream = FileInputStream(file)

            properties.load(stream)
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

    loadProperties()
    MainWindow(properties) { exitApplication() }
}

fun main() = application {
    App()
}
