package vin.pam.closetly.feature.planner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vin.pam.closetly.core.ui.components.FeatureDetailScreen
import vin.pam.closetly.core.ui.components.FeatureFormScreen
import vin.pam.closetly.core.ui.components.FeatureListScreen

@Composable
fun PlannerScreen(viewModel: PlannerViewModel, onItemClick: (String) -> Unit, onAddClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureListScreen("Outfit Planner", "No outfits are scheduled yet.", false, state, onItemClick, onAddClick, onBack)
}

@Composable
fun PlanDetailScreen(viewModel: PlannerViewModel, planId: String, onEditClick: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.items.firstOrNull { it.id == planId }
    FeatureDetailScreen("Plan details", item, item?.let { listOf("Date" to it.date, "Status" to it.status, "Outfit ID" to it.outfitId, "Notes" to it.notes) }.orEmpty(), onEditClick, { viewModel.delete(planId, onBack) }, onBack)
}

@Composable
fun PlanFormScreen(viewModel: PlannerViewModel, planId: String?, onSave: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val existing = planId?.let(viewModel::item)
    FeatureFormScreen(
        if (existing == null) "Schedule outfit" else "Edit plan",
        existing?.activity.orEmpty(), existing?.notes.orEmpty(), null, false, state.isSubmitting,
        onSave = { activity, notes, _ ->
            viewModel.save(OutfitPlan(existing?.id ?: "plan-${System.currentTimeMillis()}", existing?.outfitId ?: "outfit-campus", existing?.date ?: "2026-11-15", activity, notes, existing?.status ?: "Planned"), onSave)
        },
        onBack = onBack,
    )
}
