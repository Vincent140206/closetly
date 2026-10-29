package vin.pam.closetly.feature.planner

import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class OutfitPlan(
    override val id: String,
    val outfitId: String,
    val date: String,
    val activity: String,
    val notes: String,
    val status: String,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val title: String get() = activity
    override val subtitle: String get() = "$date • $status"
}

interface PlannerRepository : CrudRepository<OutfitPlan>
typealias PlannerUiState = FeatureUiState<OutfitPlan>

class FakePlannerRepository : InMemoryCrudRepository<OutfitPlan>(
    listOf(
        OutfitPlan("plan-presentation", "outfit-campus", "2026-11-02", "Group presentation", "Bring blazer", "Planned"),
        OutfitPlan("plan-brunch", "outfit-weekend", "2026-11-08", "Sunday brunch", "Outdoor seating", "Planned"),
    ),
), PlannerRepository

class PlannerViewModel(repository: PlannerRepository) : FeatureViewModel<OutfitPlan>(repository)
