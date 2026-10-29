package vin.pam.closetly.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    fun createOrNull(url: String?, publishableKey: String?): SupabaseClient? {
        if (url.isNullOrBlank() || publishableKey.isNullOrBlank()) return null
        return createSupabaseClient(supabaseUrl = url, supabaseKey = publishableKey) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
