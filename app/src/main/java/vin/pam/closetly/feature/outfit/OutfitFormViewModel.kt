package vin.pam.closetly.feature.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.auth.toUserMessage
import vin.pam.closetly.feature.outfit.data.repository.OutfitDraft
import vin.pam.closetly.feature.outfit.data.repository.OutfitRepository
import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.ui.OutfitFormUiState
import vin.pam.closetly.feature.outfit.ui.moved

/** [outfitId] null = outfit baru; terisi = mode edit. */
class OutfitFormViewModel(
    private val repository: OutfitRepository,
    private val outfitId: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutfitFormUiState(isEditing = outfitId != null))
    val uiState: StateFlow<OutfitFormUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val options = repository.getSelectableClothing()
                val outfit = outfitId?.let { repository.getOutfit(it) }
                _uiState.update {
                    it.copy(
                        availableItems = options,
                        name = outfit?.name.orEmpty(),
                        description = outfit?.description.orEmpty(),
                        selectedItems = outfit?.items.orEmpty(),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorMessage = error.toUserMessage("Data outfit tidak dapat dimuat."))
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onNameFocusLost() = _uiState.update { it.copy(nameTouched = true) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onCategorySelected(category: ClothingCategory) =
        _uiState.update { it.copy(activeCategory = category) }

    /** Boleh lebih dari satu item per kategori. */
    fun onToggleItem(item: ClothingOption) = _uiState.update { state ->
        val alreadySelected = state.selectedItems.any { it.id == item.id }
        state.copy(
            selectedItems = if (alreadySelected) {
                state.selectedItems.filterNot { it.id == item.id }
            } else {
                state.selectedItems + item
            },
        )
    }

    /** Urutan [selectedItems] dipertahankan saat disimpan (-> `outfit_items.position`). */
    fun onMoveItem(from: Int, to: Int) = _uiState.update { state ->
        state.copy(selectedItems = state.selectedItems.moved(from, to))
    }

    fun onRemoveItem(itemId: String) = _uiState.update { state ->
        state.copy(selectedItems = state.selectedItems.filterNot { it.id == itemId })
    }

    fun onSave() {
        _uiState.update { it.copy(nameTouched = true) }
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val draft = OutfitDraft(
                    name = state.name.trim(),
                    description = state.description.trim(),
                    clothingItemIds = state.selectedItems.map { it.id },
                )
                if (outfitId == null) {
                    repository.createOutfit(draft)
                } else {
                    repository.updateOutfit(outfitId, draft)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorMessage = error.toUserMessage("Outfit tidak dapat disimpan."))
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    companion object {
        fun factory(repository: OutfitRepository, outfitId: String?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OutfitFormViewModel(repository, outfitId) as T
            }
    }
}
