package com.resmass.frac2lz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window

import com.resmass.frac2lz.ui.components.StatusBar

@Composable
fun MainWindow(state: Frac2lzApplicationState, onClose: () -> Unit) {
    Window(onCloseRequest = onClose, title = state.settings.title) {
        MaterialTheme {
            MainMenu(state)
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.align(Alignment.TopStart).background(Color.Cyan)) { Text("Cyan") }
                Box(Modifier.align(Alignment.BottomEnd).background(Color.Yellow)) { Text("Yellow") }
                StatusBar(state)
            }
        }
    }
}