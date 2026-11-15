package vin.pam.closetly.feature.outfit.ui

import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit

data class OutfitListUiState(
    val isLoading: Boolean = true,
    val outfits: List<Outfit> = emptyList(),
    val errorMessage: String? = null,
)

data class OutfitFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val description: String = "",
    val selectedItems: List<ClothingOption> = emptyList(),
    val activeCategory: ClothingCategory = ClothingCategory.Top,
    /** Hanya item aktif dari Digital Wardrobe. */
    val availableItems: List<ClothingOption> = emptyList(),
    val nameTouched: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
) {
    val nameError: String?
        get() = if (nameTouched && name.isBlank()) "Nama outfit wajib diisi" else null

    val canSave: Boolean
        get() = name.isNotBlank() && selectedItems.isNotEmpty() && !isSaving && !isLoading

    val visibleItems: List<ClothingOption>
        get() = availableItems.filter { it.category == activeCategory }
}

data class OutfitDetailUiState(
    val isLoading: Boolean = true,
    val outfit: Outfit? = null,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
)

/** Memindahkan elemen dari indeks [from] ke [to]; indeks di luar rentang diabaikan. */
internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
