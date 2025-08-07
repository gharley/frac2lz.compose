import action.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import components.*
import frac2lz_compose.generated.resources.Res
import frac2lz_compose.generated.resources.frac2lz128
import org.jetbrains.compose.resources.painterResource
import java.awt.FileDialog
import java.io.*
import java.util.*
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonReader

@Composable
fun MainWindow(props: Properties, closeFunction: () -> Unit) {
    val appName = "Frac2lz"
    val properties = remember { props }
    val useDark = props["useDark"].toString().toBoolean()
    val refresh = properties["autoRefresh"] == "true"

    fun checkRefresh() {
        if (refresh) {
            EventBus.publish(CalculateEvent(CalculateAction.REFRESH))
        }
    }

    MaterialTheme(if (useDark) darkColorScheme() else lightColorScheme()) {
        Window(
            onCloseRequest = { closeFunction() },
            title = appName,
            icon = painterResource(Res.drawable.frac2lz128),
            state = WindowState(placement = WindowPlacement.Maximized),
        ) {
            MainMenu()
            val fractal: Fractal = remember { Mandelbrot() }
            var palette by remember { mutableStateOf(Palette()) }

            var jsonLoaded by remember { mutableStateOf(false) }
            var subscribed by remember { mutableStateOf(false) }

            fun getInitPath(key: String): String {
                return if (properties.containsKey(key)) properties[key] as String
                else "./"
            }

            fun getFile(initPath: String, extFilter: String, save: Boolean = false, title: String = ""): File? {
                val dlg = FileDialog(this.window, title, if (save) FileDialog.SAVE else FileDialog.LOAD)

                dlg.directory = initPath
                dlg.file = "*$extFilter"
                dlg.filenameFilter = FilenameFilter { _, name -> name.endsWith(extFilter) }
                dlg.isVisible = true

                var filename = dlg.file
                val directory = dlg.directory

                if (!filename.isNullOrEmpty() && !filename.endsWith(extFilter)) filename += extFilter

                return if (directory.isNullOrEmpty() || filename.isNullOrEmpty()) null
                else File(directory, filename)
            }

            fun onOpen() {
                val file = getFile(getInitPath("2lzPath"), ".2lz", title = "Load Fractal Image")

                if (file != null) {
                    properties["2lzPath"] = file.parent ?: "./"

                    val stream = ObjectInputStream(file.inputStream())

                    fractal.readObject(stream)
                    palette.readObject(stream)

                    stream.close()

                    EventBus.publish(AppTitle(file.name))
                    EventBus.publish(FractalSizeEvent(fractal.params.width, fractal.params.height))

                    fractal.refreshImage()
                }
            }

            fun onOpenJson() {
                val file = getFile(getInitPath("jsonPath"), ".json", title = "Load JSON Specification")

                if (file != null) {
                    properties["jsonPath"] = file.parent ?: "./"

                    val stream: InputStream = file.inputStream()
                    val reader: JsonReader = Json.createReader(stream)
                    val data: JsonObject = reader.readObject()

                    EventBus.publish(AppTitle(file.name))

                    fractal.fromJson(data)
                    jsonLoaded = true
                }
            }

            fun onOpenPalette() {
                val file = getFile(getInitPath("palPath"), ".pal", title = "Load Palette")

                if (file != null) {
                    properties["palPath"] = file.parent ?: "./"

                    val stream = ObjectInputStream(file.inputStream())

                    palette.readObject(stream)
                    stream.close()

                    fractal.refreshImage()
                }
            }

            fun onSave() {
                val file = getFile(getInitPath("2lzPath"), ".2lz", true, "Save Fractal Image")

                if (file != null) {
                    properties["2lzPath"] = file.parent ?: "./"

                    val stream = ObjectOutputStream(file.outputStream())

                    fractal.writeObject(stream)
                    palette.writeObject(stream)

                    stream.close()

                    EventBus.publish(AppTitle(file.name))
                }
            }

            fun onSaveJson() {
                val file = getFile(getInitPath("jsonPath"), ".json", true, title = "Save JSON Specification")

                if (file != null) {
                    properties["jsonPath"] = file.parent ?: "./"

                    EventBus.publish(AppTitle(file.name))

                    val stream: OutputStream = file.outputStream()
                    val writer = Json.createWriter(stream)
                    val data = fractal.toJson()

                    writer.writeObject(data)
                }
            }

            fun onSaveImage() {
                val file = getFile(getInitPath("imgPath"), ".png", true, title = "Export Image")

                if (file != null) {
                    properties["imgPath"] = file.parent ?: "./"
                    val filename = file.absolutePath

                    EventBus.publish(FileEvent(FileAction.WRITE_IMAGE, filename))
                }
            }

            fun onSavePalette() {
                val file = getFile(getInitPath("palPath"), ".pal", true, title = "Save Palette")

                if (file != null) {
                    properties["palPath"] = file.parent ?: "./"

                    val stream = ObjectOutputStream(file.outputStream())

                    palette.writeObject(stream)
                    stream.close()
                }
            }

            if (!subscribed) {
                subscribed = true

                EventBus.listen(AppTitle::class.java).subscribe {
                    var appTitle = appName + ": " + VersionInfo.VERSION

                    if (it.title.isNotEmpty()) {
                        appTitle += " - " + it.title
                    }

                    this.window.title = appTitle
                }

                EventBus.listen(FileEvent::class.java).subscribe {
                    when (it.action) {
                        FileAction.OPEN_FRACTAL -> onOpen()
                        FileAction.OPEN_JSON -> onOpenJson()
                        FileAction.OPEN_PALETTE -> onOpenPalette()
                        FileAction.SAVE_FRACTAL -> onSave()
                        FileAction.SAVE_JSON -> onSaveJson()
                        FileAction.SAVE_PALETTE -> onSavePalette()
                        FileAction.SAVE_IMAGE -> onSaveImage()
                        else -> {}
                    }
                }

                EventBus.listen(NewPaletteEvent::class.java).subscribe {
                    palette = Palette(it.palette)
                    checkRefresh()
                }

                EventBus.publish(AppTitle(""))
            }

            Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                Row(Modifier.weight(1f)) {
                    if (jsonLoaded) {
                        YesNoAlert(
                            title = "JSON File Loaded",
                            text = "Do you want to recalculate now?",
                            dismiss = { jsonLoaded = false },
                            confirm = {
                                EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE))
                                jsonLoaded = false
                            })
                    } else {
                        Column(Modifier.weight(1f)) { FractalImage(fractal.params, palette) }
                        Column { SettingsPanel(fractal.params, palette, refresh) }
                    }
                }
                PaletteCanvas(palette, fractal)
                Row(Modifier.fillMaxWidth().padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Color Range:", color = MaterialTheme.colorScheme.primary) }
                    Column(Modifier.weight(1f)) {
                        PaletteSlider(1f, 100f, PaletteSliderType.COLOR_RANGE, palette) {
                            palette.colorRange = it
                            checkRefresh()
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Palette Size:", color = MaterialTheme.colorScheme.primary) }
                    Column(Modifier.weight(1f)) {
                        PaletteSlider(2f, 512f, PaletteSliderType.SIZE, palette) {
                            palette.size = it
                            checkRefresh()
                        }
                    }
                }
                Row { PaletteBar() }
                StatusBar(palette)
            }
        }
    }
}
