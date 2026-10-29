package vin.pam.closetly.feature.wearhistory

import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class WearHistoryEntry(
    override val id: String,
    val outfitId: String,
    val outfitName: String,
    val dateWorn: String,
    val notes: String,
    val sourcePlanId: String? = null,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val title: String get() = outfitName
    override val subtitle: String get() = "$dateWorn • $notes"
}

interface WearHistoryRepository : CrudRepository<WearHistoryEntry> {
    suspend fun saveFromCompletedPlan(entry: WearHistoryEntry)
}
typealias WearHistoryUiState = FeatureUiState<WearHistoryEntry>

class FakeWearHistoryRepository : InMemoryCrudRepository<WearHistoryEntry>(
    listOf(
        WearHistoryEntry("wear-001", "outfit-campus", "Easy Campus Day", "2026-10-20", "All-day comfortable"),
        WearHistoryEntry("wear-002", "outfit-weekend", "Sage Weekend", "2026-10-25", "Coffee with friends"),
    ),
), WearHistoryRepository {
    override suspend fun saveFromCompletedPlan(entry: WearHistoryEntry) {
        val duplicate = entry.sourcePlanId != null && observeAll().first().any { it.sourcePlanId == entry.sourcePlanId }
        if (!duplicate) save(entry)
    }
}

class WearHistoryViewModel(repository: WearHistoryRepository) : FeatureViewModel<WearHistoryEntry>(repository)
