package vin.pam.closetly.feature.inspiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun InspirationScreen(viewModel: InspirationViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Style Inspiration", "Save your first style reference.", true, state, onItemClick, onAddClick, onBack)
}

@Composable
fun InspirationDetailScreen(viewModel: InspirationViewModel, inspirationId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == inspirationId }
    FeatureDetailScreen("Inspiration details", item, item?.let { listOf("Collection" to (it.collectionId ?: "Unsorted")) }.orEmpty(), onEditClick, { viewModel.delete(inspirationId, onBack) }, onBack)
}

@Composable
fun InspirationFormScreen(viewModel: InspirationViewModel, inspirationId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = inspirationId?.let(viewModel::item)
    FeatureFormScreen(
        if (existing == null) "Add inspiration" else "Edit inspiration",
        existing?.title.orEmpty(), existing?.notes.orEmpty(), existing?.imageUrl, true, state.isSubmitting,
        onSave = { title, notes, image ->
            viewModel.save(StyleInspiration(existing?.id ?: "inspiration-${System.currentTimeMillis()}", title, notes, existing?.collectionId ?: "collection-everyday", image), onSave)
        },
        onBack = onBack,
    )
}
