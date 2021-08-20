package com.resmass.frac2lz.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Settings {
    private val default = "Frac2lz"
    var title: String by mutableStateOf(default)
        private set

    var actualIterationsProperty: String by mutableStateOf("0")
    var maxIterationsProperty: String by mutableStateOf("0")
    var centerImaginaryProperty: String by mutableStateOf("0")
    var centerRealProperty: String by mutableStateOf("0")
    var magnificationProperty: String by mutableStateOf("0")
    var imageHeightProperty: String by mutableStateOf("0")
    var imageWidthProperty: String by mutableStateOf("0")
    var colorRangeProperty: String by mutableStateOf("0")
    var numColorsProperty: String by mutableStateOf("0")

    fun setAppTitle(title: String){
        this.title = "$default - $title"
    }
}