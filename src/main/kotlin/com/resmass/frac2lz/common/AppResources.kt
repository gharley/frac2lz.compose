package com.resmass.frac2lz.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import java.net.URL

val LocalAppResources = staticCompositionLocalOf<AppResources> {
    error("LocalAppResources isn't provided")
}

@Composable
fun rememberAppResources(): AppResources {
    val icon = rememberImageResource()
    return remember { AppResources(icon) }
}

class AppResources(val icon: Painter)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberVectorPainter(image: ImageVector, tintColor: Color) =
    rememberVectorPainter(
        defaultWidth = image.defaultWidth,
        defaultHeight = image.defaultHeight,
        viewportWidth = image.viewportWidth,
        viewportHeight = image.viewportHeight,
        name = image.name,
        tintColor = tintColor,
        tintBlendMode = image.tintBlendMode,
        content = { _, _ -> RenderVectorGroup(group = image.root) }
    )

@Composable
fun rememberImageResource(): Painter {
    val loader = ResourceLoader()
    return painterResource(loader.getFile("frac2lz32.png"))
}

class ResourceLoader(){
    private val classLoader: ClassLoader = this.javaClass.classLoader

    fun getFile(filename: String): String {
        val resource: URL? = classLoader.getResource(filename)

        return (resource.toString()).substring(6)
    }
}