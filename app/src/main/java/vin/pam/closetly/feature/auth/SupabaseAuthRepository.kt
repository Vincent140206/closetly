package vin.pam.closetly.feature.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
) : AuthRepository {
    override val sessionState: Flow<AuthSessionState> = supabase.auth.sessionStatus
        .map(::toAuthSessionState)
        .distinctUntilChanged()

    override suspend fun restoreSession() {
        supabase.auth.awaitInitialization()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): RegistrationResult {
        val pendingUser = supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName)
            }
        }

        return if (pendingUser == null || supabase.auth.currentUserOrNull() != null) {
            RegistrationResult.Authenticated
        } else {
            RegistrationResult.EmailConfirmationRequired(email)
        }
    }

    override suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override fun currentUserEmail(): String? = supabase.auth.currentUserOrNull()?.email

    private fun toAuthSessionState(status: SessionStatus): AuthSessionState = when (status) {
        SessionStatus.Initializing -> AuthSessionState.Initializing
        is SessionStatus.Authenticated -> {
            val user = status.session.user
            if (user == null) {
                AuthSessionState.Failure("The authenticated session does not contain user information.")
            } else {
                AuthSessionState.Authenticated(
                    user = AuthenticatedUser(
                        id = user.id,
                        email = user.email.orEmpty(),
                    ),
                )
            }
        }
        is SessionStatus.NotAuthenticated -> AuthSessionState.NotAuthenticated
        is SessionStatus.RefreshFailure -> AuthSessionState.Failure(
            "The saved session could not be restored. Check your connection and try again.",
        )
    }
}
