package vin.pam.closetly.feature.wardrobe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Digital Wardrobe", "Your wardrobe is ready for its first item.", true, state, onItemClick, onAddClick, onBack)
}

@Composable
fun ClothingDetailScreen(viewModel: WardrobeViewModel, clothingId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == clothingId }
    FeatureDetailScreen(
        title = "Clothing details",
        item = item,
        details = item?.let { listOf("Category" to it.category, "Color" to it.color, "Size" to it.size) }.orEmpty(),
        onEditClick = onEditClick,
        onDelete = { viewModel.delete(clothingId, onBack) },
        onBack = onBack,
    )
}

@Composable
fun ClothingFormScreen(viewModel: WardrobeViewModel, clothingId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = clothingId?.let(viewModel::item)
    FeatureFormScreen(
        title = if (existing == null) "Add clothing" else "Edit clothing",
        initialTitle = existing?.name.orEmpty(),
        initialDescription = existing?.let { "${it.category} • ${it.color} • ${it.size}" }.orEmpty(),
        initialImageUrl = existing?.imageUrl,
        supportsImage = true,
        isSubmitting = state.isSubmitting,
        onSave = { name, _, image ->
            viewModel.save(
                ClothingItem(existing?.id ?: "clothing-${System.currentTimeMillis()}", name, existing?.color ?: "Neutral", existing?.size ?: "M", existing?.category ?: "Uncategorized", image),
                onSave,
            )
        },
        onBack = onBack,
    )
}
