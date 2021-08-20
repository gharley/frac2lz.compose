package com.resmass.frac2lz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.resmass.frac2lz.ui.Frac2lzApplicationState

@Composable
fun StatusBar(state: Frac2lzApplicationState) {
    Row(Modifier) {
        addTextField("Iterations -> Allowed: ", state.settings.maxIterationsProperty)
        addTextField("Used: ", state.settings.actualIterationsProperty)
        addTextField("RE Center: ", state.settings.centerRealProperty)
        addTextField("IM Center: ", state.settings.centerImaginaryProperty)
        addTextField("Zoom: ", state.settings.magnificationProperty)
        addTextField("Width: ", state.settings.imageWidthProperty)
        addTextField("Height: ", state.settings.imageHeightProperty)
        addTextField("Color Range: ", state.settings.colorRangeProperty)
        addTextField("Palette Size: ", state.settings.numColorsProperty)
    }
}

@Composable
fun addTextField(labelText: String, boundProperty: String) {
    Column {
        Row {
            Text(labelText)
            Text(boundProperty)
        }
    }
}

