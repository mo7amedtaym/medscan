package com.albarmajy.medscan.ui.customUi

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.albarmajy.medscan.ui.theme.PrimaryBlue

@Composable
fun CustomAlert(title: String, message: String, confirmText: String = "Confirm", dismissText: String = "Cancel", icon : @Composable (() -> Unit)?,onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = icon,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,

    )
}

@Preview
@Composable
private fun CustomAlertPreview() {
    CustomAlert(
        "Plan Expired",
        "You need an active medication plan to enable reminders. Would you like to set up a new schedule now",
        icon ={ Icon(Icons.Default.EventRepeat, contentDescription = null, tint = PrimaryBlue) },
        onDismiss = { /*TODO*/ },
        onConfirm = { /*TODO*/ })
}
