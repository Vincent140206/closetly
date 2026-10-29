package vin.pam.closetly.feature.profile

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
)

interface ProfileRepository {
    suspend fun fetchCurrentProfile(): UserProfile

    suspend fun updateDisplayName(displayName: String): UserProfile
}

class UnavailableProfileRepository(
    private val reason: String =
        "Supabase is not configured. Profile data is unavailable until the app is rebuilt with valid credentials.",
) : ProfileRepository {
    override suspend fun fetchCurrentProfile(): UserProfile = throw IllegalStateException(reason)

    override suspend fun updateDisplayName(displayName: String): UserProfile =
        throw IllegalStateException(reason)
}
