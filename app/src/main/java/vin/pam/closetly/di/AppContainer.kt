package vin.pam.closetly.di

import io.github.jan.supabase.SupabaseClient
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.data.SupabaseProvider
import vin.pam.closetly.feature.auth.AuthRepository
import vin.pam.closetly.feature.auth.SupabaseAuthRepository
import vin.pam.closetly.feature.auth.UnavailableAuthRepository
import vin.pam.closetly.feature.profile.ProfileRepository
import vin.pam.closetly.feature.profile.SupabaseProfileRepository
import vin.pam.closetly.feature.profile.UnavailableProfileRepository

class AppContainer(
    val config: AppConfig,
) {
    val supabaseClient: SupabaseClient? by lazy {
        SupabaseProvider.createOrNull(
            url = config.supabaseUrl,
            publishableKey = config.supabasePublishableKey,
        )
    }

    val authRepository: AuthRepository by lazy {
        supabaseClient?.let(::SupabaseAuthRepository) ?: UnavailableAuthRepository()
    }

    val profileRepository: ProfileRepository by lazy {
        supabaseClient?.let(::SupabaseProfileRepository) ?: UnavailableProfileRepository()
    }
}
