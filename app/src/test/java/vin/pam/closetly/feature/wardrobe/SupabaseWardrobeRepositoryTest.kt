package vin.pam.closetly.feature.wardrobe

import java.io.IOException
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemInsert
import vin.pam.closetly.feature.wardrobe.data.dto.ClothingItemRecord
import vin.pam.closetly.feature.wardrobe.data.repository.SupabaseWardrobeRepository
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeImageFiles
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRemoteDataSource
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.PhotoAdjustments
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.toDraft

class SupabaseWardrobeRepositoryTest {
    private val owner = "00000000-0000-0000-0000-000000000001"
    private val other = "00000000-0000-0000-0000-000000000002"
    private val timestamp = "2026-11-14T10:00:00Z"

    private fun draft(name: String = "Kemeja linen") = ClothingDraft(
        name = name, category = WardrobeCategory.Top, color = "Putih", size = "M",
    )

    private fun record(id: String = UUID.randomUUID().toString(), userId: String = owner) = ClothingItemRecord(
        id = id, userId = userId, name = "Kemeja linen", category = "atasan", color = "Putih", size = "M",
        createdAt = timestamp, updatedAt = timestamp,
    )

    @Test
    fun dto_mapsArchivedTimestamp_defaultsAndLegacyCategories() {
        val source = """{"id":"x","user_id":"$owner","name":"Jaket lama","category":"Outerwear","color":"Biru","size":"L","archived_at":"$timestamp","photo_adjustments":{},"created_at":"2026-11-14T17:00:00+07:00","updated_at":"$timestamp"}"""
        val decoded = Json.decodeFromString<ClothingItemRecord>(source)
        val item = decoded.toDomain("cache.png", "original.png")
        assertTrue(item.isArchived)
        assertFalse(item.isFavorite)
        assertEquals(Instant.parse(timestamp).toEpochMilli(), item.createdAt)
        assertEquals(WardrobeCategory.Other, item.category)
        assertEquals("Outerwear", item.categoryLabel)
        assertEquals("Outerwear", item.toDraft().validated().categoryKey)
        assertEquals(PhotoAdjustments(), item.photoAdjustments)
    }

    @Test
    fun create_recoversLostCommitResponse_andRetryKeepsOneRow() = runBlocking {
        val remote = FakeRemote().apply { loseInsertResponse = true }
        val repository = repository(remote)
        val id = UUID.randomUUID().toString()
        assertEquals(id, repository.createItem(id, draft()).id)
        assertEquals(id, repository.createItem(id, draft()).id)
        assertEquals(1, remote.rows.size)
        assertEquals(1, remote.insertCount)
        assertEquals(1, repository.items.value.size)
    }

    @Test
    fun uploadFailure_keepsDatabaseEmpty_andRetryReusesImmutableObjects() = runBlocking {
        val remote = FakeRemote().apply { failSecondUpload = true }
        val repository = repository(remote)
        val id = UUID.randomUUID().toString()
        val photoDraft = draft().copy(imagePath = "picked.png", originalImagePath = "original.png")
        try {
            repository.createItem(id, photoDraft)
            throw AssertionError("Expected upload failure")
        } catch (_: IllegalStateException) { }
        assertTrue(remote.rows.isEmpty())
        val originalPath = remote.objects.keys.single()
        repository.createItem(id, photoDraft)
        assertEquals(2, remote.objects.size)
        assertTrue(originalPath in remote.objects)
        val row = remote.rows.getValue(id)
        assertTrue(requireNotNull(row.imagePath).startsWith("$owner/wardrobe/$id/"))
        assertTrue(requireNotNull(row.imagePath).endsWith("-processed.png"))
        assertFalse(requireNotNull(row.imagePath).contains("cache"))
    }

    @Test
    fun metadataEdit_keepsCurrentCloudPhotoAndFavoriteAfterConcurrentChange() = runBlocking {
        val remote = FakeRemote()
        val files = FakeImages()
        val originalPath = "$owner/wardrobe/old/original.png"
        val oldPath = "$owner/wardrobe/old/processed.png"
        val initial = record().copy(imagePath = oldPath, originalImagePath = originalPath)
        remote.rows[initial.id] = initial
        remote.objects[oldPath] = byteArrayOf(1)
        remote.objects[originalPath] = byteArrayOf(2)
        val repository = repository(remote, files)
        val form = repository.getItem(initial.id).toDraft()
        val currentPath = "$owner/wardrobe/new/processed.png"
        remote.rows[initial.id] = initial.copy(
            imagePath = currentPath, isFavorite = true,
            photoAdjustments = PhotoAdjustments(backgroundRemoved = true),
        )
        remote.objects[currentPath] = byteArrayOf(3)
        repository.updateItem(initial.id, form.copy(name = "Kemeja biru"))
        val saved = remote.rows.getValue(initial.id)
        assertEquals(currentPath, saved.imagePath)
        assertTrue(saved.isFavorite)
        assertTrue(saved.photoAdjustments.backgroundRemoved)
        assertEquals(0, remote.uploadCount)
        assertFalse(requireNotNull(remote.lastPatch).containsKey("image_path"))
        assertFalse(requireNotNull(remote.lastPatch).containsKey("photo_adjustments"))
    }

    @Test
    fun explicitPhotoRemoval_clearsBothPathsAndAdjustments() = runBlocking {
        val remote = FakeRemote()
        val item = record().copy(imagePath = "$owner/wardrobe/x.png", photoAdjustments = PhotoAdjustments(backgroundRemoved = true))
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.updateItem(item.id, draft().copy(photoChanged = true))
        val saved = remote.rows.getValue(item.id)
        assertEquals(null, saved.imagePath)
        assertEquals(null, saved.originalImagePath)
        assertEquals(PhotoAdjustments(), saved.photoAdjustments)
    }

    @Test
    fun archiveAndRestore_patchTimestamp_andRetainRecord() = runBlocking {
        val remote = FakeRemote()
        val item = record().copy(isFavorite = true)
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.archiveItem(item.id)
        assertEquals(timestamp, remote.rows.getValue(item.id).archivedAt)
        assertTrue(repository.items.value.single().isArchived)
        assertTrue(remote.rows.getValue(item.id).isFavorite)
        assertEquals(setOf("archived_at", "updated_at"), remote.lastPatch?.keys)
        repository.restoreItem(item.id)
        assertEquals(null, remote.rows.getValue(item.id).archivedAt)
        assertFalse(repository.items.value.single().isArchived)
        assertEquals(1, remote.rows.size)
    }

    @Test
    fun refresh_pagesBeyondServerLimit_andOnlyLoadsOwnerRows() = runBlocking {
        val remote = FakeRemote()
        (1..425).forEach { index ->
            val id = "00000000-0000-0000-0001-${index.toString().padStart(12, '0')}"
            remote.rows[id] = record(id)
        }
        val foreign = record(userId = other)
        remote.rows[foreign.id] = foreign
        val repository = repository(remote)
        repository.refresh()
        assertEquals(425, repository.items.value.size)
        assertEquals(3, remote.pageCount)
        assertFalse(repository.items.value.any { it.id == foreign.id })
    }

    @Test
    fun changedSession_clearsCachedState_andRejectsNewWrites() = runBlocking {
        val remote = FakeRemote()
        val item = record()
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.refresh()
        remote.sessionUserId = other
        try {
            repository.createItem(UUID.randomUUID().toString(), draft())
            throw AssertionError("Expected session guard")
        } catch (_: IllegalStateException) { }
        assertTrue(repository.items.value.isEmpty())
        assertEquals(0, remote.insertCount)
    }

    @Test
    fun sessionChangeDuringUpload_preventsDatabaseInsert() = runBlocking {
        val remote = FakeRemote().apply { afterUpload = { sessionUserId = other } }
        val repository = repository(remote)
        try {
            repository.createItem(UUID.randomUUID().toString(), draft().copy(imagePath = "picked.png"))
            throw AssertionError("Expected session guard")
        } catch (_: IllegalStateException) { }
        assertTrue(remote.rows.isEmpty())
        assertEquals(1, remote.uploadCount)
        assertTrue(repository.items.value.isEmpty())
    }

    @Test
    fun photoDownloadFailure_keepsClothingVisible() = runBlocking {
        val remote = FakeRemote()
        val item = record().copy(imagePath = "$owner/wardrobe/missing.png")
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.refresh()
        assertEquals(item.id, repository.items.value.single().id)
        assertEquals("cache/${item.imagePath}", repository.items.value.single().imagePath)
    }

    @Test
    fun validation_matchesVerifiedDatabaseBounds() {
        draft("A").validated()
        draft("A".repeat(120)).copy(color = "B".repeat(60), size = "S".repeat(40)).validated()
        listOf(draft().copy(color = " "), draft().copy(size = ""), draft("A".repeat(121))).forEach { invalid ->
            try {
                invalid.validated()
                throw AssertionError("Expected invalid draft to be rejected")
            } catch (_: IllegalArgumentException) { }
        }
    }

    private fun repository(remote: FakeRemote, files: FakeImages = FakeImages()) =
        SupabaseWardrobeRepository(remote, owner, files) { Instant.parse(timestamp) }

    @Test
    fun refresh_coalescesConcurrentReads_includingEmptyCollection_andExpiresOrForces() = runBlocking {
        val remote = FakeRemote()
        var elapsed = 0L
        val repository = SupabaseWardrobeRepository(remote, owner, FakeImages(), elapsedMillis = { elapsed })
        coroutineScope { (1..20).map { async { repository.refresh() } }.awaitAll() }
        assertEquals(1, remote.pageCount)
        elapsed = 59_999
        repository.refresh()
        assertEquals(1, remote.pageCount)
        elapsed = 60_000
        repository.refresh()
        assertEquals(2, remote.pageCount)
        repository.refresh(force = true)
        assertEquals(3, remote.pageCount)
    }

    @Test
    fun failedRefresh_retainsVisibleData_andNextAttemptRetriesDespiteCache() = runBlocking {
        val remote = FakeRemote()
        val item = record()
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.refresh()
        remote.pageError = IOException("Offline")
        try {
            repository.refresh(force = true)
            throw AssertionError("Expected offline failure")
        } catch (_: IllegalStateException) { }
        assertEquals(item.id, repository.items.value.single().id)
        remote.pageError = null
        remote.rows[item.id] = item.copy(name = "Dari perangkat lain")
        repository.refresh()
        assertEquals(3, remote.pageCount)
        assertEquals("Dari perangkat lain", repository.items.value.single().name)
    }

    @Test
    fun successfulMutation_updatesCachedCollection_withoutAnotherListRequest() = runBlocking {
        val remote = FakeRemote()
        val item = record()
        remote.rows[item.id] = item
        val repository = repository(remote)
        repository.refresh()
        repository.setFavorite(item.id, true)
        repository.refresh()
        assertEquals(1, remote.pageCount)
        assertTrue(repository.items.value.single().isFavorite)
    }

    private class FakeImages : WardrobeImageFiles {
        private val cached = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()
        override fun cachedPath(objectPath: String) = "cache/$objectPath"
        override fun existingPath(objectPath: String) = cachedPath(objectPath).takeIf { cached.containsKey(objectPath) }
        override suspend fun cache(objectPath: String, download: suspend () -> ByteArray): String {
            if (!cached.containsKey(objectPath)) cached[objectPath] = download()
            return cachedPath(objectPath)
        }
        override suspend fun prepareUpload(localPath: String) = localPath.toByteArray()
    }

    private inner class FakeRemote : WardrobeRemoteDataSource {
        var sessionUserId: String? = owner
        val rows = mutableMapOf<String, ClothingItemRecord>()
        val objects = mutableMapOf<String, ByteArray>()
        var insertCount = 0
        var uploadCount = 0
        var pageCount = 0
        var pageError: Exception? = null
        var loseInsertResponse = false
        var failSecondUpload = false
        var afterUpload: (() -> Unit)? = null
        var lastPatch: JsonObject? = null
        private val json = Json { encodeDefaults = true }

        override fun currentUserId() = sessionUserId
        private fun requireOwner(userId: String) = check(sessionUserId == userId)
        override suspend fun listPage(userId: String, afterId: String?, limit: Int): List<ClothingItemRecord> {
            requireOwner(userId)
            pageCount++
            pageError?.let { throw it }
            return rows.values.filter { it.userId == userId && (afterId == null || it.id > afterId) }.sortedBy { it.id }.take(limit)
        }
        override suspend fun find(userId: String, id: String): ClothingItemRecord? {
            requireOwner(userId)
            return rows[id]?.takeIf { it.userId == userId }
        }
        override suspend fun insert(item: ClothingItemInsert): ClothingItemRecord {
            requireOwner(item.userId)
            check(item.id !in rows)
            insertCount++
            val saved = ClothingItemRecord(
                item.id, item.userId, item.name, item.category, item.color, item.size, item.style,
                imagePath = item.imagePath, originalImagePath = item.originalImagePath,
                photoAdjustments = item.photoAdjustments, createdAt = timestamp, updatedAt = timestamp,
            )
            rows[item.id] = saved
            if (loseInsertResponse) {
                loseInsertResponse = false
                throw IOException("Response lost after commit")
            }
            return saved
        }
        override suspend fun update(userId: String, id: String, values: JsonObject, activeOnly: Boolean): ClothingItemRecord {
            val previous = find(userId, id) ?: throw NoSuchElementException()
            if (activeOnly && previous.archivedAt != null) throw NoSuchElementException()
            lastPatch = values
            val merged = JsonObject(json.encodeToJsonElement(previous).jsonObject + values)
            val updated = json.decodeFromString<ClothingItemRecord>(merged.toString())
            rows[id] = updated
            return updated
        }
        override suspend fun upload(userId: String, objectPath: String, png: ByteArray) {
            requireOwner(userId)
            uploadCount++
            if (failSecondUpload && uploadCount == 2) {
                failSecondUpload = false
                throw IOException("Connection lost")
            }
            objects[objectPath] = png
            afterUpload?.invoke()
        }
        override suspend fun download(userId: String, objectPath: String): ByteArray {
            requireOwner(userId)
            return objects[objectPath] ?: throw IOException("Missing photo")
        }
    }
}
