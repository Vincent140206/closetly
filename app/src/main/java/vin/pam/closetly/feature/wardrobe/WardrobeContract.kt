package vin.pam.closetly.feature.wardrobe

import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class ClothingItem(
    override val id: String,
    val name: String,
    val color: String,
    val size: String,
    val category: String,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val title: String get() = name
    override val subtitle: String get() = "$category • $color • $size"
}

interface WardrobeRepository : CrudRepository<ClothingItem>
typealias WardrobeUiState = FeatureUiState<ClothingItem>

class FakeWardrobeRepository : InMemoryCrudRepository<ClothingItem>(
    listOf(
        ClothingItem("clothing-tee", "Ivory Essential Tee", "Ivory", "M", "Top"),
        ClothingItem("clothing-jeans", "Straight Blue Jeans", "Indigo", "30", "Bottom"),
        ClothingItem("clothing-shoes", "Classic White Sneakers", "White", "41", "Shoes"),
        ClothingItem("clothing-jacket", "Lightweight Overshirt", "Sage", "M", "Outerwear"),
    ),
), WardrobeRepository

class WardrobeViewModel(repository: WardrobeRepository) : FeatureViewModel<ClothingItem>(repository)
