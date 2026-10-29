package vin.pam.closetly.feature.outfit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun OutfitCollectionScreen(viewModel: OutfitViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Mix & Match", "Create your first outfit combination.", true, state, onItemClick, onAddClick, onBack)
}

@Composable
fun OutfitDetailScreen(viewModel: OutfitViewModel, outfitId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == outfitId }
    FeatureDetailScreen("Outfit details", item, item?.let { listOf("Clothing items" to it.clothingIds.size.toString()) }.orEmpty(), onEditClick, { viewModel.delete(outfitId, onBack) }, onBack)
}

@Composable
fun OutfitFormScreen(viewModel: OutfitViewModel, outfitId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = outfitId?.let(viewModel::item)
    FeatureFormScreen(
        if (existing == null) "Create outfit" else "Edit outfit",
        existing?.name.orEmpty(), existing?.description.orEmpty(), existing?.imageUrl, false, state.isSubmitting,
        onSave = { name, description, image ->
            viewModel.save(Outfit(existing?.id ?: "outfit-${System.currentTimeMillis()}", name, description, existing?.clothingIds ?: listOf("clothing-tee", "clothing-jeans"), image), onSave)
        },
        onBack = onBack,
    )
}
