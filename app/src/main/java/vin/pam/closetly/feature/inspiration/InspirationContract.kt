package vin.pam.closetly.feature.inspiration

import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class StyleInspiration(
    override val id: String,
    override val title: String,
    val notes: String,
    val collectionId: String?,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val subtitle: String get() = notes
}

@Serializable
data class InspirationCollection(val id: String, val name: String)

interface InspirationRepository : CrudRepository<StyleInspiration>
typealias InspirationUiState = FeatureUiState<StyleInspiration>

class FakeInspirationRepository : InMemoryCrudRepository<StyleInspiration>(
    listOf(
        StyleInspiration("inspiration-linen", "Soft Linen Layers", "Warm neutrals and breathable fabrics.", "collection-everyday"),
        StyleInspiration("inspiration-monochrome", "Monochrome Workwear", "Clean tailoring with one accent texture.", "collection-campus"),
        StyleInspiration("inspiration-color", "Quiet Color", "Muted green paired with cream and denim.", "collection-everyday"),
    ),
), InspirationRepository

class InspirationViewModel(repository: InspirationRepository) : FeatureViewModel<StyleInspiration>(repository)
