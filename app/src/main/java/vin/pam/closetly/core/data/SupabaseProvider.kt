package vin.pam.closetly.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    const val AUTH_CALLBACK_SCHEME = "vin.pam.closetly"
    const val AUTH_CALLBACK_HOST = "auth-callback"
    const val AUTH_CALLBACK_URL = "$AUTH_CALLBACK_SCHEME://$AUTH_CALLBACK_HOST"

    fun createOrNull(url: String?, publishableKey: String?): SupabaseClient? {
        if (url.isNullOrBlank() || publishableKey.isNullOrBlank()) return null
        return createSupabaseClient(supabaseUrl = url, supabaseKey = publishableKey) {
            install(Auth) {
                scheme = AUTH_CALLBACK_SCHEME
                host = AUTH_CALLBACK_HOST
                flowType = FlowType.PKCE
            }
            install(Postgrest)
            install(Storage)
        }
    }
}
