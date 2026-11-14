package vin.pam.closetly.feature.wardrobe.data.repository

import kotlinx.coroutines.flow.StateFlow
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem

interface WardrobeRepository {
    val items: StateFlow<List<ClothingItem>>
    suspend fun refresh()
    suspend fun getItem(id: String): ClothingItem
    /** A stable request ID makes a retried create idempotent. */
    suspend fun createItem(requestId: String, draft: ClothingDraft): ClothingItem
    suspend fun updateItem(id: String, draft: ClothingDraft): ClothingItem
    suspend fun setFavorite(id: String, favorite: Boolean)
    /** Retain the row and its image so existing outfits keep their references. */
    suspend fun archiveItem(id: String)
    suspend fun restoreItem(id: String)
}
