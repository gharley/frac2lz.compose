import action.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import org.apache.commons.io.FileUtils
import java.io.FileInputStream
import java.util.*
import kotlin.io.path.Path
import kotlin.system.exitProcess

@Composable
@Preview
fun App() {
    val userHome = FileUtils.getUserDirectoryPath()
    val propFile = "frac2lz.properties"
    val properties = remember { Properties() }

    fun loadProperties() {
        val file = Path(userHome).resolve(propFile).toFile()

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

    EventBus.listen(UIEvent::class.java).subscribe {
        if (it.action == UIAction.EXIT) {
            try{
                val file = Path(userHome).resolve(propFile).toFile()

                if (!file.isFile) {
                    file.createNewFile()
                }

                properties.store(file.outputStream(), "Frac2lz Properties")
            }catch (_: Exception){}finally {
                exitProcess(0)
            }
        }
    }

    loadProperties()
    MainWindow(properties) { EventBus.publish(UIEvent(UIAction.EXIT)) }
}

fun main() = application {
    App()
}
