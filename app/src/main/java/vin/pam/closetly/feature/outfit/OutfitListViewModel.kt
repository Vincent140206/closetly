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
import vin.pam.closetly.feature.outfit.ui.OutfitListUiState

class OutfitListViewModel(
    private val repository: OutfitRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutfitListUiState())
    val uiState: StateFlow<OutfitListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** Dipanggil saat layar resume, jadi daftar ikut segar setelah simpan/edit. */
    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.outfits.isEmpty(), errorMessage = null) }
            try {
                val outfits = repository.getOutfits()
                _uiState.update { it.copy(outfits = outfits) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(errorMessage = error.toUserMessage("Daftar outfit tidak dapat dimuat."))
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(repository: OutfitRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OutfitListViewModel(repository) as T
            }
    }
}
