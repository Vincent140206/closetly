package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette
import vin.pam.closetly.feature.inspiration.ui.SheetState

/**
 * One bottom sheet for the whole save flow. "Buat Koleksi Baru" swaps the sheet content to the
 * create form instead of opening another popup, then returns to the list with the new collection selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationSheet(
    sheet: SheetState,
    post: InspirationPost?,
    collections: List<CollectionSummary>,
    onDismiss: () -> Unit,
    onSelectCollection: (String) -> Unit,
    onStartCreateInSheet: () -> Unit,
    onCancelCreateInSheet: () -> Unit,
    onSubmitCreateInSheet: (String) -> Unit,
    onConfirmSave: () -> Unit,
    onSubmitCollectionForm: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InspirationPalette.CardWhite,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            when (sheet) {
                is SheetState.SaveToCollection ->
                    if (sheet.isCreating) {
                        CollectionForm(
                            title = "Buat Koleksi Baru", submitLabel = "Buat", initialName = "",
                            onBack = onCancelCreateInSheet, onSubmit = onSubmitCreateInSheet, onCancel = onCancelCreateInSheet,
                        )
                    } else {
                        SaveContent(sheet, post, collections, onSelectCollection, onStartCreateInSheet, onConfirmSave, onDismiss)
                    }
                SheetState.CreateCollection -> CollectionForm(
                    title = "Buat Koleksi Baru", submitLabel = "Buat", initialName = "",
                    onBack = null, onSubmit = onSubmitCollectionForm, onCancel = onDismiss,
                )
                is SheetState.RenameCollection -> CollectionForm(
                    title = "Ubah Nama Koleksi", submitLabel = "Simpan", initialName = sheet.currentName,
                    onBack = null, onSubmit = onSubmitCollectionForm, onCancel = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun SaveContent(
    sheet: SheetState.SaveToCollection,
    post: InspirationPost?,
    collections: List<CollectionSummary>,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CollectionThumb(post, Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)))
        Column {
            Text("Simpan ke Koleksi", color = InspirationPalette.Heading, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Pilih folder untuk item ini", color = InspirationPalette.Muted, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(12.dp))
    collections.forEach { summary ->
        val selected = summary.collection.id == sheet.selectedCollectionId
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(if (selected) InspirationPalette.Vanilla else InspirationPalette.CardWhite)
                .border(1.dp, if (selected) InspirationPalette.SoftYellow else InspirationPalette.CardWhite, RoundedCornerShape(14.dp))
                .clickable { onSelect(summary.collection.id) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CollectionThumb(summary.cover, Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(summary.collection.name, color = InspirationPalette.Heading, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${summary.postCount} items", color = InspirationPalette.Muted, fontSize = 12.sp)
            }
            RadioButton(
                selected = selected, onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = InspirationPalette.Golden, unselectedColor = InspirationPalette.Border),
            )
        }
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onCreate).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("+", Modifier.width(40.dp), color = InspirationPalette.Pressed, fontSize = 20.sp)
        Text("Buat Koleksi Baru", Modifier.padding(start = 12.dp), color = InspirationPalette.Heading, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(12.dp))
    PrimaryButton("Simpan", onSave, Modifier.fillMaxWidth(), enabled = sheet.selectedCollectionId != null)
    Spacer(Modifier.height(8.dp))
    SecondaryButton("Batal", onCancel, Modifier.fillMaxWidth())
}

@Composable
private fun CollectionForm(
    title: String,
    submitLabel: String,
    initialName: String,
    onBack: (() -> Unit)?,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onBack != null) {
            CircleIconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", Modifier.size(18.dp), InspirationPalette.Heading) }
        }
        Column {
            Text(title, color = InspirationPalette.Heading, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Organisir inspirasi gayamu lebih rapi", color = InspirationPalette.Muted, fontSize = 12.sp)
        }
    }
    Text("Nama Koleksi", Modifier.padding(top = 16.dp, bottom = 6.dp), color = InspirationPalette.Heading, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
        value = name, onValueChange = { if (it.length <= 30) name = it },
        placeholder = { Text("Contoh: Liburan Musim Panas", color = InspirationPalette.Muted) },
        singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InspirationPalette.Pressed, unfocusedBorderColor = InspirationPalette.Border,
            focusedContainerColor = InspirationPalette.OffWhite, unfocusedContainerColor = InspirationPalette.OffWhite,
            cursorColor = InspirationPalette.Pressed,
        ),
    )
    Spacer(Modifier.height(24.dp))
    PrimaryButton(submitLabel, { onSubmit(name) }, Modifier.fillMaxWidth(), enabled = name.isNotBlank())
    Spacer(Modifier.height(8.dp))
    SecondaryButton("Batal", onCancel, Modifier.fillMaxWidth())
}
