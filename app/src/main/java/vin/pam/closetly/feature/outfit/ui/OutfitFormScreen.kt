package vin.pam.closetly.feature.outfit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.ui.components.ClothingThumb
import vin.pam.closetly.feature.outfit.ui.components.OutfitCollage
import vin.pam.closetly.feature.outfit.ui.components.OutfitScaffold
import vin.pam.closetly.feature.outfit.ui.components.ReorderableItemList
import vin.pam.closetly.feature.outfit.ui.components.dashedBorder
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlyRadius
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

/** Form Tambah/Ubah Outfit. Bottom bar global disembunyikan oleh AppNavHost pada route ini. */
@Composable
fun OutfitFormScreen(
    uiState: OutfitFormUiState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNameChange: (String) -> Unit,
    onNameFocusLost: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (ClothingCategory) -> Unit,
    onToggleItem: (ClothingOption) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onOpenWardrobe: () -> Unit,
    onSave: () -> Unit,
) {
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    OutfitScaffold(
        title = if (uiState.isEditing) "Ubah Outfit" else "Buat Outfit",
        onBack = onBack,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                LoadingState(Modifier.weight(1f))
            } else {
                FormContent(
                    uiState = uiState,
                    onNameChange = onNameChange,
                    onNameFocusLost = onNameFocusLost,
                    onDescriptionChange = onDescriptionChange,
                    onCategorySelected = onCategorySelected,
                    onToggleItem = onToggleItem,
                    onMoveItem = onMoveItem,
                    onRemoveItem = onRemoveItem,
                    onOpenWardrobe = onOpenWardrobe,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(Modifier.padding(ClosetlyDimensions.ScreenHorizontalPadding)) {
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = ClosetlySpacing.Sm),
                    )
                }
                ClosetlyButton(
                    text = "Simpan Outfit  ✓",
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canSave,
                    isLoading = uiState.isSaving,
                )
            }
        }
    }
}

@Composable
private fun FormContent(
    uiState: OutfitFormUiState,
    onNameChange: (String) -> Unit,
    onNameFocusLost: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (ClothingCategory) -> Unit,
    onToggleItem: (ClothingOption) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onOpenWardrobe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleItems = uiState.visibleItems
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(ClosetlyDimensions.ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        fullSpan { OutfitPreviewCard(uiState.selectedItems) }

        fullSpan {
            var hadFocus by remember { mutableStateOf(false) }
            LabeledField(
                label = "Nama Outfit",
                value = uiState.name,
                onValueChange = onNameChange,
                placeholder = "Contoh: Casual Weekend, Office Look",
                error = uiState.nameError,
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) hadFocus = true else if (hadFocus) onNameFocusLost()
                },
            )
        }

        fullSpan {
            LabeledField(
                label = "Catatan Styling (Opsional)",
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholder = "Tambahkan tips padu padan...",
                minLines = 3,
            )
        }

        if (uiState.selectedItems.isNotEmpty()) {
            fullSpan {
                Column(Modifier.padding(top = ClosetlySpacing.Sm)) {
                    Text(
                        text = "Item Terpilih (${uiState.selectedItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (uiState.selectedItems.size > 1) {
                        Text(
                            text = "Geser ikon ≡ untuk mengatur urutan pakaian.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            fullSpan {
                ReorderableItemList(
                    items = uiState.selectedItems,
                    onMove = onMoveItem,
                    onRemove = onRemoveItem,
                )
            }
        }

        fullSpan {
            Column(Modifier.padding(top = ClosetlySpacing.Sm)) {
                Text(text = "Pilih Pakaian", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Ambil item aktif dari koleksi lemari digitalmu. " +
                        "Boleh lebih dari satu per kategori.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        fullSpan {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
            ) {
                ClothingCategory.entries.forEach { category ->
                    CategoryChip(
                        label = category.label,
                        selected = category == uiState.activeCategory,
                        onClick = { onCategorySelected(category) },
                    )
                }
            }
        }

        if (visibleItems.isEmpty()) {
            fullSpan { EmptyCategoryCard(onOpenWardrobe) }
        } else {
            items(visibleItems, key = { it.id }) { item ->
                ClothingPickCard(
                    item = item,
                    selected = uiState.selectedItems.any { it.id == item.id },
                    onToggle = { onToggleItem(item) },
                )
            }
        }
    }
}

private fun LazyGridScope.fullSpan(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun OutfitPreviewCard(selectedItems: List<ClothingOption>) {
    val colors = MaterialTheme.colorScheme
    if (selectedItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .dashedBorder(colors.outlineVariant, ClosetlyRadius.Xl),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_closetly_outfit),
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(ClosetlySpacing.Md))
                Text(
                    text = "Mulai pilih pakaian untuk outfit-mu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(MaterialTheme.shapes.large)
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.large)
                .padding(ClosetlySpacing.Md),
        ) {
            OutfitCollage(selectedItems, Modifier.fillMaxSize())
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(ClosetlySpacing.Sm),
                shape = ClosetlyFullShape,
                color = colors.surfaceVariant,
            ) {
                Text(
                    text = "PRATINJAU · ${selectedItems.size} ITEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onBackground,
                    modifier = Modifier.padding(
                        horizontal = ClosetlySpacing.Md,
                        vertical = ClosetlySpacing.Xs,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    error: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(ClosetlySpacing.Sm))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            },
            minLines = minLines,
            isError = error != null,
            supportingText = error?.let { message ->
                { Text(text = message, style = MaterialTheme.typography.bodySmall) }
            },
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outlineVariant,
            ),
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Surface(onClick) otomatis menjaga touch target minimal 48dp.
    Surface(
        selected = selected,
        onClick = onClick,
        shape = ClosetlyFullShape,
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = ClosetlySpacing.Lg,
                vertical = ClosetlySpacing.Sm,
            ),
        )
    }
}

@Composable
private fun ClothingPickCard(item: ClothingOption, selected: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onToggle,
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.primary else colors.outlineVariant,
        ),
    ) {
        Column(Modifier.padding(ClosetlySpacing.Sm)) {
            Box {
                ClothingThumb(item, Modifier.fillMaxWidth().height(120.dp))
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(ClosetlySpacing.Sm)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Dipilih",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(ClosetlySpacing.Sm))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(item.color, item.size).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EmptyCategoryCard(onOpenWardrobe: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ClosetlySpacing.Xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Belum ada pakaian aktif di kategori ini.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(ClosetlySpacing.Md))
            // Tidak ada upload di Mix & Match: pakaian baru ditambahkan lewat Digital Wardrobe.
            ClosetlyButton(
                text = "Buka Lemari",
                onClick = onOpenWardrobe,
                style = ClosetlyButtonStyle.Dark,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitFormEmptyPreview() = ClosetlyTheme {
    OutfitFormScreen(
        uiState = OutfitFormUiState(
            availableItems = FakeOutfitRepository.SampleWardrobe.filterNot { it.isArchived },
        ),
        onBack = {}, onSaved = {}, onNameChange = {}, onNameFocusLost = {},
        onDescriptionChange = {}, onCategorySelected = {}, onToggleItem = {},
        onMoveItem = { _, _ -> }, onRemoveItem = {}, onOpenWardrobe = {}, onSave = {},
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitFormFilledPreview() = ClosetlyTheme {
    val outfit = FakeOutfitRepository.SampleOutfits.first()
    OutfitFormScreen(
        uiState = OutfitFormUiState(
            isEditing = true,
            name = outfit.name,
            description = outfit.description,
            selectedItems = outfit.items,
            availableItems = FakeOutfitRepository.SampleWardrobe.filterNot { it.isArchived },
        ),
        onBack = {}, onSaved = {}, onNameChange = {}, onNameFocusLost = {},
        onDescriptionChange = {}, onCategorySelected = {}, onToggleItem = {},
        onMoveItem = { _, _ -> }, onRemoveItem = {}, onOpenWardrobe = {}, onSave = {},
    )
}
