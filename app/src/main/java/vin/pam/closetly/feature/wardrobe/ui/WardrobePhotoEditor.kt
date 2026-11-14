package vin.pam.closetly.feature.wardrobe.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.min
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.feature.wardrobe.photo.WhiteBalance

@Composable
internal fun WardrobePhotoEditor(
    state: PhotoEditorState,
    onClose: () -> Unit,
    onUse: () -> Unit,
    onPickNeutral: () -> Unit,
    onNeutralSelected: (Int, Int) -> Unit,
    onResetCalibration: () -> Unit,
    onToggleBackground: () -> Unit,
) {
    var showOriginal by remember { mutableStateOf(false) }
    val bitmap = if (showOriginal || state.isPickingNeutral) state.original else state.preview
    val background = MaterialTheme.colorScheme.surfaceVariant
    val square = MaterialTheme.colorScheme.outlineVariant
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Siapkan foto", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Tutup pengaturan foto") }
                }
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryChip("Asli", showOriginal || state.isPickingNeutral) { showOriginal = true }
                        CategoryChip("Hasil", !showOriginal && !state.isPickingNeutral) { showOriginal = false }
                    }
                    Box(
                        Modifier.fillMaxWidth().height(300.dp).drawBehind {
                            drawRect(background)
                            val side = 12.dp.toPx()
                            for (row in 0..(size.height / side).toInt()) {
                                for (column in 0..(size.width / side).toInt()) {
                                    if ((row + column) % 2 == 0) drawRect(
                                        square, Offset(column * side, row * side), Size(side, side),
                                    )
                                }
                            }
                        }.pointerInput(state.original, state.isPickingNeutral, state.isProcessing) {
                            detectTapGestures { point ->
                                if (state.isPickingNeutral && !state.isProcessing) {
                                    val scale = min(size.width.toFloat() / state.original.width, size.height.toFloat() / state.original.height)
                                    val left = (size.width - state.original.width * scale) / 2f
                                    val top = (size.height - state.original.height * scale) / 2f
                                    val x = (point.x - left) / scale
                                    val y = (point.y - top) / scale
                                    if (x >= 0 && y >= 0 && x < state.original.width && y < state.original.height) {
                                        showOriginal = false
                                        onNeutralSelected(x.toInt(), y.toInt())
                                    }
                                }
                            }
                        },
                    ) {
                        Image(
                            bitmap.asImageBitmap(), "Pratinjau foto pakaian",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                        )
                    }
                    if (state.isProcessing) {
                        CircularProgressIndicator()
                        Text("Menyiapkan model dan memproses foto…", style = MaterialTheme.typography.bodyMedium)
                        Text("Penggunaan pertama penghapus background memerlukan internet untuk mengunduh model.", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Kalibrasi warna", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (state.isPickingNeutral) "Ketuk area putih atau abu netral pada foto asli."
                        else "Pilih kartu abu atau area putih netral yang terkena cahaya yang sama dengan pakaian untuk mengoreksi bias warna.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onPickNeutral, enabled = !state.isProcessing) {
                            Text(if (state.isPickingNeutral) "Batal pilih titik" else "Pilih titik netral")
                        }
                        TextButton(onClick = onResetCalibration, enabled = !state.isProcessing) { Text("Reset warna") }
                    }
                    Text(
                        if (state.balance != WhiteBalance()) "Kalibrasi warna diterapkan" else "Warna asli",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    OutlinedButton(onClick = onToggleBackground, enabled = !state.isProcessing, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.backgroundRemoved) "Pulihkan background" else "Hapus background")
                    }
                    if (state.backgroundRemoved) Text("Background transparan", style = MaterialTheme.typography.labelMedium)
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                ClosetlyButton(
                    "Gunakan foto", onUse,
                    modifier = Modifier.fillMaxWidth().padding(24.dp), enabled = !state.isProcessing,
                )
            }
        }
    }
}
