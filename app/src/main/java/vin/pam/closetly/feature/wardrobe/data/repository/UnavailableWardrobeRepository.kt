package vin.pam.closetly.feature.wardrobe.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem

class UnavailableWardrobeRepository(private val message: String) : WardrobeRepository {
    override val items = MutableStateFlow<List<ClothingItem>>(emptyList()).asStateFlow()
    override suspend fun refresh(): Unit = error(message)
    override suspend fun getItem(id: String): ClothingItem = error(message)
    override suspend fun createItem(requestId: String, draft: ClothingDraft): ClothingItem = error(message)
    override suspend fun updateItem(id: String, draft: ClothingDraft): ClothingItem = error(message)
    override suspend fun setFavorite(id: String, favorite: Boolean): Unit = error(message)
    override suspend fun archiveItem(id: String): Unit = error(message)
    override suspend fun restoreItem(id: String): Unit = error(message)
}
