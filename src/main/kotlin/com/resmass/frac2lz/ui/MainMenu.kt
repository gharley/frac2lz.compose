package com.resmass.frac2lz.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import kotlin.system.exitProcess

@Composable
fun FrameWindowScope.MainMenu(state: Frac2lzApplicationState){
    MenuBar {
        Menu("File") {
            Item("Open Fractal", onClick = {state.settings.setAppTitle("Open Fractal")})
            Item("Open JSON File", onClick = {state.settings.setAppTitle("Open JSON File")})
            Item("Open Palette", onClick = {state.settings.setAppTitle("Open Palette")})
            Separator()
            Item("Save Fractal", onClick = {state.settings.setAppTitle("Save Fractal")})
            Item("Save JSON File", onClick = {state.settings.setAppTitle("Save JSON File")})
            Item("Save Palette", onClick = {state.settings.setAppTitle("Save Palette")})
            Separator()
            Item("Export To Image File", onClick = {state.settings.setAppTitle("Export To Image File")})
            Separator()
            Item("Exit", onClick = { exitProcess(0) })
        }

        Menu("Fractal"){
            Item("Calculate Base Fractal", onClick = {state.settings.setAppTitle("Calculate Base Fractal")})
            Separator()
            Item("Recalculate", onClick = {state.settings.setAppTitle("Recalculate")})
            Item("Refine", onClick = {state.settings.setAppTitle("Refine")})
            Separator()
            Item("Refresh Image", onClick = {state.settings.setAppTitle("Refresh Image")})
            Separator()
            Item("Show Histogram", onClick = {state.settings.setAppTitle("Show Histogram")})
        }
    }
}