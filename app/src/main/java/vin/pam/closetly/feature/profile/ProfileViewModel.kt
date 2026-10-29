package vin.pam.closetly.feature.profile

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

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
)

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var authenticatedUserId: String? = null
    private var loadJob: Job? = null

    fun onSessionChanged(userId: String?) {
        if (authenticatedUserId == userId) return
        authenticatedUserId = userId

        if (userId == null) {
            loadJob?.cancel()
            _uiState.value = ProfileUiState()
        } else {
            loadProfile()
        }
    }

    fun refresh() {
        if (authenticatedUserId != null) loadProfile()
    }

    fun updateDisplayName(displayName: String) {
        val normalizedName = displayName.trim()
        if (normalizedName.length < 2) {
            _uiState.update {
                it.copy(errorMessage = "Display name must contain at least 2 characters.")
            }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, errorMessage = null, noticeMessage = null)
            }
            try {
                val updatedProfile = repository.updateDisplayName(normalizedName)
                _uiState.update {
                    it.copy(
                        profile = updatedProfile,
                        noticeMessage = "Display name updated.",
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = error.toUserMessage("The profile could not be updated."),
                    )
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, noticeMessage = null) }
    }

    private fun loadProfile() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, noticeMessage = null)
            }
            try {
                val profile = repository.fetchCurrentProfile()
                _uiState.update { it.copy(profile = profile) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        profile = null,
                        errorMessage = error.toUserMessage("The profile could not be loaded."),
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(repository: ProfileRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProfileViewModel(repository) as T
            }
    }
}
