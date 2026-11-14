package vin.pam.closetly.feature.wardrobe.model

import kotlinx.serialization.Serializable

@Serializable
enum class WardrobeCategory(val key: String, val label: String) {
    Top("atasan", "Atasan"),
    Bottom("bawahan", "Bawahan"),
    Shoes("sepatu", "Sepatu"),
    Accessory("aksesori", "Aksesori"),
}

/** Domain model. Remote DTOs will follow the verified clothing_items schema. */
@Serializable
data class ClothingItem(
    val id: String,
    val name: String,
    val category: WardrobeCategory,
    val color: String = "",
    val size: String = "",
    val style: String = "Casual",
    val imagePath: String? = null,
    val originalImagePath: String? = null,
    val photoAdjustments: PhotoAdjustments = PhotoAdjustments(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class ClothingDraft(
    val name: String = "",
    val category: WardrobeCategory? = null,
    val color: String = "",
    val size: String = "",
    val style: String = "Casual",
    val imagePath: String? = null,
    val originalImagePath: String? = null,
    val photoAdjustments: PhotoAdjustments = PhotoAdjustments(),
) {
    fun validated(): ClothingDraft {
        require(name.trim().length in 2..80) { "Nama pakaian harus berisi 2–80 karakter." }
        require(category != null) { "Pilih kategori pakaian." }
        require(color.trim().length <= 40) { "Warna maksimal 40 karakter." }
        require(size.trim().length <= 20) { "Ukuran maksimal 20 karakter." }
        require(style.trim().length <= 40) { "Gaya maksimal 40 karakter." }
        return copy(name = name.trim(), color = color.trim(), size = size.trim(), style = style.trim())
    }
}

@Serializable
data class PhotoAdjustments(
    val redGain: Double = 1.0,
    val greenGain: Double = 1.0,
    val blueGain: Double = 1.0,
    val backgroundRemoved: Boolean = false,
)

fun ClothingItem.toDraft() = ClothingDraft(
    name, category, color, size, style, imagePath, originalImagePath, photoAdjustments,
)

data class WardrobeFilter(
    val category: WardrobeCategory? = null,
    val color: String = "",
    val favoritesOnly: Boolean = false,
    val archivedOnly: Boolean = false,
) {
    val isActive: Boolean get() = category != null || color.isNotBlank() || favoritesOnly || archivedOnly
}

fun List<ClothingItem>.matching(query: String, criteria: WardrobeFilter): List<ClothingItem> {
    val search = query.trim()
    return filter { item ->
        item.isArchived == criteria.archivedOnly &&
            (criteria.category == null || item.category == criteria.category) &&
            (criteria.color.isBlank() || item.color.equals(criteria.color, ignoreCase = true)) &&
            (!criteria.favoritesOnly || item.isFavorite) &&
            (search.isBlank() || item.name.contains(search, ignoreCase = true))
    }.sortedByDescending { it.createdAt }
}
