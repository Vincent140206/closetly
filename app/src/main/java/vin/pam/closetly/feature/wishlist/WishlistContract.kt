package vin.pam.closetly.feature.wishlist

import kotlinx.serialization.Serializable
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.data.InMemoryCrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity
import vin.pam.closetly.core.ui.FeatureViewModel
import vin.pam.closetly.core.ui.FeatureUiState

@Serializable
data class WishlistItem(
    override val id: String,
    val name: String,
    val category: String,
    val estimatedPrice: Long,
    val store: String,
    val productUrl: String,
    val priority: String,
    val purchased: Boolean,
    override val imageUrl: String? = null,
) : ClosetlyEntity {
    override val title: String get() = name
    override val subtitle: String get() = "$category • Rp$estimatedPrice • $priority"
}

interface WishlistRepository : CrudRepository<WishlistItem>
typealias WishlistUiState = FeatureUiState<WishlistItem>

class FakeWishlistRepository : InMemoryCrudRepository<WishlistItem>(
    listOf(
        WishlistItem("wish-loafers", "Leather Penny Loafers", "Shoes", 899000, "Local Atelier", "https://example.com/loafers", "High", false),
        WishlistItem("wish-tote", "Structured Canvas Tote", "Bag", 425000, "Studio Market", "https://example.com/tote", "Medium", false),
    ),
), WishlistRepository

class WishlistViewModel(repository: WishlistRepository) : FeatureViewModel<WishlistItem>(repository)
