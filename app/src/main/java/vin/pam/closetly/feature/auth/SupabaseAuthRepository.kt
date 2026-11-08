package vin.pam.closetly.feature.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import vin.pam.closetly.core.data.SupabaseProvider

class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
) : AuthRepository {
    private var recoverySession: UserSession? = null

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
        username: String,
    ): RegistrationResult {
        val pendingUser = supabase.auth.signUpWith(
            Email,
            redirectUrl = SupabaseProvider.AUTH_CALLBACK_URL,
        ) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName)
                put("username", username)
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

    override suspend fun requestPasswordReset(email: String) {
        supabase.auth.resetPasswordForEmail(email, redirectUrl = SupabaseProvider.PASSWORD_RECOVERY_URL)
    }

    override suspend fun openPasswordRecovery(code: String): String {
        supabase.auth.awaitInitialization()
        val session = supabase.auth.exchangeCodeForSession(code, saveSession = false)
        val email = requireNotNull(session.user?.email)
        recoverySession = session
        return email
    }

    override suspend fun updateRecoveredPassword(password: String) {
        val session = checkNotNull(recoverySession)
        // Keep the recovery token separate from the app's normal sign-in and storage.
        val recoveryClient = createSupabaseClient(supabase.supabaseUrl, supabase.supabaseKey) {
            install(Auth) {
                autoLoadFromStorage = false
                autoSaveToStorage = false
                alwaysAutoRefresh = false
            }
        }
        try {
            recoveryClient.auth.awaitInitialization()
            recoveryClient.auth.importSession(session, autoRefresh = false)
            recoveryClient.auth.updateUser(updateCurrentUser = false) { this.password = password }
            clearPasswordRecovery()
        } finally {
            recoveryClient.close()
        }
    }

    override fun clearPasswordRecovery() {
        recoverySession = null
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
