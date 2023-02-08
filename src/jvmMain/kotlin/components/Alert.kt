package components

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YesNoAlert(
    title: String = "Alert",
    text: String = "Your text here",
    dismiss: () -> Unit,
    confirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Text(
                title,
                overflow = TextOverflow.Visible,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text,
                overflow = TextOverflow.Visible,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    confirm()
                    dismiss()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dismiss()
                }
            ) {
                Text("No")
            }
        },
    )
}
