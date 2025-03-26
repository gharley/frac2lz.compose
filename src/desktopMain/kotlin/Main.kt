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

        if (!properties.containsKey("autoRefresh")) properties["autoRefresh"] = "true"
    }

    EventBus.listen(PropertyEvent::class.java).subscribe {
        when (it.action) {
            PropertyAction.GET_PROPERTY -> {
                if (properties.containsKey(it.data))
                    EventBus.publish(PropertyEvent(PropertyAction.HAVE_PROPERTY, properties[it.data]))
            }
            PropertyAction.GET_PROPERTIES ->
                EventBus.publish(PropertyEvent(PropertyAction.HAVE_PROPERTY, properties))
            PropertyAction.SET_PROPERTY -> {
                val data = it.data as KeyValuePair
                properties[data.key] = data.value
            }
            else -> {}
        }
    }

    EventBus.listen(UIEvent::class.java).subscribe {
        if (it.action == UIAction.EXIT) {
            try {
                val file = Path(userHome).resolve(propFile).toFile()

                if (!file.isFile) {
                    file.createNewFile()
                }

                properties.store(file.outputStream(), "Frac2lz Properties")
            } catch (ex: Exception) {
//                val ex1 = ex
            } finally {
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
