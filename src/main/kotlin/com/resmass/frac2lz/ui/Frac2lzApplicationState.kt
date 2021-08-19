package com.resmass.frac2lz.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberApplicationState() = remember {
    Frac2lzApplicationState()
}

class Frac2lzApplicationState {
    val settings = Settings()
}