package vin.pam.closetly.feature.outfit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.ErrorState
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit
import vin.pam.closetly.feature.outfit.ui.components.ClothingThumb
import vin.pam.closetly.feature.outfit.ui.components.OutfitCollage
import vin.pam.closetly.feature.outfit.ui.components.OutfitScaffold
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

/** Detail Outfit + dialog konfirmasi hapus. Bottom bar global disembunyikan pada route ini. */
@Composable
fun OutfitDetailScreen(
    uiState: OutfitDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDeleted: () -> Unit,
    onItemClick: (ClothingOption) -> Unit,
    onRetry: () -> Unit,
) {
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    val outfit = uiState.outfit
    var menuExpanded by remember { mutableStateOf(false) }

    OutfitScaffold(
        title = "Detail Outfit",
        onBack = onBack,
        actions = {
            if (outfit != null) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu lainnya")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Outfit") },
                            onClick = { menuExpanded = false; onEdit() },
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus Outfit", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDeleteClick() },
                        )
                    }
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(padding))
            outfit == null -> ErrorState(
                message = uiState.errorMessage ?: "Outfit tidak ditemukan.",
                modifier = Modifier.padding(padding),
                onRetry = onRetry,
            )
            else -> DetailContent(
                outfit = outfit,
                errorMessage = uiState.errorMessage,
                onEdit = onEdit,
                onDeleteClick = onDeleteClick,
                onItemClick = onItemClick,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteOutfitDialog(
            isDeleting = uiState.isDeleting,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

@Composable
private fun DetailContent(
    outfit: Outfit,
    errorMessage: String?,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: (ClothingOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ClosetlyDimensions.ScreenHorizontalPadding),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
        ) {
            Box(Modifier.fillMaxWidth().height(280.dp).padding(ClosetlySpacing.Md)) {
                OutfitCollage(outfit.items, Modifier.fillMaxSize())
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(ClosetlySpacing.Sm),
                    shape = ClosetlyFullShape,
                    color = colors.surfaceVariant,
                ) {
                    Text(
                        text = "LOOK BOOK ${outfit.createdAt.year}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = ClosetlySpacing.Md,
                            vertical = ClosetlySpacing.Xs,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(ClosetlySpacing.Xl))
        Text(text = outfit.name, style = MaterialTheme.typography.titleLarge)
        if (outfit.description.isNotBlank()) {
            Spacer(Modifier.height(ClosetlySpacing.Xs))
            Text(
                text = outfit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(ClosetlySpacing.Xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Item yang Digunakan", style = MaterialTheme.typography.titleMedium)
            Surface(shape = ClosetlyFullShape, color = colors.surfaceVariant) {
                Text(
                    text = "${outfit.items.size} Item",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = ClosetlySpacing.Md,
                        vertical = ClosetlySpacing.Xs,
                    ),
                )
            }
        }
        Spacer(Modifier.height(ClosetlySpacing.Md))
        Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
            outfit.items.forEachIndexed { index, item ->
                UsedItemRow(item = item, toneIndex = index, onClick = { onItemClick(item) })
            }
        }

        errorMessage?.let { message ->
            Spacer(Modifier.height(ClosetlySpacing.Md))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = colors.error)
        }

        Spacer(Modifier.height(ClosetlySpacing.Xl))
        ClosetlyButton(
            text = "Edit Outfit",
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Filled.Edit,
        )
        Spacer(Modifier.height(ClosetlySpacing.Sm))
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = ClosetlyDimensions.MinimumTouchTarget),
            shape = ClosetlyFullShape,
            border = BorderStroke(1.dp, colors.outlineVariant),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.surface,
                contentColor = colors.error,
            ),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(ClosetlySpacing.Sm))
            Text(text = "Hapus Outfit", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(ClosetlySpacing.Lg))
    }
}

@Composable
private fun UsedItemRow(item: ClothingOption, toneIndex: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ClosetlySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClothingThumb(item, Modifier.size(48.dp), toneIndex)
            Column(Modifier.weight(1f).padding(horizontal = ClosetlySpacing.Md)) {
                Text(
                    text = item.category.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeleteOutfitDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Dialog(onDismissRequest = { if (!isDeleting) onDismiss() }) {
        Surface(shape = MaterialTheme.shapes.large, color = colors.surface) {
            Column(
                modifier = Modifier.padding(ClosetlySpacing.Xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error)
                }
                Spacer(Modifier.height(ClosetlySpacing.Lg))
                Text(text = "Hapus Outfit?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(ClosetlySpacing.Sm))
                Text(
                    text = "Outfit ini akan dihapus dari koleksimu.\nTindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(ClosetlySpacing.Xl))
                ClosetlyButton(
                    text = "Hapus",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isDeleting,
                )
                Spacer(Modifier.height(ClosetlySpacing.Sm))
                ClosetlyButton(
                    text = "Batal",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    style = ClosetlyButtonStyle.Secondary,
                    enabled = !isDeleting,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitDetailPreview() = ClosetlyTheme {
    OutfitDetailScreen(
        uiState = OutfitDetailUiState(isLoading = false, outfit = FakeOutfitRepository.SampleOutfits[1]),
        onBack = {}, onEdit = {}, onDeleteClick = {}, onDismissDelete = {},
        onConfirmDelete = {}, onDeleted = {}, onItemClick = {}, onRetry = {},
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitDeleteDialogPreview() = ClosetlyTheme {
    OutfitDetailScreen(
        uiState = OutfitDetailUiState(
            isLoading = false,
            outfit = FakeOutfitRepository.SampleOutfits[1],
            showDeleteDialog = true,
        ),
        onBack = {}, onEdit = {}, onDeleteClick = {}, onDismissDelete = {},
        onConfirmDelete = {}, onDeleted = {}, onItemClick = {}, onRetry = {},
    )
}
