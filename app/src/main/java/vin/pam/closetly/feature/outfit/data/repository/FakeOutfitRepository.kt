package vin.pam.closetly.feature.outfit.data.repository

import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit
import java.time.LocalDate
import java.util.UUID

/**
 * Repository in-memory untuk pengembangan UI dan unit test.
 * Ganti dengan SupabaseOutfitRepository (tabel `outfits` + `outfit_items`)
 * setelah schema aktif diverifikasi; cukup ubah satu baris di OutfitGraph.
 */
class FakeOutfitRepository(
    private val wardrobe: List<ClothingOption> = SampleWardrobe,
    initialOutfits: List<Outfit> = SampleOutfits,
) : OutfitRepository {
    private var outfits: List<Outfit> = initialOutfits

    override suspend fun getOutfits(): List<Outfit> = outfits

    override suspend fun getOutfit(outfitId: String): Outfit =
        outfits.firstOrNull { it.id == outfitId }
            ?: throw NoSuchElementException("Outfit tidak ditemukan.")

    override suspend fun getSelectableClothing(): List<ClothingOption> =
        wardrobe.filterNot { it.isArchived }

    override suspend fun createOutfit(draft: OutfitDraft): Outfit {
        val outfit = draft.toOutfit(id = UUID.randomUUID().toString(), createdAt = LocalDate.now())
        outfits = listOf(outfit) + outfits
        return outfit
    }

    override suspend fun updateOutfit(outfitId: String, draft: OutfitDraft): Outfit {
        val existing = getOutfit(outfitId)
        val updated = draft.toOutfit(id = outfitId, createdAt = existing.createdAt)
        outfits = outfits.map { if (it.id == outfitId) updated else it }
        return updated
    }

    override suspend fun deleteOutfit(outfitId: String) {
        getOutfit(outfitId)
        outfits = outfits.filterNot { it.id == outfitId }
    }

    private fun OutfitDraft.toOutfit(id: String, createdAt: LocalDate): Outfit {
        require(name.isNotBlank()) { "Nama outfit wajib diisi." }
        require(clothingItemIds.isNotEmpty()) { "Pilih minimal satu pakaian." }
        val items = clothingItemIds.map { itemId ->
            wardrobe.firstOrNull { it.id == itemId }
                ?: throw IllegalArgumentException("Pakaian tidak ditemukan di lemari.")
        }
        // Item arsip tidak boleh baru dipilih; pada edit, item lama yang kini
        // diarsipkan dibiarkan agar outfit lama tidak rusak.
        val previous = outfits.firstOrNull { it.id == id }?.items?.map { it.id }.orEmpty()
        require(items.none { it.isArchived && it.id !in previous }) {
            "Pakaian yang diarsipkan tidak dapat dipilih."
        }
        return Outfit(
            id = id,
            name = name.trim(),
            description = description.trim(),
            items = items,
            createdAt = createdAt,
        )
    }

    companion object {
        val SampleWardrobe = listOf(
            ClothingOption("w1", "White Oversized Shirt", ClothingCategory.Top, "Putih", "L", null),
            ClothingOption("w2", "Cropped Tee", ClothingCategory.Top, "Hitam", "M", null),
            ClothingOption("w3", "Linen Blouse", ClothingCategory.Top, "Beige", "M", null),
            ClothingOption("w4", "Striped Shirt", ClothingCategory.Top, "Biru", "L", null),
            ClothingOption("w5", "Dark Blue Jeans", ClothingCategory.Bottom, "Biru Tua", "30", null),
            ClothingOption("w6", "Pleated Skirt", ClothingCategory.Bottom, "Terakota", "M", null),
            ClothingOption("w7", "Chino Pants", ClothingCategory.Bottom, "Krem", "30", null, isArchived = true),
            ClothingOption("w8", "White Sneakers", ClothingCategory.Shoes, "Putih", "40", null),
            ClothingOption("w9", "Loafers", ClothingCategory.Shoes, "Cokelat", "40", null),
            ClothingOption("w10", "Gold Necklace", ClothingCategory.Accessory, "Emas", null, null),
            ClothingOption("w11", "Leather Belt", ClothingCategory.Accessory, "Cokelat", "M", null, isArchived = true),
        )

        private fun sample(vararg ids: String) = ids.map { id -> SampleWardrobe.first { it.id == id } }

        val SampleOutfits = listOf(
            Outfit("o1", "Casual Weekend", "Tampilan santai namun tetap terlihat rapi untuk sabtu pagi.", sample("w1", "w5", "w8")),
            Outfit("o2", "Office Minimalist", "Profesional tapi tetap nyaman seharian.", sample("w3", "w5", "w9", "w10")),
            Outfit("o3", "Coffee Date", "Vibe hangat cafe estetik", sample("w4", "w6")),
            Outfit("o4", "Night Out", "Gaya edgy yang berani.", sample("w2", "w5", "w8")),
        )
    }
}
