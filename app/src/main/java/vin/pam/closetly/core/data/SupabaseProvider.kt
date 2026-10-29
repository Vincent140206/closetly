package vin.pam.closetly.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    fun createOrNull(url: String?, anonKey: String?): SupabaseClient? {
        if (url.isNullOrBlank() || anonKey.isNullOrBlank()) return null
        return createSupabaseClient(supabaseUrl = url, supabaseKey = anonKey) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}

interface ImageStorageRepository {
    suspend fun upload(userId: String, feature: String, fileName: String, bytes: ByteArray): String
    suspend fun resolveUrl(storagePath: String): String
    suspend fun delete(storagePath: String)
}
