package vin.pam.closetly.feature.wearhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun WearHistoryScreen(viewModel: WearHistoryViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Wear History", "Your outfit archive will appear here.", true, state, onItemClick, onAddClick, onBack)
}

@Composable
fun WearHistoryDetailScreen(viewModel: WearHistoryViewModel, historyId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == historyId }
    FeatureDetailScreen("History details", item, item?.let { listOf("Date worn" to it.dateWorn, "Outfit ID" to it.outfitId, "Source plan" to (it.sourcePlanId ?: "Manual entry")) }.orEmpty(), onEditClick, { viewModel.delete(historyId, onBack) }, onBack)
}

@Composable
fun WearHistoryFormScreen(viewModel: WearHistoryViewModel, historyId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = historyId?.let(viewModel::item)
    FeatureFormScreen(
        if (existing == null) "Record outfit" else "Edit history",
        existing?.outfitName.orEmpty(), existing?.notes.orEmpty(), existing?.imageUrl, false, state.isSubmitting,
        onSave = { outfitName, notes, image ->
            viewModel.save(WearHistoryEntry(existing?.id ?: "wear-${System.currentTimeMillis()}", existing?.outfitId ?: "outfit-campus", outfitName, existing?.dateWorn ?: "2026-10-29", notes, existing?.sourcePlanId, image), onSave)
        },
        onBack = onBack,
    )
}
