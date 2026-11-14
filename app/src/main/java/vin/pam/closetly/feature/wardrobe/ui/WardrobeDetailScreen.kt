package vin.pam.closetly.feature.wardrobe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.ui.components.WardrobePhoto

@Composable
fun WardrobeDetailScreen(
    item: ClothingItem?,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onFavorite: (ClothingItem) -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
) {
    var confirmArchive by remember { mutableStateOf(false) }
    ClosetlyScaffold(title = "Detail pakaian", onBack = onBack) { padding ->
        if (isLoading && item == null) {
            LoadingState(Modifier.padding(padding))
        } else if (item == null) {
            Text(error ?: "Pakaian tidak ditemukan.", Modifier.padding(padding).padding(24.dp))
        } else {
            Column(
                Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WardrobePhoto(item.imagePath, item.name, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)))
                Text(item.category.label + if (item.isArchived) " · Diarsipkan" else "", style = MaterialTheme.typography.labelLarge)
                Text(item.name, style = MaterialTheme.typography.headlineMedium)
                if (item.color.isNotBlank()) DetailField("Warna", item.color)
                if (item.size.isNotBlank()) DetailField("Ukuran", item.size)
                if (item.style.isNotBlank()) DetailField("Gaya", item.style)
                if (item.photoAdjustments.backgroundRemoved) Text("Background foto transparan", style = MaterialTheme.typography.bodySmall)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
                if (item.isArchived) {
                    ClosetlyButton("Pulihkan ke koleksi", onRestore, Modifier.fillMaxWidth())
                } else {
                    ClosetlyButton("Edit pakaian", onEdit, Modifier.fillMaxWidth())
                    ClosetlyButton(
                        if (item.isFavorite) "Hapus dari favorit" else "Jadikan favorit",
                        { onFavorite(item) }, Modifier.fillMaxWidth(), style = ClosetlyButtonStyle.Secondary,
                    )
                    TextButton(onClick = { confirmArchive = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Hapus pakaian", color = MaterialTheme.colorScheme.error)
                    }
                }
                Text(
                    "Ditambahkan " + Instant.ofEpochMilli(item.createdAt).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID"))),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (confirmArchive) AlertDialog(
        onDismissRequest = { confirmArchive = false },
        title = { Text("Hapus pakaian dari koleksi?") },
        text = { Text("Pakaian dipindahkan ke arsip agar referensi outfit tetap tersimpan. Kamu bisa memulihkannya melalui filter Arsip.") },
        confirmButton = { TextButton(onClick = { confirmArchive = false; onArchive() }) { Text("Hapus", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmArchive = false }) { Text("Batal") } },
    )
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
