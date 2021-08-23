// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.application
import com.resmass.frac2lz.common.LocalAppResources
import com.resmass.frac2lz.common.rememberAppResources
import com.resmass.frac2lz.ui.Frac2lzApplication
import com.resmass.frac2lz.ui.rememberApplicationState

fun main() = application {
    CompositionLocalProvider(LocalAppResources provides rememberAppResources()) {
        Frac2lzApplication(rememberApplicationState(), ::exitApplication)
    }
}