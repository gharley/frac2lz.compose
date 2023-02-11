package components

import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
        }, modifier = Modifier.width(200.dp)
    )
}
