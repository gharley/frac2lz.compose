import action.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import components.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonReader
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun MainWindow(props: Properties, closeFunction: () -> Unit) {
    val properties = remember { props }
    val fractal = rememberSaveable { mutableStateOf(Mandelbrot()) }
    val appName = "Frac2lz"
    val appTitle = remember { mutableStateOf(appName) }

    EventBus.listen(AppTitle::class.java).subscribe {
        appTitle.value = appName
        if (it.title.isNotEmpty()) {
            appTitle.value += " - " + it.title
        }
    }

    fun startCalc() {
        GlobalScope.launch { fractal.value.calcAll() }
    }

    fun baseCalc() = runBlocking {
        fractal.value.setDefaultParameters()
        startCalc()
    }

    fun refineImage() {
        GlobalScope.launch { fractal.value.refineSet() }
    }

    fun refreshImage() {
        GlobalScope.launch { fractal.value.refresh() }
    }

    EventBus.listen(CalculateEvent::class.java).subscribe {
        when (it.action) {
            CalculateAction.CALCULATE_BASE -> baseCalc()
            CalculateAction.RECALCULATE -> startCalc()
            CalculateAction.REFINE -> refineImage()
            CalculateAction.REFRESH -> refreshImage()
            else -> {}
        }
    }

    fun getInitPath(key: String): String {
        return if (properties.containsKey(key)) properties[key] as String
        else "./"
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
                if (!filename.endsWith(ext, true)) {
                    filename += ".$ext"
                }
            }
            file = File(filename)
        }

        return file
    }

    fun onOpen() {
        val initPath: String = getInitPath("2lzPath")
        val extFilter = FileNameExtensionFilter("Frac2lz image", "2lz")

        val file = getFile(initPath, extFilter)

        if (file != null) {
            properties["2lzPath"] = file.parent ?: "./"

            val stream = ObjectInputStream(file.inputStream())

            fractal.value.readObject(stream)
            stream.close()

            EventBus.publish(AppTitle(file.name))

            refreshImage()
        }
    }

    fun onOpenJson() {
        val initPath: String = getInitPath("jsonPath")
        val extFilter = FileNameExtensionFilter("JSON Fractal Spec", "json")

        val file = getFile(initPath, extFilter)

        if (file != null) {
            properties["jsonPath"] = file.parent ?: "./"

            val stream: InputStream = file.inputStream()
            val reader: JsonReader = Json.createReader(stream)
            val data: JsonObject = reader.readObject()

            EventBus.publish(AppTitle(file.name))

            fractal.value.fromJson(data)
            EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE))
//            val calcAlert = Alert(Alert.AlertType.CONFIRMATION, "Calculate Now?")
//
//            calcAlert.showAndWait()
//                .filter { response -> response === ButtonType.OK }
//                .ifPresent { EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE)) }
//        }
        }
    }
//
//    fun onOpenPalette() {
//        val initPath: String = getInitPath("palPath")
//        val extFilter = FileNameExtensionFilter("Fra2lz Palette", "pal")
//
//        val file = getFile(initPath, extFilter)
//
//        if (file != null) {
//            properties["palPath"] = file.parent ?: "./"
//
//            val stream = ObjectInputStream(file.inputStream())
//
//            palette.readObject(stream)
//            stream.close()
//
//            refreshImage()
//        }
//    }

    fun onSaveImage() {
        val initPath: String = getInitPath("imgPath")
        val extFilter = FileNameExtensionFilter("Save to PNG", "png")

        val file = getFile(initPath, extFilter, true)

        if (file != null) {
            properties["imgPath"] = file.parent ?: "./"
            val filename = file.absolutePath

            EventBus.publish(FileEvent(FileAction.WRITE_IMAGE, filename))
        }
    }

//    fun onSavePalette() {
//        val initPath: String = getInitPath("palPath")
//        val extFilter = FileNameExtensionFilter("Fra2lz Palette", "pal")
//
//        val file = getFile(initPath, extFilter, true)
//
//        if (file != null) {
//            properties["palPath"] = file.parent ?: "./"
//
//            val stream = ObjectOutputStream(file.outputStream())
//
//            palette.writeObject(stream)
//            stream.close()
//        }
//    }

    EventBus.listen(FileEvent::class.java).subscribe {
        when (it.action) {
            FileAction.OPEN_FRACTAL -> onOpen()
            FileAction.OPEN_JSON -> onOpenJson()
//            FileAction.OPEN_PALETTE -> onOpenPalette()
//            FileAction.SAVE_PALETTE -> onSavePalette()
            FileAction.SAVE_IMAGE -> onSaveImage()
            else -> {}
        }
    }

    Window(
        onCloseRequest = { closeFunction() },
        title = appTitle.value,
        icon = painterResource("frac2lz128.png"),
        state = WindowState(size = DpSize(1200.dp, 800.dp))
    ) {
        var palette by remember { mutableStateOf(Palette()) }

        EventBus.listen(NewPaletteEvent::class.java).subscribe {
            palette = Palette(it.palette)
        }

        MaterialTheme {
            MainMenu()
            Column {
                Row(Modifier.weight(1f)) { FractalImage(fractal.value.params, palette) }
                PaletteCanvas(palette)
                Row(Modifier.fillMaxWidth()) {
                    Column { Text("Color Range:") }
                    Column(Modifier.weight(1f)) {
                        PaletteSlider(1f, 100f, PaletteSliderType.COLOR_RANGE, palette){
                            palette.colorRange = it
                        }
                    }
                }
                PaletteBar()
                StatusBar(palette)
            }
        }
    }
}
