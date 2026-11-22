package vin.pam.closetly.feature.wearhistory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.feature.wearhistory.data.repository.WearHistoryRepository
import vin.pam.closetly.feature.wearhistory.model.SavedOutfit
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import java.time.LocalDate
import java.time.YearMonth

/** State tunggal yang dibaca oleh Compose. */
data class WearHistoryUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val histories: List<WearHistory> = emptyList(),
    val savedOutfits: List<SavedOutfit> = emptyList(),
    val isLoadingOutfits: Boolean = false,
    val outfitError: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val historiesThisMonth: List<WearHistory>
        get() = histories.filter { YearMonth.from(it.wornDate) == visibleMonth }
}

/** ViewModel hanya tahu interface Repository, tidak mengimpor SDK Supabase. */
class WearHistoryViewModel(
    private val repository: WearHistoryRepository,
    private val userId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WearHistoryUiState())
    val uiState: StateFlow<WearHistoryUiState> = _uiState.asStateFlow()
    private var refreshJob: Job? = null
    private var outfitsJob: Job? = null

    init { refresh() }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val records = repository.loadHistories(userId)
                _uiState.update { it.copy(histories = records, isLoading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat riwayat")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadSavedOutfits() {
        if (outfitsJob?.isActive == true) return
        outfitsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingOutfits = true,
                    outfitError = null
                )
            }

            try {
                val outfits = repository.loadSavedOutfits(userId)

                _uiState.update {
                    it.copy(
                        savedOutfits = outfits,
                        isLoadingOutfits = false
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingOutfits = false,
                        outfitError = error.message
                            ?: "Gagal mengambil outfit tersimpan"
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoadingOutfits = false) }
            }
        }
    }


    /** offset = -1 untuk bulan sebelumnya, +1 untuk bulan berikutnya. */
    fun changeMonth(offset: Long) {
        _uiState.update { state ->
            state.copy(visibleMonth = state.visibleMonth.plusMonths(offset), selectedDate = null)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    companion object {
        fun factory(repository: WearHistoryRepository, userId: String): ViewModelProvider.Factory =
            viewModelFactory { initializer { WearHistoryViewModel(repository, userId) } }
    }
}
