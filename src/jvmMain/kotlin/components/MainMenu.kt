package components

import EventBus
import action.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.MainMenu() {
    MenuBar {
        Menu("File") {
            Item("Open Fractal", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_FRACTAL)) })
            Item("Open JSON file", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_JSON)) })
            Separator()
            Item("Open Palette", onClick = { EventBus.publish(FileEvent(FileAction.OPEN_PALETTE)) })
            Separator()
            Item("Save Fractal", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_FRACTAL)) })
            Item("Save JSON file", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_JSON)) })
            Separator()
            Item("Save Palette", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_PALETTE)) })
            Separator()
            Item("Export to image file", onClick = { EventBus.publish(FileEvent(FileAction.SAVE_IMAGE)) })
            Separator()
            Item("Exit", onClick = { EventBus.publish(UIEvent(UIAction.EXIT)) })
        }
        Menu("Fractal") {
            Item("Calculate Base Fractal",
                onClick = { EventBus.publish(CalculateEvent(CalculateAction.CALCULATE_BASE)) },
                shortcut = KeyShortcut(Key.F5, true)
            )
            Separator()
            Item("Recalculate", onClick = { EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE)) })
            Item("Refine", onClick = { EventBus.publish(CalculateEvent(CalculateAction.REFINE)) })
            Separator()
            Item("Refresh Image",
                onClick = { EventBus.publish(CalculateEvent(CalculateAction.REFRESH)) },
                shortcut = KeyShortcut(Key.F5)
            )
            Separator()
            Item("Show Histogram", onClick = { EventBus.publish(CalculateEvent(CalculateAction.SHOW_HISTOGRAM)) })
        }
    }
}