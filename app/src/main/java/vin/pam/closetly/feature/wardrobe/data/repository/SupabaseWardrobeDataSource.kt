package vin.pam.closetly.feature.wardrobe.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonObject
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemInsert
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemRecord

class SupabaseWardrobeDataSource(private val supabase: SupabaseClient) : WardrobeRemoteDataSource {
    override fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    private fun requireAccount(userId: String) {
        check(currentUserId() == userId) { "Sesi pengguna berubah. Masuk kembali untuk mengakses koleksi." }
    }

    override suspend fun listPage(userId: String, afterId: String?, limit: Int): List<ClothingItemRecord> {
        requireAccount(userId)
        return supabase.from(TABLE).select(columns = COLUMNS) {
            filter {
                eq("user_id", userId)
                if (afterId != null) gt("id", afterId)
            }
            order("id", Order.ASCENDING)
            limit(limit.toLong())
        }.decodeList()
    }

    override suspend fun find(userId: String, id: String): ClothingItemRecord? {
        requireAccount(userId)
        return supabase.from(TABLE).select(columns = COLUMNS) {
            filter { eq("id", id); eq("user_id", userId) }
        }.decodeSingleOrNull()
    }

    override suspend fun insert(item: ClothingItemInsert): ClothingItemRecord {
        requireAccount(item.userId)
        return supabase.from(TABLE).insert(item) { select(columns = COLUMNS) }.decodeSingle()
    }

    override suspend fun update(userId: String, id: String, values: JsonObject, activeOnly: Boolean): ClothingItemRecord {
        requireAccount(userId)
        return supabase.from(TABLE).update(values) {
            select(columns = COLUMNS)
            filter {
                eq("id", id)
                eq("user_id", userId)
                if (activeOnly) exact("archived_at", null)
            }
        }.decodeSingleOrNull<ClothingItemRecord>()
            ?: throw NoSuchElementException("Pakaian tidak ditemukan atau sudah diarsipkan. Muat ulang koleksi.")
    }

    override suspend fun upload(userId: String, objectPath: String, png: ByteArray) {
        requireAccount(userId)
        require(objectPath.startsWith("$userId/wardrobe/")) { "Lokasi foto tidak sesuai dengan akun." }
        supabase.storage.from(BUCKET).upload(objectPath, png) {
            contentType = ContentType.Image.PNG
            // Object names contain a digest of their contents, making an upload retry safe.
            upsert = true
        }
    }

    override suspend fun download(userId: String, objectPath: String): ByteArray {
        requireAccount(userId)
        require(objectPath.startsWith("$userId/")) { "Foto bukan milik akun ini." }
        return supabase.storage.from(BUCKET).downloadAuthenticated(objectPath)
    }

    companion object {
        private const val TABLE = "clothing_items"
        const val BUCKET = "closetly-images"
        private val COLUMNS = Columns.list(
            "id", "user_id", "name", "category", "color", "size", "style",
            "image_path", "original_image_path", "photo_adjustments", "is_favorite",
            "archived_at", "created_at", "updated_at",
        )
    }
}
