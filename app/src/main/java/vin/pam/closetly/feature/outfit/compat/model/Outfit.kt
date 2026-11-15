package vin.pam.closetly.feature.outfit.compat.model

import java.time.LocalDate

enum class ClothingCategory {
    Top,
    Bottom,
    Shoes,
    Accessory,
}

data class ClothingOption(
    val id: String,
    val name: String,
    val category: ClothingCategory,
    val color: String,
    val size: String?,
    val imageUrl: String?,
    val isArchived: Boolean = false,
)

data class Outfit(
    val id: String,
    val name: String,
    val description: String,
    val items: List<ClothingOption>,
    val createdAt: LocalDate = LocalDate.now(),
)
