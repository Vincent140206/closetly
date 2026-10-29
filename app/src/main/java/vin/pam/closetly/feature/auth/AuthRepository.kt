package vin.pam.closetly.feature.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionState: Flow<AuthSessionState>

    suspend fun restoreSession()

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): RegistrationResult

    suspend fun signIn(email: String, password: String)

    suspend fun signOut()

    fun currentUserId(): String?

    fun currentUserEmail(): String?
}

class UnavailableAuthRepository(
    private val reason: String = CONFIGURATION_ERROR,
) : AuthRepository {
    override val sessionState: Flow<AuthSessionState> =
        kotlinx.coroutines.flow.flowOf(AuthSessionState.Unavailable(reason))

    override suspend fun restoreSession() = Unit

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): RegistrationResult = throw IllegalStateException(reason)

    override suspend fun signIn(email: String, password: String): Unit =
        throw IllegalStateException(reason)

    override suspend fun signOut(): Unit = throw IllegalStateException(reason)

    override fun currentUserId(): String? = null

    override fun currentUserEmail(): String? = null

    companion object {
        const val CONFIGURATION_ERROR =
            "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to secrets.properties, then rebuild the app."
    }
}
