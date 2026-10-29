package vin.pam.closetly.di

import io.github.jan.supabase.SupabaseClient
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.data.SupabaseProvider

class AppContainer(
    val config: AppConfig,
) {
    val supabaseClient: SupabaseClient? by lazy {
        SupabaseProvider.createOrNull(
            url = config.supabaseUrl,
            publishableKey = config.supabasePublishableKey,
        )
    }
}
