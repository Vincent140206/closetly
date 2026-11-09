package vin.pam.closetly.feature.outfit.model

import java.time.LocalDate

enum class ClothingCategory(val key: String, val label: String) {
    Top("atasan", "Atasan"),
    Bottom("bawahan", "Bawahan"),
    Shoes("sepatu", "Sepatu"),
    Accessory("aksesori", "Aksesori"),
    ;

    companion object {
        fun fromKey(key: String): ClothingCategory? =
            entries.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) }
    }
}

/**
 * Item Digital Wardrobe yang dilihat Mix & Match (baca saja).
 * Sumber akhirnya tabel `clothing_items`; `isArchived` = kolom `is_archived`.
 * Item aktif = `isArchived == false`.
 */
data class ClothingOption(
    val id: String,
    val name: String,
    val category: ClothingCategory,
    val color: String,
    val size: String?,
    val imagePath: String?,
    val isArchived: Boolean = false,
)

/**
 * Kombinasi outfit. Urutan [items] dipertahankan dan dipetakan ke
 * `outfit_items.position`. [description] dipetakan ke `outfits.description`
 * (catatan styling).
 */
data class Outfit(
    val id: String,
    val name: String,
    val description: String,
    val items: List<ClothingOption>,
    /** Dipetakan ke `outfits.created_at`. */
    val createdAt: LocalDate = LocalDate.now(),
)
