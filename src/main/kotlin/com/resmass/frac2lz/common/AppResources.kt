package com.resmass.frac2lz.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

val LocalAppResources = staticCompositionLocalOf<AppResources> {
    error("LocalAppResources isn't provided")
}

@Composable
fun rememberAppResources(): AppResources {
    val resources = Resources(
        rememberImageResource("frac2lz16.png"),
        rememberImageResource("frac2lz32.png"),
        rememberImageResource("frac2lz48.png"),
        rememberImageResource("frac2lz64.png"),
        rememberImageResource("animate32.png"),
        rememberImageResource("default32.png"),
        rememberImageResource("random32.png"),
        rememberImageResource("smooth32.png"),
    )
    return remember { AppResources(resources) }
}

class AppResources(val resources: Resources)

@Composable
fun rememberImageResource(filename: String): Painter {
    return painterResource(filename)
}

data class Resources(val icon16: Painter, val icon32: Painter, val icon48: Painter, val icon64: Painter,
val btnAnimate: Painter, val btnDefault: Painter, val btnRandom: Painter, val btnSmooth: Painter)
