package com.resmass.frac2lz.ui

import androidx.compose.runtime.Composable

@Composable
fun Frac2lzApplication(state: Frac2lzApplicationState, onClose: () -> Unit){
    MainWindow(state, onClose)
}