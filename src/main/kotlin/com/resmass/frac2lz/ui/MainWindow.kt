package com.resmass.frac2lz.ui

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import kotlin.system.exitProcess

@Composable
fun MainWindow(onClose: () -> Unit) {
    Window(onCloseRequest = onClose, title = "Frac2lz") {
        MaterialTheme {
            MenuBar {
                Menu("File") {
                    Item("Open Fractal", onClick = {})
                    Item("Open JSON File", onClick = {})
                    Item("Open Palette", onClick = {})
                    Separator()
                    Item("Save Fractal", onClick = {})
                    Item("Save JSON File", onClick = {})
                    Item("Save Palette", onClick = {})
                    Separator()
                    Item("Export To Image File", onClick = {})
                    Separator()
                    Item("Exit", onClick = { exitProcess(0) })
                }
            }
        }
    }
}