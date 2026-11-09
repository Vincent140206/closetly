package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette

/** Back button, title, and an optional Manage Collection action. */
@Composable
fun InspirationTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onManageClick: (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().background(InspirationPalette.OffWhite).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", Modifier.size(18.dp), InspirationPalette.Heading) }
        Text(
            title,
            Modifier.weight(1f).padding(horizontal = 8.dp),
            color = InspirationPalette.Heading, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        if (onManageClick != null) {
            CircleIconButton(onManageClick) { Icon(Icons.Default.Settings, "Kelola koleksi", Modifier.size(18.dp), InspirationPalette.Heading) }
        } else {
            Spacer(Modifier.size(36.dp))
        }
    }
}

@Composable
fun CircleIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(InspirationPalette.CardWhite)
            .border(1.dp, InspirationPalette.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
