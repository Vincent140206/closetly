package vin.pam.closetly.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.sessionState.collect { sessionState ->
                _uiState.update { it.copy(sessionState = sessionState) }
            }
        }
        restoreSession()
    }

    fun login(email: String, password: String) {
        val validationError = AuthInputValidator.loginError(email, password)
        if (validationError != null) {
            showError(validationError)
            return
        }

        submit {
            repository.signIn(email.trim(), password)
            _uiState.update { it.copy(noticeMessage = null) }
        }
    }

    fun register(
        displayName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        val validationError = AuthInputValidator.registrationError(
            displayName = displayName,
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
        )
        if (validationError != null) {
            showError(validationError)
            return
        }

        submit {
            when (
                repository.signUp(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim(),
                    username = username,
                )
            ) {
                RegistrationResult.Authenticated -> {
                    _uiState.update {
                        it.copy(noticeMessage = "Account created successfully.")
                    }
                }
                is RegistrationResult.EmailConfirmationRequired -> {
                    _uiState.update {
                        it.copy(
                            noticeMessage =
                                "Cek email konfirmasi di perangkat ini. Buka tautannya untuk kembali ke Closetly. " +
                                    "Jika dibuka di perangkat lain, masuk manual setelah email terverifikasi.",
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        submit {
            repository.signOut()
            _uiState.update { it.copy(noticeMessage = "You have been signed out.") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, noticeMessage = null) }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                repository.restoreSession()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        sessionState = AuthSessionState.Failure(
                            error.toUserMessage("The saved session could not be restored."),
                        ),
                    )
                }
            }
        }
    }

    private fun submit(action: suspend () -> Unit) {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, noticeMessage = null)
            }
            try {
                action()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                showError(error.toUserMessage("Authentication request failed. Please try again."))
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, noticeMessage = null) }
    }

    companion object {
        fun factory(repository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(repository) as T
            }
    }
}

object AuthInputValidator {
    internal const val MinimumPasswordLength = 8
    private val UsernamePattern = Regex("[a-z0-9]+")

    fun loginError(email: String, password: String): String? = when {
        !email.isValidEmail() -> "Enter a valid email address."
        password.isBlank() -> "Password is required."
        else -> null
    }

    fun registrationError(
        displayName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): String? = when {
        displayName.trim().length < 2 -> "Display name must contain at least 2 characters."
        !email.isValidEmail() -> "Enter a valid email address."
        !UsernamePattern.matches(username) -> "Gunakan huruf kecil dan angka tanpa spasi untuk username."
        !hasMinimumPasswordLength(password) -> "Password harus memiliki minimal $MinimumPasswordLength karakter."
        !hasPasswordNumber(password) -> "Password harus mengandung angka."
        password != confirmPassword -> "Password confirmation does not match."
        else -> null
    }

    internal fun hasMinimumPasswordLength(password: String): Boolean =
        password.length >= MinimumPasswordLength

    internal fun hasPasswordNumber(password: String): Boolean =
        password.any(Char::isDigit)

    private fun String.isValidEmail(): Boolean {
        val normalized = trim()
        return normalized.count { it == '@' } == 1 &&
            normalized.substringBefore('@').isNotBlank() &&
            normalized.substringAfter('@').contains('.') &&
            !normalized.contains(' ')
    }
}

internal fun Throwable.toUserMessage(fallback: String): String =
    message?.takeIf(String::isNotBlank) ?: fallback
