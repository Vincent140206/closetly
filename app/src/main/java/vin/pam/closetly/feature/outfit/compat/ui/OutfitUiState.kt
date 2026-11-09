package vin.pam.closetly.feature.outfit.compat.ui

import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit

sealed interface OutfitLoadState<out T> {
    data object Loading : OutfitLoadState<Nothing>
    data class Loaded<T>(val value: T) : OutfitLoadState<T>
    data class Error(val message: String) : OutfitLoadState<Nothing>
}

data class OutfitDetailUiState(
    val outfit: OutfitLoadState<Outfit> = OutfitLoadState.Loading,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleted: Boolean = false,
)

data class OutfitFormUiState(
    val name: String = "",
    val nameTouched: Boolean = false,
    val description: String = "",
    val availableItems: List<ClothingOption> = emptyList(),
    val selectedItems: List<ClothingOption> = emptyList(),
    val activeCategory: ClothingCategory? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false,
) {
    val nameError: String?
        get() = "Nama outfit wajib diisi".takeIf { nameTouched && name.isBlank() }

    val visibleItems: List<ClothingOption>
        get() = availableItems.filter { activeCategory == null || it.category == activeCategory }

    val canSave: Boolean
        get() = !isLoading && loadError == null && !isSaving && !saved &&
            name.isNotBlank() && selectedItems.isNotEmpty()
}

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
