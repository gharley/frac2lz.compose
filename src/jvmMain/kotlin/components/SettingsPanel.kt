package components

import EventBus
import action.UIAction
import action.UIEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Properties

data class UISettings(var colorFromFractal:Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel() {
    Box(Modifier.background(Color.Cyan).padding(20.dp).wrapContentSize()) {
        val settings: UISettings = remember { UISettings() }
        var trigger by remember { mutableStateOf(0) }

        fun broadcastSettings() {
            EventBus.publish(UIEvent(UIAction.SETTINGS, settings))
            trigger++
        }

        Row {
            trigger
            Text("Color from Fractal")
            Checkbox(settings.colorFromFractal,
                { settings.colorFromFractal = it; broadcastSettings() })
        }
    }
}