package vin.pam.closetly.feature.wardrobe.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.feature.wardrobe.WardrobeFormViewModel
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.ui.components.WardrobePhoto

@Composable
fun WardrobeFormDestination(viewModel: WardrobeFormViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    WardrobeFormScreen(
        state = state, onEdit = viewModel::edit, onSave = viewModel::save, onBack = onBack,
        onCreateCameraUri = viewModel::createCameraUri, onPhoto = viewModel::importPhoto,
        onPhotoError = viewModel::reportPhotoError, onEditPhoto = viewModel::openPhotoEditor,
        onRemovePhoto = viewModel::removePhoto,
    )
    state.photoEditor?.let { editor ->
        WardrobePhotoEditor(
            state = editor, onClose = viewModel::closePhotoEditor, onUse = viewModel::usePhoto,
            onPickNeutral = viewModel::pickNeutral, onNeutralSelected = viewModel::calibrate,
            onResetCalibration = viewModel::resetCalibration, onToggleBackground = viewModel::toggleBackground,
        )
    }
}

@Composable
private fun WardrobeFormScreen(
    state: WardrobeFormUiState,
    onEdit: (ClothingDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onCreateCameraUri: () -> Uri,
    onPhoto: (Uri) -> Unit,
    onPhotoError: (String) -> Unit,
    onEditPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val draft = state.draft
    val enabled = !state.isLoading && !state.isSaving && !state.isImporting
    val focus = LocalFocusManager.current
    val requestBack = {
        if (!state.isSaving) {
            if (state.hasChanges) confirmDiscard = true else onBack()
        }
    }
    BackHandler(enabled = state.photoEditor == null) { requestBack() }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onPhoto)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { onPhoto(it.toUri()) }
        pendingCameraUri = null
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = requestBack, enabled = !state.isSaving) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali ke koleksi")
                }
                Text(if (state.isEditing) "Edit pakaian" else "Tambah pakaian", style = MaterialTheme.typography.titleLarge)
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("* Wajib diisi. Informasi lain bisa ditambahkan nanti.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (state.isLoading) CircularProgressIndicator()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Foto pakaian · Opsional", style = MaterialTheme.typography.titleMedium)
                if (draft.imagePath != null) {
                    WardrobePhoto(
                        draft.imagePath, "Foto ${draft.name.ifBlank { "pakaian" }}",
                        Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(22.dp)),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (draft.photoAdjustments.backgroundRemoved) Text("Background transparan", style = MaterialTheme.typography.labelMedium)
                        if (draft.photoAdjustments.redGain != 1.0 || draft.photoAdjustments.greenGain != 1.0 || draft.photoAdjustments.blueGain != 1.0) {
                            Text("Warna dikalibrasi", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Row {
                        TextButton(onClick = onEditPhoto, enabled = enabled) { Text("Atur foto") }
                        TextButton(onClick = onRemovePhoto, enabled = enabled) { Text("Hapus foto") }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddAPhoto, null)
                            Text("Tambahkan foto pakaianmu", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        enabled = enabled,
                        onClick = {
                            try {
                                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } catch (_: ActivityNotFoundException) {
                                onPhotoError("Pemilih foto tidak tersedia di perangkat ini.")
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, null, Modifier.padding(end = 8.dp))
                        Text("Galeri")
                    }
                    OutlinedButton(
                        enabled = enabled,
                        onClick = {
                            try {
                                val uri = onCreateCameraUri()
                                pendingCameraUri = uri.toString()
                                camera.launch(uri)
                            } catch (_: ActivityNotFoundException) {
                                pendingCameraUri = null
                                onPhotoError("Aplikasi kamera tidak tersedia di perangkat ini.")
                            } catch (error: Exception) {
                                pendingCameraUri = null
                                onPhotoError(error.message ?: "Kamera belum dapat dibuka.")
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.AddAPhoto, null, Modifier.padding(end = 8.dp))
                        Text("Kamera")
                    }
                }
                if (state.isImporting) {
                    CircularProgressIndicator()
                    Text("Menyiapkan foto…", style = MaterialTheme.typography.bodySmall)
                }
            }
            ClothingField(
                "Nama pakaian *", draft.name, { onEdit(draft.copy(name = it)) }, enabled,
                isError = state.attemptedSave && draft.name.trim().length !in 2..80,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kategori *", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WardrobeCategory.entries.forEach { category ->
                        CategoryChip(category.label, draft.category == category, enabled = enabled) {
                            onEdit(draft.copy(category = category))
                        }
                    }
                }
                if (state.attemptedSave && draft.category == null) Text("Pilih kategori pakaian.", color = MaterialTheme.colorScheme.error)
            }
            ClothingField("Warna", draft.color, { onEdit(draft.copy(color = it)) }, enabled)
            ClothingField("Ukuran", draft.size, { onEdit(draft.copy(size = it)) }, enabled)
            ClothingField("Gaya", draft.style, { onEdit(draft.copy(style = it)) }, enabled)
            if (state.errorMessage != null) Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            ClosetlyButton(
                if (state.isEditing) "Simpan perubahan" else "Simpan ke Koleksi",
                onClick = { focus.clearFocus(); onSave() },
                modifier = Modifier.fillMaxWidth(), enabled = enabled, isLoading = state.isSaving,
            )
        }
    }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text("Keluar tanpa menyimpan?") },
        text = { Text("Perubahan pakaian ini belum disimpan ke koleksi.") },
        confirmButton = { TextButton(onClick = onBack) { Text("Keluar") } },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Lanjut edit") } },
    )
}

@Composable
private fun ClothingField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean, isError: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
        shape = RoundedCornerShape(16.dp), isError = isError,
    )
}
