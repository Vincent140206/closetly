package vin.pam.closetly.feature.outfit

import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class Outfit(
    override val id: String,
    val name: String,
    val description: String,
    val clothingIds: List<String>,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val title: String get() = name
    override val subtitle: String get() = description
}

@Serializable
data class OutfitItem(val outfitId: String, val clothingItemId: String, val position: Int)

interface OutfitRepository : CrudRepository<Outfit>
typealias OutfitUiState = FeatureUiState<Outfit>

class FakeOutfitRepository : InMemoryCrudRepository<Outfit>(
    listOf(
        Outfit("outfit-campus", "Easy Campus Day", "Relaxed neutrals for lectures and coffee.", listOf("clothing-tee", "clothing-jeans", "clothing-shoes")),
        Outfit("outfit-weekend", "Sage Weekend", "Layered casual look for cooler afternoons.", listOf("clothing-tee", "clothing-jeans", "clothing-jacket")),
    ),
), OutfitRepository

class OutfitViewModel(repository: OutfitRepository) : FeatureViewModel<Outfit>(repository)
