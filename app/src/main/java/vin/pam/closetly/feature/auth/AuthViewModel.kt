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

    fun requestPasswordReset(email: String) {
        if (AuthInputValidator.emailError(email) != null) {
            showError("Masukkan alamat email yang valid.")
            return
        }

        submit {
            repository.requestPasswordReset(email.trim())
            _uiState.update {
                it.copy(
                    noticeMessage =
                        "Jika alamat email ini terdaftar, tautan reset akan dikirim. " +
                            "Buka tautannya di perangkat yang sama.",
                )
            }
        }
    }

    fun openPasswordRecovery(code: String?) {
        val current = _uiState.value.passwordRecovery
        if (current == PasswordRecoveryState.Opening ||
            (current is PasswordRecoveryState.Ready && current.isSaving)
        ) return

        repository.clearPasswordRecovery()
        if (code.isNullOrBlank()) {
            setPasswordRecovery(PasswordRecoveryState.InvalidLink)
            return
        }

        setPasswordRecovery(PasswordRecoveryState.Opening)
        viewModelScope.launch {
            try {
                setPasswordRecovery(PasswordRecoveryState.Ready(repository.openPasswordRecovery(code)))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                setPasswordRecovery(PasswordRecoveryState.InvalidLink)
            }
        }
    }

    fun updateRecoveredPassword(password: String, confirmation: String) {
        val recovery = _uiState.value.passwordRecovery as? PasswordRecoveryState.Ready ?: return
        if (recovery.isSaving) return
        val validationError = AuthInputValidator.passwordChangeError(password, confirmation)
        if (validationError != null) {
            setPasswordRecovery(recovery.copy(errorMessage = validationError))
            return
        }

        setPasswordRecovery(recovery.copy(isSaving = true, errorMessage = null))
        viewModelScope.launch {
            try {
                repository.updateRecoveredPassword(password)
                setPasswordRecovery(PasswordRecoveryState.Complete)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                setPasswordRecovery(
                    recovery.copy(
                        errorMessage = "Password gagal disimpan. Coba lagi atau minta tautan reset baru.",
                    ),
                )
            }
        }
    }

    fun dismissPasswordRecovery() {
        val current = _uiState.value.passwordRecovery
        if (current == PasswordRecoveryState.Opening ||
            (current is PasswordRecoveryState.Ready && current.isSaving)
        ) return

        repository.clearPasswordRecovery()
        setPasswordRecovery(null)
    }

    private fun setPasswordRecovery(state: PasswordRecoveryState?) {
        _uiState.update { it.copy(passwordRecovery = state) }
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

    fun loginError(email: String, password: String): String? =
        emailError(email) ?: "Password is required.".takeIf { password.isBlank() }

    internal fun emailError(email: String): String? =
        "Enter a valid email address.".takeUnless { email.isValidEmail() }

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
        else -> passwordChangeError(password, confirmPassword)
    }

    internal fun passwordChangeError(password: String, confirmPassword: String): String? = when {
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
