import action.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import components.FractalImage
import components.MainMenu
import components.PaletteBar
import components.PaletteCanvas
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Properties
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun MainWindow(props: Properties, closeFunction: () -> Unit) {
    val properties = remember { props }
    val palette = rememberSaveable { mutableStateOf(Palette()) }
    val fractal = rememberSaveable { mutableStateOf(Mandelbrot()) }
    val appName = "Frac2lz"
    val appTitle = remember { mutableStateOf(appName) }

    EventBus.listen(AppTitle::class.java).subscribe {
        appTitle.value = appName
        if (it.title.isNotEmpty()) {
            appTitle.value += " - " + it.title
        }
    }

    EventBus.listen(NewPaletteEvent::class.java).subscribe{
        palette.value = Palette(it.palette)
    }

    fun getInitPath(key: String): String {
        return if (properties.containsKey(key)) properties[key] as String
        else "./"
    }

    fun refreshImage() {
//        fractalImage.prepareForCalc(fractal.params.width, fractal.params.height)
//        fractal.refresh()
    }

    fun getFile(initPath: String, extFilter: FileNameExtensionFilter, save: Boolean = false): File? {
        val dlg = JFileChooser()

        dlg.fileFilter = extFilter
        dlg.currentDirectory = File(initPath)

        var file: File? = null

        val result = if (save) dlg.showSaveDialog(null) else dlg.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            var filename = dlg.selectedFile.absolutePath
            val ext = extFilter.extensions[0]

            if (save) {
                if (!filename.endsWith(ext, true)){
                    filename += ".$ext"
                }
            }
            file = File(filename)
        }

        return file
    }

    fun onOpenPalette() {
        val initPath: String = getInitPath("palPath")
        val extFilter = FileNameExtensionFilter("Fra2lz Palette", "pal")

        val file = getFile(initPath, extFilter)

        if (file != null) {
            properties["palPath"] = file.parent ?: "./"

            val stream = ObjectInputStream(file.inputStream())

            palette.value.readObject(stream)
            stream.close()

            refreshImage()
        }
    }

    fun onSavePalette() {
        val initPath: String = getInitPath("palPath")
        val extFilter = FileNameExtensionFilter("Fra2lz Palette", "pal")

        val file = getFile(initPath, extFilter, true)

        if (file != null) {
            properties["palPath"] = file.parent ?: "./"

            val stream = ObjectOutputStream(file.outputStream())

            palette.value.writeObject(stream)
            stream.close()

            refreshImage()
        }
    }

    EventBus.listen(FileEvent::class.java).subscribe {
        when (it.action) {
            FileAction.OPEN_PALETTE -> onOpenPalette()
            FileAction.SAVE_PALETTE -> onSavePalette()
            else -> {}
        }
    }

    Window(
        onCloseRequest = { closeFunction() },
        title = appTitle.value,
        icon = painterResource("frac2lz128.png")
    ) {
        MaterialTheme {
            MainMenu()
            Column {
                FractalImage(fractal.value.params, palette.value)
                PaletteCanvas(palette.value)
                PaletteBar(3840.dp)
            }
        }
    }

}