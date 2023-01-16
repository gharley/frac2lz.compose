import action.CalculateAction
import action.CalculateEvent
import action.FileAction
import action.FileEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import kotlin.system.exitProcess

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
            Item("Exit", onClick = { exitProcess(0) })
        }
        Menu("Fractal") {
            Item("Calculate Base Fractal", onClick = {EventBus.publish(CalculateEvent(CalculateAction.CALCULATE_BASE)) })
            Separator()
            Item("Recalculate", onClick = {EventBus.publish(CalculateEvent(CalculateAction.RECALCULATE)) })
            Item("Refine", onClick = {EventBus.publish(CalculateEvent(CalculateAction.REFINE)) })
            Separator()
            Item("Refresh Image", onClick = {EventBus.publish(CalculateEvent(CalculateAction.REFRESH)) })
            Separator()
            Item("Show Histogram", onClick = {EventBus.publish(CalculateEvent(CalculateAction.SHOW_HISTOGRAM)) })
        }
    }
}