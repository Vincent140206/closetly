package vin.pam.closetly.feature.outfit.compat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.data.repository.OutfitDraft
import vin.pam.closetly.feature.outfit.data.repository.OutfitRepository
import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.feature.outfit.model.Outfit
import vin.pam.closetly.feature.outfit.compat.ui.OutfitDetailUiState
import vin.pam.closetly.feature.outfit.compat.ui.OutfitFormUiState
import vin.pam.closetly.feature.outfit.compat.ui.OutfitLoadState
import vin.pam.closetly.feature.outfit.compat.ui.moved

object OutfitGraph {
    val repository: OutfitRepository by lazy { FakeOutfitRepository() }
}

class OutfitListViewModel(private val repository: OutfitRepository) : ViewModel() {
    private val state = MutableStateFlow<OutfitLoadState<List<Outfit>>>(OutfitLoadState.Loading)
    val uiState = state.asStateFlow()
    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (state.value is OutfitLoadState.Error) state.value = OutfitLoadState.Loading
            state.value = loadOutfitData("Daftar outfit gagal dimuat.", repository::getOutfits)
        }
    }

    companion object {
        fun factory(repository: OutfitRepository) = viewModelFactory {
            initializer { OutfitListViewModel(repository) }
        }
    }
}

class OutfitDetailViewModel(
    private val repository: OutfitRepository,
    private val outfitId: String,
) : ViewModel() {
    private val state = MutableStateFlow(OutfitDetailUiState())
    val uiState = state.asStateFlow()
    private var refreshJob: Job? = null

    fun refresh() {
        if (state.value.isDeleting || state.value.deleted) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (state.value.outfit is OutfitLoadState.Error) {
                state.update { it.copy(outfit = OutfitLoadState.Loading) }
            }
            val outfit = loadOutfitData("Outfit gagal dimuat.") { repository.getOutfit(outfitId) }
            state.update { it.copy(outfit = outfit) }
        }
    }

    fun onDeleteClick() = state.update { it.copy(showDeleteConfirmation = true, deleteError = null) }

    fun onDismissDelete() = state.update { it.copy(showDeleteConfirmation = false) }

    fun onConfirmDelete() {
        val current = state.value
        if (!current.showDeleteConfirmation || current.isDeleting || current.deleted) return
        refreshJob?.cancel()
        state.update { it.copy(showDeleteConfirmation = false, isDeleting = true) }
        viewModelScope.launch {
            when (val result = loadOutfitData("Outfit gagal dihapus.") { repository.deleteOutfit(outfitId) }) {
                is OutfitLoadState.Loaded -> state.update { it.copy(isDeleting = false, deleted = true) }
                is OutfitLoadState.Error -> state.update { it.copy(isDeleting = false, deleteError = result.message) }
                OutfitLoadState.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(repository: OutfitRepository, outfitId: String) = viewModelFactory {
            initializer { OutfitDetailViewModel(repository, outfitId) }
        }
    }
}

class OutfitFormViewModel(
    private val repository: OutfitRepository,
    private val outfitId: String?,
) : ViewModel() {
    private val state = MutableStateFlow(OutfitFormUiState(isEditing = outfitId != null, isLoading = true))
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val outfit = outfitId?.let { repository.getOutfit(it) }
                val options = repository.getSelectableClothing()
                state.update {
                    it.copy(
                        name = outfit?.name.orEmpty(),
                        description = outfit?.description.orEmpty(),
                        selectedItems = outfit?.items.orEmpty(),
                        availableItems = options,
                        isLoading = false,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                state.update { it.copy(isLoading = false, loadError = error.message ?: "Form outfit gagal dimuat.") }
            }
        }
    }

    fun onNameChange(name: String) = state.update { it.copy(name = name, saveError = null) }

    fun onNameFocusLost() = state.update { it.copy(nameTouched = true) }

    fun onDescriptionChange(description: String) =
        state.update { it.copy(description = description, saveError = null) }

    fun onCategorySelected(category: ClothingCategory?) =
        state.update { it.copy(activeCategory = category) }

    fun onToggleItem(item: ClothingOption) = state.update { current ->
        val remaining = current.selectedItems.filterNot { it.id == item.id }
        if (remaining.size != current.selectedItems.size) {
            current.copy(selectedItems = remaining, saveError = null)
        } else {
            current.copy(selectedItems = remaining + item, saveError = null)
        }
    }

    fun onMoveItem(from: Int, to: Int) = state.update {
        it.copy(selectedItems = it.selectedItems.moved(from, to), saveError = null)
    }

    fun onRemoveItem(item: ClothingOption) = state.update {
        it.copy(selectedItems = it.selectedItems.filterNot { selected -> selected.id == item.id }, saveError = null)
    }

    fun onSave() {
        val current = state.value
        if (!current.canSave) return
        val draft = OutfitDraft(current.name, current.description, current.selectedItems.map { it.id })
        state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                if (outfitId == null) repository.createOutfit(draft) else repository.updateOutfit(outfitId, draft)
                state.update { it.copy(isSaving = false, saved = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                state.update { it.copy(isSaving = false, saveError = error.message ?: "Outfit gagal disimpan.") }
            }
        }
    }

    companion object {
        fun factory(repository: OutfitRepository, outfitId: String?) = viewModelFactory {
            initializer { OutfitFormViewModel(repository, outfitId) }
        }
    }
}

private suspend fun <T> loadOutfitData(
    fallback: String,
    request: suspend () -> T,
): OutfitLoadState<T> = try {
    OutfitLoadState.Loaded(request())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    OutfitLoadState.Error(error.message ?: fallback)
}
