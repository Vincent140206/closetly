package vin.pam.closetly.feature.wardrobe.data.repository

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem

/** Persistent local adapter used before Supabase integration. One instance per account directory. */
class LocalWardrobeRepository(
    private val file: File,
    private val initialItems: List<ClothingItem> = emptyList(),
    private val clock: () -> Long = System::currentTimeMillis,
) : WardrobeRepository {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mutableItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    override val items = mutableItems.asStateFlow()
    private var loaded = false

    @Serializable
    private data class Snapshot(val version: Int = 1, val items: List<ClothingItem>)

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val snapshot = if (file.exists()) {
                json.decodeFromString<Snapshot>(file.readText()).also {
                    check(it.version == 1) { "Format koleksi belum didukung oleh versi aplikasi ini." }
                }
            } else {
                Snapshot(items = initialItems).also { write(it.items) }
            }
            mutableItems.value = snapshot.items
            loaded = true
        }
    }

    private suspend fun ensureLoaded() {
        if (!loaded) refresh()
    }

    override suspend fun getItem(id: String): ClothingItem {
        ensureLoaded()
        return items.value.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Pakaian tidak ditemukan.")
    }

    override suspend fun createItem(requestId: String, draft: ClothingDraft): ClothingItem {
        UUID.fromString(requestId)
        val valid = draft.validated()
        ensureLoaded()
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val existing = items.value.firstOrNull { it.id == requestId }
                if (existing != null) return@withLock existing
                val now = clock()
                val item = ClothingItem(
                    id = requestId, name = valid.name, category = requireNotNull(valid.category),
                    color = valid.color, size = valid.size, style = valid.style,
                    imagePath = valid.imagePath, originalImagePath = valid.originalImagePath,
                    photoAdjustments = valid.photoAdjustments,
                    createdAt = now, updatedAt = now,
                )
                commit(listOf(item) + items.value)
                item
            }
        }
    }

    override suspend fun updateItem(id: String, draft: ClothingDraft): ClothingItem {
        val valid = draft.validated()
        return change(id) { item ->
            check(!item.isArchived) { "Pulihkan pakaian dari arsip sebelum mengedit." }
            item.copy(
                name = valid.name, category = requireNotNull(valid.category), color = valid.color,
                size = valid.size, style = valid.style, imagePath = valid.imagePath,
                originalImagePath = valid.originalImagePath, updatedAt = clock(),
                photoAdjustments = valid.photoAdjustments,
            )
        }
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        change(id) { it.copy(isFavorite = favorite, updatedAt = clock()) }
    }

    override suspend fun archiveItem(id: String) {
        change(id) { it.copy(isArchived = true, updatedAt = clock()) }
    }

    override suspend fun restoreItem(id: String) {
        change(id) { it.copy(isArchived = false, updatedAt = clock()) }
    }

    private suspend fun change(id: String, transform: (ClothingItem) -> ClothingItem): ClothingItem {
        ensureLoaded()
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val existing = items.value.firstOrNull { it.id == id }
                    ?: throw NoSuchElementException("Pakaian tidak ditemukan.")
                val updated = transform(existing)
                commit(items.value.map { if (it.id == id) updated else it })
                updated
            }
        }
    }

    private suspend fun commit(next: List<ClothingItem>) = withContext(NonCancellable) {
        write(next)
        mutableItems.value = next
    }

    private fun write(items: List<ClothingItem>) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(json.encodeToString(Snapshot(items = items)).toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        Files.move(
            temporary.toPath(), file.toPath(),
            StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
