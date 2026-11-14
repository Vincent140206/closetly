package vin.pam.closetly.feature.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
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

    init {
        viewModelScope.launch { repository.items.collect { items -> state.update { it.copy(items = items) } } }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refresh()
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
    fun selectCategory(category: WardrobeCategory?) = state.update { it.copy(filter = it.filter.copy(category = category)) }
    fun applyFilter(filter: WardrobeFilter) = state.update { it.copy(filter = filter) }

    fun toggleFavorite(item: ClothingItem) = mutate { repository.setFavorite(item.id, !item.isFavorite) }
    fun archive(id: String) = mutate {
        repository.archiveItem(id)
        state.update { it.copy(archivedItemId = id) }
    }
    fun consumeArchive() = state.update { it.copy(archivedItemId = null) }
    fun restore(id: String) = mutate { repository.restoreItem(id) }

    private fun mutate(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                state.update { it.copy(errorMessage = null) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                state.update { it.copy(errorMessage = error.message ?: "Perubahan belum tersimpan. Coba lagi.") }
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
