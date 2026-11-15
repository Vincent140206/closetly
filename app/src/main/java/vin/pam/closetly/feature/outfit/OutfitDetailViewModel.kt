package vin.pam.closetly.feature.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.auth.toUserMessage
import vin.pam.closetly.feature.outfit.data.repository.OutfitRepository
import vin.pam.closetly.feature.outfit.ui.OutfitDetailUiState

class OutfitDetailViewModel(
    private val repository: OutfitRepository,
    private val outfitId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutfitDetailUiState())
    val uiState: StateFlow<OutfitDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** Dipanggil saat layar resume, jadi hasil edit langsung terlihat. */
    fun refresh() {
        if (_uiState.value.isDeleted) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.outfit == null, errorMessage = null) }
            try {
                val outfit = repository.getOutfit(outfitId)
                _uiState.update { it.copy(outfit = outfit) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorMessage = error.toUserMessage("Outfit tidak dapat dimuat."))
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDeleteClick() = _uiState.update { it.copy(showDeleteDialog = true, errorMessage = null) }

    fun onDismissDelete() = _uiState.update {
        if (it.isDeleting) it else it.copy(showDeleteDialog = false)
    }

    /** Hapus hanya berjalan setelah pengguna menekan "Hapus" pada dialog konfirmasi. */
    fun onConfirmDelete() {
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                repository.deleteOutfit(outfitId)
                _uiState.update { it.copy(isDeleted = true, showDeleteDialog = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        errorMessage = error.toUserMessage("Outfit tidak dapat dihapus."),
                    )
                }
            } finally {
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    companion object {
        fun factory(repository: OutfitRepository, outfitId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OutfitDetailViewModel(repository, outfitId) as T
            }
    }
}
