package com.resmass.frac2lz.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.resmass.frac2lz.ui.Frac2lzApplicationState

@Preview
@Composable
fun StatusBar(state: Frac2lzApplicationState) {
    Divider()
    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        addTextField("Iterations -> Allowed: ", state.settings.maxIterationsProperty)
        addTextField("Used: ", state.settings.actualIterationsProperty)
        addTextField("RE Center: ", state.settings.centerRealProperty)
        addTextField("IM Center: ", state.settings.centerImaginaryProperty)
        addTextField("Zoom: ", state.settings.magnificationProperty)
        addTextField("Width: ", state.settings.imageWidthProperty)
        addTextField("Height: ", state.settings.imageHeightProperty)
        addTextField("Color Range: ", state.settings.colorRangeProperty)
        addTextField("Palette Size: ", state.settings.numColorsProperty, false)
    }
}

@Composable
fun addTextField(labelText: String, boundProperty: String, addBorder: Boolean = true) {
    Column {
        Row {
            Text(labelText, Modifier.padding(horizontal = 5.dp, vertical = 10.dp))
            Text(
                boundProperty,
                Modifier
                    .padding(end = 5.dp, top = 10.dp, bottom = 10.dp)
                    .drawBehind {
                        try {
                            check(addBorder)
                            val x = size.width + 5.dp.toPx()
                            drawLine(
                                Color.Black,
                                Offset(x, 0f),
                                Offset(x, size.height)
                            )
                        }catch (ex: Exception){}
                    }
            )
        }
    }
}

