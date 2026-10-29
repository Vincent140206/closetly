package vin.pam.closetly.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vin.pam.closetly.core.data.CrudRepository
import vin.pam.closetly.core.model.ClosetlyEntity

data class FeatureUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

abstract class FeatureViewModel<T : ClosetlyEntity>(
    private val repository: CrudRepository<T>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState<T>())
    val uiState: StateFlow<FeatureUiState<T>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unable to load items")
                    }
                }
                .collect { items ->
                    _uiState.value = FeatureUiState(items = items, isLoading = false)
                }
        }
    }

    fun item(id: String): T? = uiState.value.items.firstOrNull { it.id == id }

    fun save(item: T, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching { repository.save(item) }
                .onSuccess { onComplete() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to save") }
                }
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    fun delete(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess { onComplete() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to delete") }
                }
        }
    }
}
