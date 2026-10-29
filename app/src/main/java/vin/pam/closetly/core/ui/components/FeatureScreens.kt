package vin.pam.closetly.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureUiState

@Composable
fun <T : ClosetlyEntity> FeatureListScreen(
    title: String,
    emptyMessage: String,
    useGrid: Boolean,
    state: FeatureUiState<T>,
    onItemClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit,
) {
    ClosetlyScaffold(title = title, onBack = onBack, floatingActionLabel = "Add", onFloatingAction = onAddClick) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.items.isEmpty() -> ErrorState(state.errorMessage, Modifier.padding(padding))
            state.items.isEmpty() -> EmptyState(emptyMessage, Modifier.padding(padding))
            else -> FeatureListContent(state.items, useGrid, onItemClick, Modifier.padding(padding))
        }
    }
}

@Composable
fun FeatureDetailScreen(
    title: String,
    item: ClosetlyEntity?,
    details: List<Pair<String, String>>,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    ClosetlyScaffold(title = title, onBack = onBack) { padding ->
        if (item == null) {
            ErrorState("Item not found", Modifier.padding(padding))
        } else {
            FeatureDetailContent(item, details, onEditClick, onDelete, Modifier.padding(padding))
        }
    }
}

@Composable
fun FeatureFormScreen(
    title: String,
    initialTitle: String,
    initialDescription: String,
    initialImageUrl: String?,
    supportsImage: Boolean,
    isSubmitting: Boolean,
    onSave: (String, String, String?) -> Unit,
    onBack: () -> Unit,
) {
    ClosetlyScaffold(title = title, onBack = onBack) { padding ->
        FeatureFormContent(
            initialTitle = initialTitle,
            initialDescription = initialDescription,
            initialImageUrl = initialImageUrl,
            supportsImage = supportsImage,
            isSubmitting = isSubmitting,
            onSave = onSave,
            modifier = Modifier.padding(padding),
        )
    }
}
