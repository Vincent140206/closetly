package vin.pam.closetly.feature.wardrobe.ui

import android.graphics.Bitmap
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeFilter
import vin.pam.closetly.feature.wardrobe.model.matching
import vin.pam.closetly.feature.wardrobe.photo.WhiteBalance

data class WardrobeUiState(
    val isLoading: Boolean = true,
    val items: List<ClothingItem> = emptyList(),
    val outfitCount: Int = 0,
    val query: String = "",
    val filter: WardrobeFilter = WardrobeFilter(),
    val errorMessage: String? = null,
    val archivedItemId: String? = null,
) {
    val visibleItems: List<ClothingItem> get() = items.matching(query, filter)
    val activeCount: Int get() = items.count { !it.isArchived }
}

data class WardrobeFormUiState(
    val draft: ClothingDraft = ClothingDraft(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val attemptedSave: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val photoEditor: PhotoEditorState? = null,
    val hasChanges: Boolean = false,
)

data class PhotoEditorState(
    val originalPath: String,
    val original: Bitmap,
    val preview: Bitmap,
    val balance: WhiteBalance = WhiteBalance(),
    val backgroundRemoved: Boolean = false,
    val isProcessing: Boolean = false,
    val isPickingNeutral: Boolean = false,
    val errorMessage: String? = null,
)
