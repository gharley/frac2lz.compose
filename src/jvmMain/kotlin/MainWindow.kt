import action.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import components.MainMenu
import components.PaletteBar
import java.awt.FileDialog
import java.io.File
import java.io.ObjectInputStream
import java.util.Properties
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

@Composable
fun MainWindow(props: Properties, closeFunction: () -> Unit) {
    val properties = remember { props }
    val palette by remember { mutableStateOf(Palette()) }
    val appName = "Frac2lz"
    val appTitle = remember { mutableStateOf(appName) }

    EventBus.listen(AppTitle::class.java).subscribe {
        appTitle.value = appName
        if (it.title.isNotEmpty()) {
            appTitle.value += " - " + it.title
        }
    }

    fun getInitPath(key: String): String {
        return if (properties.containsKey(key)) properties[key] as String
        else "./"
    }

    fun refreshImage() {
//        fractalImage.prepareForCalc(fractal.params.width, fractal.params.height)
//        fractal.refresh()
    }

    fun onOpenPalette() {
        val initPath: String = getInitPath("palPath")
        val dlg = JFileChooser()
        val extFilter = FileNameExtensionFilter("Fra2lz Palette", "pal")

        dlg.fileFilter = extFilter
        dlg.currentDirectory = File(initPath)

        if (dlg.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = File(dlg.selectedFile.absolutePath)

            properties["palPath"] = file.parent ?: "./"

            val stream = ObjectInputStream(file.inputStream())

            palette.readObject(stream)
            stream.close()

            refreshImage()
        }
    }

    EventBus.listen(FileEvent::class.java).subscribe {
        when (it.action) {
            FileAction.OPEN_PALETTE -> onOpenPalette()
            else -> {}
        }
    }

    Window(
        onCloseRequest = { closeFunction() },
        title = appTitle.value,
        icon = painterResource("frac2lz128.png")
    ) {
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
                PaletteBar(3840.dp, palette)
            }
        }
    }

}