package vin.pam.closetly.feature.outfit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.core.ui.components.ConfirmDialog
import vin.pam.closetly.core.ui.components.EmptyState
import vin.pam.closetly.core.ui.components.ErrorState
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
fun OutfitScreen(
    uiState: OutfitLoadState<List<Outfit>>,
    onCreateOutfit: () -> Unit,
    onOutfitClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    ClosetlyScaffold(
        title = "Mix & Match Outfit",
        actionLabel = "Buat outfit",
        onAction = onCreateOutfit,
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (uiState) {
            OutfitLoadState.Loading -> LoadingState(modifier)
            is OutfitLoadState.Error -> ErrorState(uiState.message, modifier, onRetry)
            is OutfitLoadState.Loaded -> {
                if (uiState.value.isEmpty()) {
                    EmptyState("Belum ada outfit", "Buat outfit dari item lemari.", modifier)
                } else {
                    OutfitList(modifier) {
                        items(uiState.value, key = { it.id }) { outfit ->
                            ClosetlyCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOutfitClick(outfit.id) },
                            ) {
                                Text(outfit.name, style = MaterialTheme.typography.titleMedium)
                                Text("${outfit.items.size} item")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitDetailScreen(
    uiState: OutfitDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDeleted: () -> Unit,
    onItemClick: ((String) -> Unit)?,
    onRetry: () -> Unit,
) {
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }
    ClosetlyScaffold(title = "Detail outfit", onBack = onBack) { padding ->
        val modifier = Modifier.padding(padding)
        when (val outfit = uiState.outfit) {
            OutfitLoadState.Loading -> LoadingState(modifier)
            is OutfitLoadState.Error -> ErrorState(outfit.message, modifier, onRetry)
            is OutfitLoadState.Loaded -> OutfitList(modifier) {
                item {
                    Text(outfit.value.name, style = MaterialTheme.typography.headlineSmall)
                    if (outfit.value.description.isNotBlank()) Text(outfit.value.description)
                }
                items(outfit.value.items, key = { it.id }) { clothing ->
                    ClosetlyCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onItemClick?.let { openItem -> { openItem(clothing.id) } },
                    ) {
                        Text(clothing.name, style = MaterialTheme.typography.titleMedium)
                        Text(listOfNotNull(clothing.color, clothing.size).joinToString(" · "))
                    }
                }
                item {
                    uiState.deleteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                        ClosetlyButton("Edit outfit", onEdit, enabled = !uiState.isDeleting && !uiState.deleted)
                        ClosetlyTextButton("Hapus outfit", onDeleteClick, enabled = !uiState.isDeleting && !uiState.deleted)
                    }
                }
            }
        }
    }
    if (uiState.showDeleteConfirmation) {
        ConfirmDialog(
            title = "Hapus outfit?",
            message = "Outfit ini akan dihapus dari daftar.",
            confirmLabel = "Hapus",
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

@Composable
fun OutfitFormScreen(
    uiState: OutfitFormUiState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNameChange: (String) -> Unit,
    onNameFocusLost: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (ClothingCategory?) -> Unit,
    onToggleItem: (ClothingOption) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onRemoveItem: (ClothingOption) -> Unit,
    onOpenWardrobe: () -> Unit,
    onSave: () -> Unit,
) {
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }
    ClosetlyScaffold(
        title = if (uiState.isEditing) "Edit outfit" else "Buat outfit",
        onBack = onBack,
    ) { padding ->
        val modifier = Modifier.padding(padding)
        if (uiState.isLoading) {
            LoadingState(modifier)
            return@ClosetlyScaffold
        }
        uiState.loadError?.let {
            ErrorState(it, modifier)
            return@ClosetlyScaffold
        }

        val canEdit = !uiState.isSaving && !uiState.saved
        val selectedIds = uiState.selectedItems.map { it.id }.toSet()
        OutfitList(modifier) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                    var hadFocus by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text("Nama outfit") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                            if (hadFocus && !focus.isFocused) onNameFocusLost()
                            hadFocus = focus.isFocused
                        },
                        singleLine = true,
                        enabled = canEdit,
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canEdit,
                    )
                    ClosetlyButton(
                        text = "Simpan outfit",
                        onClick = onSave,
                        enabled = uiState.canSave,
                        isLoading = uiState.isSaving,
                    )
                    uiState.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
            if (uiState.selectedItems.isNotEmpty()) {
                item { Text("Urutan item terpilih", style = MaterialTheme.typography.titleMedium) }
                items(uiState.selectedItems.size, key = { "selected-${uiState.selectedItems[it].id}" }) { index ->
                    val selected = uiState.selectedItems[index]
                    ClosetlyCard(Modifier.fillMaxWidth()) {
                        Text("${index + 1}. ${selected.name}")
                        Row {
                            ClosetlyTextButton("Naik", { onMoveItem(index, index - 1) }, enabled = canEdit && index > 0)
                            ClosetlyTextButton(
                                "Turun",
                                { onMoveItem(index, index + 1) },
                                enabled = canEdit && index < uiState.selectedItems.lastIndex,
                            )
                            ClosetlyTextButton("Lepas", { onRemoveItem(selected) }, enabled = canEdit)
                        }
                    }
                }
            }
            item {
                Text("Pilih pakaian", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                    item {
                        FilterChip(
                            selected = uiState.activeCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text("Semua") },
                            enabled = canEdit,
                        )
                    }
                    items(ClothingCategory.entries) { category ->
                        FilterChip(
                            selected = uiState.activeCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.label()) },
                            enabled = canEdit,
                        )
                    }
                }
            }
            if (uiState.availableItems.isEmpty()) {
                item {
                    Text("Belum ada item aktif di lemari.")
                    ClosetlyTextButton("Buka Lemari", onOpenWardrobe, enabled = canEdit)
                }
            } else if (uiState.visibleItems.isEmpty()) {
                item { Text("Tidak ada pakaian di kategori ini.") }
            }
            items(uiState.visibleItems, key = { "available-${it.id}" }) { clothing ->
                val isSelected = clothing.id in selectedIds
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .toggleable(isSelected, canEdit, Role.Checkbox) { onToggleItem(clothing) }
                        .padding(vertical = ClosetlySpacing.Sm),
                    horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = isSelected, onCheckedChange = null, enabled = canEdit)
                    Text(clothing.name)
                }
            }
        }
    }
}

@Composable
private fun OutfitList(
    modifier: Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(ClosetlySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        content = content,
    )
}

private fun ClothingCategory.label(): String = when (this) {
    ClothingCategory.Top -> "Atasan"
    ClothingCategory.Bottom -> "Bawahan"
    ClothingCategory.Shoes -> "Sepatu"
    ClothingCategory.Accessory -> "Aksesori"
}
