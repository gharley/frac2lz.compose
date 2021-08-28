package com.resmass.frac2lz.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement

import com.resmass.frac2lz.common.LocalAppResources
import com.resmass.frac2lz.ui.components.FractalImage
import com.resmass.frac2lz.ui.components.StatusBar

@Preview
@Composable
fun MainWindow(state: Frac2lzApplicationState, onClose: () -> Unit) {
    MaterialTheme {
        Window(onCloseRequest = onClose, title = state.settings.title, icon = LocalAppResources.current.resources.icon32) {
            window.placement = WindowPlacement.Maximized
            MainMenu(state)
            Box(Modifier.fillMaxSize()) {
//                Column {
                    FractalImage(state, Modifier.size(width = 1920.dp, height = 1080.dp).background(color = Color.Gray))
                    Box(Modifier.align(alignment = Alignment.BottomStart)) {
                        StatusBar(state)
                    }
//                }
            }
        }
    }
}