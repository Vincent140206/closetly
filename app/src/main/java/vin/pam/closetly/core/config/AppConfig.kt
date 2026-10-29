package vin.pam.closetly.core.config

import vin.pam.closetly.BuildConfig

data class AppConfig(
    val supabaseUrl: String?,
    val supabasePublishableKey: String?,
) {
    val isSupabaseConfigured: Boolean
        get() = !supabaseUrl.isNullOrBlank() && !supabasePublishableKey.isNullOrBlank()

    companion object {
        fun fromBuildConfig(): AppConfig = AppConfig(
            supabaseUrl = BuildConfig.SUPABASE_URL.takeIf(String::isNotBlank),
            supabasePublishableKey = BuildConfig.SUPABASE_ANON_KEY.takeIf(String::isNotBlank),
        )
    }
}
