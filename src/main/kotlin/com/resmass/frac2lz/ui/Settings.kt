package com.resmass.frac2lz.ui

import androidx.compose.runtime.*

class Settings {
    private val default = "Frac2lz"
    var title: String by mutableStateOf(default)
        private set

    var actualIterationsProperty: String by mutableStateOf("4389")
    var maxIterationsProperty: String by mutableStateOf("5000")
    var centerImaginaryProperty: String by mutableStateOf("0.0")
    var centerRealProperty: String by mutableStateOf("0.0")
    var magnificationProperty: String by mutableStateOf("1.0")
    var imageHeightProperty: String by mutableStateOf("1080")
    var imageWidthProperty: String by mutableStateOf("1920")
    var colorRangeProperty: String by mutableStateOf("1")
    var numColorsProperty: String by mutableStateOf("16")

    fun setAppTitle(title: String){
        this.title = "$default - $title"
    }
}
