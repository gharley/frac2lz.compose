package com.resmass.frac2lz.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Settings {
    private val default = "Frac2lz"
    var title by mutableStateOf(default)
        private set

    fun setAppTitle(title: String){
        this.title = "$default - $title"
    }
}