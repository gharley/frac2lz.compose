package com.resmass.frac2lz.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import kotlin.system.exitProcess

@Composable
fun MainWindow(state: Frac2lzApplicationState, onClose: () -> Unit) {
    Window(onCloseRequest = onClose, title = state.settings.title) {
        MaterialTheme {
            MainMenu(state)
        }
    }
}