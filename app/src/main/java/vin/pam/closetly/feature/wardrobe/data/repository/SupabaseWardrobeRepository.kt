package vin.pam.closetly.feature.wardrobe.data.repository

import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemInsert
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemRecord
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.PhotoAdjustments

class SupabaseWardrobeRepository(
    private val remote: WardrobeRemoteDataSource,
    private val userId: String,
    private val images: WardrobeImageFiles,
    private val elapsedMillis: () -> Long = { System.nanoTime() / 1_000_000 },
    private val now: () -> Instant = Instant::now,
) : WardrobeRepository {
    private val mutex = Mutex()
    private val state = MutableStateFlow<List<ClothingItem>>(emptyList())
    override val items = state.asStateFlow()
    private var refreshedAt: Long? = null
    private var recordsById = emptyMap<String, ClothingItemRecord>()
    private val photoDownloads = Semaphore(4)

    override suspend fun refresh(force: Boolean) = operation {
        // The mutex also coalesces concurrent lifecycle refreshes into one successful fetch.
        if (!force && refreshedAt?.let { elapsedMillis() - it in 0 until CACHE_TTL_MILLIS } == true) return@operation
        refreshedAt = null
        val records = mutableListOf<ClothingItemRecord>()
        var after: String? = null
        do {
            val page = remote.listPage(userId, after, PAGE_SIZE)
            page.forEach(::checkOwner)
            check(page.size <= PAGE_SIZE &&
                page.zipWithNext().all { (first, second) -> first.id < second.id } &&
                (after == null || page.all { it.id > after })) {
                "Urutan data koleksi tidak valid. Coba muat ulang."
            }
            records.addAll(page)
            val last = page.lastOrNull()?.id
            check(page.isEmpty() || last != after) { "Daftar koleksi tidak dapat dimuat. Coba lagi." }
            after = last
        } while (page.size == PAGE_SIZE)
        val cached = records.map(::cachedItem)
        requireSession()
        recordsById = records.associateBy { it.id }
        state.value = cached.sortedByDescending { it.createdAt }
        refreshedAt = elapsedMillis()
    }

    override suspend fun loadPhoto(id: String) {
        val record = operation { recordsById[id] } ?: return
        val path = record.imagePath ?: return
        if (images.existingPath(path) != null) return
        // Keep photo latency out of the CRUD lock. A stale download cannot undo newer metadata.
        val hydrated = photoDownloads.withPermit { hydrate(record) }
        operation {
            val current = recordsById[id]
            if (current != null && current.imagePath == record.imagePath) {
                publish(cachedItem(current).copy(imagePath = hydrated.imagePath))
            }
        }
    }

    override suspend fun getItem(id: String): ClothingItem = operation {
        val record = findRequired(id)
        val item = hydrate(record)
        recordsById = recordsById + (record.id to record)
        publish(item)
        item
    }

    override suspend fun loadPhotoForEditing(id: String): ClothingItem = operation {
        val record = recordsById[id] ?: findRequired(id)
        val item = hydrate(record, includeOriginal = true)
        recordsById = recordsById + (record.id to record)
        publish(item)
        item
    }

    override suspend fun createItem(requestId: String, draft: ClothingDraft): ClothingItem = operation {
        UUID.fromString(requestId)
        val valid = draft.validated()
        val existing = remote.find(userId, requestId)
        if (existing != null) return@operation hydrateAndPublish(existing)
        val photos = uploadPhotos(requestId, valid)
        val insert = ClothingItemInsert(
            id = requestId, userId = userId, name = valid.name, category = valid.categoryKey,
            color = valid.color, size = valid.size, style = valid.style,
            imagePath = photos.processed, originalImagePath = photos.original,
            photoAdjustments = photos.adjustments,
        )
        val record = try {
            requireSession()
            remote.insert(insert)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            // A response can be lost after the server committed. Recover by the stable request ID.
            val committed = try {
                requireSession()
                remote.find(userId, requestId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            committed ?: throw error
        }
        hydrateAndPublish(record)
    }

    override suspend fun updateItem(id: String, draft: ClothingDraft): ClothingItem = operation {
        val valid = draft.validated()
        val existing = findRequired(id)
        check(existing.archivedAt == null) { "Pulihkan pakaian dari arsip sebelum mengedit." }
        val photos = if (valid.photoChanged) uploadPhotos(id, valid) else null
        val changes = buildJsonObject {
            put("name", valid.name)
            put("category", valid.categoryKey)
            put("color", valid.color)
            put("size", valid.size)
            put("style", valid.style)
            if (photos != null) {
                put("image_path", photos.processed?.let { kotlinx.serialization.json.JsonPrimitive(it) } ?: JsonNull)
                put("original_image_path", photos.original?.let { kotlinx.serialization.json.JsonPrimitive(it) } ?: JsonNull)
                put("photo_adjustments", Json.encodeToJsonElement(photos.adjustments))
            }
            put("updated_at", now().toString())
        }
        requireSession()
        hydrateAndPublish(remote.update(userId, id, changes, activeOnly = true))
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        operation {
            val record = remote.update(userId, id, buildJsonObject {
                put("is_favorite", favorite)
                put("updated_at", now().toString())
            })
            hydrateAndPublish(record)
        }
    }

    override suspend fun archiveItem(id: String) = setArchived(id, archived = true)
    override suspend fun restoreItem(id: String) = setArchived(id, archived = false)

    private suspend fun setArchived(id: String, archived: Boolean) {
        operation {
            val existing = findRequired(id)
            if ((existing.archivedAt != null) == archived) {
                hydrateAndPublish(existing)
            } else {
                val timestamp = now().toString()
                val changed = remote.update(userId, id, buildJsonObject {
                    put("archived_at", if (archived) kotlinx.serialization.json.JsonPrimitive(timestamp) else JsonNull)
                    put("updated_at", timestamp)
                })
                hydrateAndPublish(changed)
            }
        }
    }

    private data class PhotoPaths(val processed: String?, val original: String?, val adjustments: PhotoAdjustments)

    private suspend fun uploadPhotos(id: String, draft: ClothingDraft): PhotoPaths {
        val localImage = draft.imagePath ?: return PhotoPaths(null, null, PhotoAdjustments())
        val processed = images.prepareUpload(localImage)
        val original = images.prepareUpload(draft.originalImagePath ?: localImage)
        require(processed.size <= MAX_IMAGE_BYTES && original.size <= MAX_IMAGE_BYTES) { "Foto maksimal 5 MB per file." }
        val digest = MessageDigest.getInstance("SHA-256").apply { update(original); update(processed) }
            .digest().joinToString("") { "%02x".format(it) }
        val prefix = "$userId/wardrobe/$id/$digest"
        val originalPath = "$prefix-original.png"
        val processedPath = "$prefix-processed.png"
        requireSession()
        remote.upload(userId, originalPath, original)
        requireSession()
        remote.upload(userId, processedPath, processed)
        cachePhoto(originalPath) { original }
        cachePhoto(processedPath) { processed }
        // Retain immutable objects after ambiguous failures; retries reuse the same digest paths.
        return PhotoPaths(processedPath, originalPath, draft.photoAdjustments)
    }

    private suspend fun hydrate(record: ClothingItemRecord, includeOriginal: Boolean = false): ClothingItem {
        checkOwner(record)
        val image = record.imagePath?.let { path -> cachePhoto(path) { remote.download(userId, path) } }
        val original = (record.originalImagePath ?: record.imagePath)?.let { path ->
            if (includeOriginal) cachePhoto(path) { remote.download(userId, path) } else images.cachedPath(path)
        }
        requireSession()
        return record.toDomain(image, original)
    }

    private fun cachedItem(record: ClothingItemRecord): ClothingItem {
        checkOwner(record)
        return record.toDomain(
            record.imagePath?.let(images::existingPath),
            (record.originalImagePath ?: record.imagePath)?.let(images::existingPath),
        )
    }

    private suspend fun cachePhoto(path: String, bytes: suspend () -> ByteArray): String {
        require(path.startsWith("$userId/")) { "Foto bukan milik akun ini." }
        return try {
            images.cache(path) {
                requireSession()
                bytes()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // A failed image download must not hide the clothing record or block metadata editing.
            images.cachedPath(path)
        }
    }

    private suspend fun hydrateAndPublish(record: ClothingItemRecord): ClothingItem {
        // A successful write should not wait for an unrelated Storage download.
        val item = cachedItem(record)
        recordsById = recordsById + (record.id to record)
        publish(item)
        return item
    }

    private fun publish(item: ClothingItem) {
        requireSession()
        state.value = (state.value.filterNot { it.id == item.id } + item).sortedByDescending { it.createdAt }
    }

    private suspend fun findRequired(id: String): ClothingItemRecord {
        requireSession()
        return remote.find(userId, id)?.also(::checkOwner)
            ?: throw NoSuchElementException("Pakaian tidak ditemukan.")
    }

    private fun checkOwner(record: ClothingItemRecord) {
        check(record.userId == userId) { "Pakaian bukan milik akun ini." }
        listOfNotNull(record.imagePath, record.originalImagePath).forEach { path ->
            check(path.startsWith("$userId/") && path.split('/').none { it.isEmpty() || it == "." || it == ".." }) {
                "Lokasi foto pakaian tidak valid untuk akun ini. Muat ulang koleksi."
            }
        }
    }

    private fun requireSession() {
        if (remote.currentUserId() != userId) {
            state.value = emptyList()
            refreshedAt = null
            recordsById = emptyMap()
            error("Sesi pengguna berubah. Masuk kembali untuk mengakses koleksi.")
        }
    }

    private suspend fun <T> operation(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            requireSession()
            try {
                block().also { requireSession() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                requireSession()
                if (error is IllegalArgumentException || error is IllegalStateException || error is NoSuchElementException) throw error
                throw IllegalStateException("Koleksi belum dapat disinkronkan. Periksa koneksi dan coba lagi.", error)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 200
        private const val CACHE_TTL_MILLIS = 60_000L
        private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}
