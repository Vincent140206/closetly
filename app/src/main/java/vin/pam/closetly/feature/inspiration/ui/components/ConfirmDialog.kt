package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette

/** Confirmation for destructive actions. The destructive button is red; Cancel is neutral. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(InspirationPalette.CardWhite).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, color = InspirationPalette.Heading, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(message, color = InspirationPalette.Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
            DangerButton(confirmLabel, onConfirm, Modifier.fillMaxWidth())
            SecondaryButton("Batal", onDismiss, Modifier.fillMaxWidth())
        }
    }
}
