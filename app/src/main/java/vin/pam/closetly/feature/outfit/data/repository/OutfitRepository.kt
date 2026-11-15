package vin.pam.closetly.feature.outfit.data.repository

import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit

data class OutfitDraft(
    val name: String,
    val description: String,
    val clothingItemIds: List<String>,
)

/** Outfit masih dipakai Planner/Wear History sehingga tidak boleh dihapus (FK `on delete restrict`). */
class OutfitInUseException(
    message: String = "Outfit ini masih dipakai di Planner atau Wear History, jadi belum bisa dihapus.",
) : Exception(message)

interface OutfitRepository {
    suspend fun getOutfits(): List<Outfit>

    suspend fun getOutfit(outfitId: String): Outfit

    /** Hanya item aktif (tidak diarsipkan) dari Digital Wardrobe milik user. */
    suspend fun getSelectableClothing(): List<ClothingOption>

    suspend fun createOutfit(draft: OutfitDraft): Outfit

    suspend fun updateOutfit(outfitId: String, draft: OutfitDraft): Outfit

    /** Melempar [OutfitInUseException] bila outfit direferensikan Planner/Wear History. */
    suspend fun deleteOutfit(outfitId: String)
}
