package vin.pam.closetly.feature.auth

data class AuthenticatedUser(
    val id: String,
    val email: String,
)

sealed interface AuthSessionState {
    data object Initializing : AuthSessionState

    data class Authenticated(
        val user: AuthenticatedUser,
    ) : AuthSessionState

    data object NotAuthenticated : AuthSessionState

    data class Failure(
        val message: String,
    ) : AuthSessionState

    data class Unavailable(
        val message: String,
    ) : AuthSessionState
}

data class AuthUiState(
    val sessionState: AuthSessionState = AuthSessionState.Initializing,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
)

sealed interface RegistrationResult {
    data object Authenticated : RegistrationResult

    data class EmailConfirmationRequired(
        val email: String,
    ) : RegistrationResult
}
