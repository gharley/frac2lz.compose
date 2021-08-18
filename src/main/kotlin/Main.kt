// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.application
import com.resmass.frac2lz.ui.MainWindow

fun main() = application {
    MainWindow(::exitApplication)
}