package vin.pam.closetly.feature.wardrobe.data.repository

import kotlinx.serialization.json.JsonObject
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemInsert
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemRecord

/** Network boundary for wardrobe requests, including an account check before every operation. */
interface WardrobeRemoteDataSource {
    fun currentUserId(): String?
    suspend fun listPage(userId: String, afterId: String?, limit: Int): List<ClothingItemRecord>
    suspend fun find(userId: String, id: String): ClothingItemRecord?
    suspend fun insert(item: ClothingItemInsert): ClothingItemRecord
    suspend fun update(userId: String, id: String, values: JsonObject, activeOnly: Boolean = false): ClothingItemRecord
    suspend fun upload(userId: String, objectPath: String, png: ByteArray)
    suspend fun download(userId: String, objectPath: String): ByteArray
}

interface WardrobeImageFiles {
    fun cachedPath(objectPath: String): String
    suspend fun cache(objectPath: String, download: suspend () -> ByteArray): String
    suspend fun prepareUpload(localPath: String): ByteArray
}
