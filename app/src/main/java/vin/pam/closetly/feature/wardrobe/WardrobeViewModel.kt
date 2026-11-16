package vin.pam.closetly.feature.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.WardrobeFilter
import vin.pam.closetly.feature.wardrobe.ui.WardrobeUiState

class WardrobeViewModel(
    private val repository: WardrobeRepository,
    private val outfitCount: suspend () -> Int,
) : ViewModel() {
    private val state = MutableStateFlow(WardrobeUiState())
    val uiState = state.asStateFlow()
    private var refreshJob: Job? = null
    private val pendingPhotos = mutableSetOf<String>()

    init {
        viewModelScope.launch { repository.items.collect { items -> state.update { it.copy(items = items) } } }
    }

    fun refresh(force: Boolean = false) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refresh(force)
                val count = outfitCount()
                state.update { it.copy(outfitCount = count) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Koleksi gagal dimuat. Coba lagi.") }
            } finally {
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun search(query: String) = state.update { it.copy(query = query) }
    fun loadPhoto(id: String) {
        if (!pendingPhotos.add(id)) return
        viewModelScope.launch {
            try {
                repository.loadPhoto(id)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Foto belum dapat dimuat. Coba muat ulang koleksi.") }
            } finally {
                pendingPhotos.remove(id)
            }
        }
    }

    fun selectCategory(category: WardrobeCategory?) = state.update { it.copy(filter = it.filter.copy(category = category)) }
    fun applyFilter(filter: WardrobeFilter) = state.update { it.copy(filter = filter) }

    fun toggleFavorite(item: ClothingItem) = mutate(item.id) { repository.setFavorite(item.id, !item.isFavorite) }
    fun archive(id: String) = mutate(id) {
        repository.archiveItem(id)
        state.update { it.copy(archivedItemId = id) }
    }
    fun consumeArchive() = state.update { it.copy(archivedItemId = null) }
    fun restore(id: String) = mutate(id) { repository.restoreItem(id) }

    private fun mutate(id: String, operation: suspend () -> Unit) {
        if (id in state.value.pendingItemIds) return
        state.update { it.copy(pendingItemIds = it.pendingItemIds + id) }
        viewModelScope.launch {
            try {
                operation()
                state.update { it.copy(errorMessage = null) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Perubahan belum tersimpan. Coba lagi.") }
            } finally {
                state.update { it.copy(pendingItemIds = it.pendingItemIds - id) }
            }
        }
    }

    companion object {
        fun factory(repository: WardrobeRepository, outfitCount: suspend () -> Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = WardrobeViewModel(repository, outfitCount) as T
            }
    }
}
