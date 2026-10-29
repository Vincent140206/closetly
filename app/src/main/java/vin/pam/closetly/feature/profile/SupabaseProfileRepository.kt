package vin.pam.closetly.feature.profile

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseProfileRepository(
    private val supabase: SupabaseClient,
) : ProfileRepository {
    override suspend fun fetchCurrentProfile(): UserProfile {
        val user = requireCurrentUser()
        val record = supabase.from(PROFILES_TABLE)
            .select(columns = PROFILE_COLUMNS) {
                filter {
                    eq("id", user.id)
                }
            }
            .decodeSingle<ProfileRecord>()

        return record.toDomain(email = user.email.orEmpty())
    }

    override suspend fun updateDisplayName(displayName: String): UserProfile {
        val user = requireCurrentUser()
        val record = supabase.from(PROFILES_TABLE)
            .update(
                update = {
                    set("display_name", displayName)
                },
                request = {
                    select(columns = PROFILE_COLUMNS)
                    filter {
                        eq("id", user.id)
                    }
                },
            )
            .decodeSingle<ProfileRecord>()

        return record.toDomain(email = user.email.orEmpty())
    }

    private fun requireCurrentUser() = requireNotNull(supabase.auth.currentUserOrNull()) {
        "An authenticated session is required to access the profile."
    }

    private fun ProfileRecord.toDomain(email: String): UserProfile = UserProfile(
        id = id,
        displayName = displayName,
        email = email,
    )

    companion object {
        private const val PROFILES_TABLE = "profiles"
        private val PROFILE_COLUMNS = Columns.list("id", "display_name", "avatar_path")
    }
}

@Serializable
private data class ProfileRecord(
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("avatar_path")
    val avatarPath: String? = null,
)
