package vin.pam.closetly

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vin.pam.closetly.feature.wardrobe.WardrobeFormViewModel
import vin.pam.closetly.feature.wardrobe.WardrobeViewModel
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.PhotoAdjustments
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoProcessor
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoStore

class WardrobeResilienceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun missingPreview_canOpenOriginal_withoutClaimingOldAdjustments() = runBlocking {
        val directory = File(context.filesDir, "wardrobe-preview-test/${UUID.randomUUID()}").apply { mkdirs() }
        val original = File(directory, "original.png")
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        try {
            original.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { bitmap.recycle() }
        val repository = ControlledRepository().apply {
            item = item.copy(imagePath = File(directory, "missing.png").absolutePath, originalImagePath = original.absolutePath,
                photoAdjustments = PhotoAdjustments(redGain = 1.2, backgroundRemoved = true))
        }
        val store = ViewModelStore()
        lateinit var viewModel: WardrobeFormViewModel
        try {
            instrumentation.runOnMainSync {
                viewModel = WardrobeFormViewModel(repository, WardrobePhotoStore(context, File(directory, "images")),
                    WardrobePhotoProcessor(context), repository.item.id, SavedStateHandle())
                store.put("form", viewModel)
                viewModel.openPhotoEditor()
            }
            val editor = requireNotNull(withTimeout(5_000) { viewModel.uiState.first { it.photoEditor != null }.photoEditor })
            assertFalse(editor.backgroundRemoved)
            assertEquals(1.0, editor.balance.red, 0.0)
            assertTrue(editor.errorMessage != null)
            Unit
        } finally {
            instrumentation.runOnMainSync { store.clear() }
            directory.deleteRecursively()
        }
    }

    @Test
    fun collection_onlyLoadsOnEntry_andCoalescesRefreshAndRepeatedFavorite() {
        val repository = ControlledRepository()
        val store = ViewModelStore()
        try {
            instrumentation.runOnMainSync {
                val viewModel = WardrobeViewModel(repository) { 0 }
                store.put("collection", viewModel)
                assertEquals(0, repository.reads.get())
                repeat(10) { viewModel.refresh() }
                assertEquals(1, repository.reads.get())
                viewModel.search("linen")
                viewModel.selectCategory(WardrobeCategory.Top)
                assertEquals(1, repository.reads.get())
                repeat(10) { viewModel.toggleFavorite(repository.item) }
                assertEquals(1, repository.favoriteWrites.get())
                repository.gate.complete(Unit)
            }
            instrumentation.waitForIdleSync()
        } finally {
            instrumentation.runOnMainSync { store.clear() }
        }
    }

    @Test
    fun editLoadFailure_blocksSave_andCanRetryWithoutLosingDraft() {
        val repository = ControlledRepository().apply { failDetail = true }
        val store = ViewModelStore()
        try {
            instrumentation.runOnMainSync {
                val savedState = SavedStateHandle()
                val viewModel = WardrobeFormViewModel(
                    repository,
                    WardrobePhotoStore(context, File(context.cacheDir, "wardrobe-resilience/${UUID.randomUUID()}/images")),
                    WardrobePhotoProcessor(context), repository.item.id, savedState,
                )
                store.put("form", viewModel)
                assertTrue(viewModel.uiState.value.loadFailed)
                viewModel.save()
                assertEquals(0, repository.updates.get())
                repository.failDetail = false
                viewModel.loadItem()
                assertFalse(viewModel.uiState.value.loadFailed)
                viewModel.edit(viewModel.uiState.value.draft.copy(name = "Kemeja diedit"))
                viewModel.loadItem()
                assertEquals("Kemeja diedit", viewModel.uiState.value.draft.name)
                repository.failUpdate = true
                viewModel.save()
                assertFalse(viewModel.uiState.value.saved)
                assertFalse(viewModel.uiState.value.isSaving)
                assertEquals("Kemeja diedit", viewModel.uiState.value.draft.name)
                assertTrue(viewModel.uiState.value.errorMessage != null)
                repository.failUpdate = false
                viewModel.save()
                assertTrue(viewModel.uiState.value.saved)
                viewModel.save()
                assertEquals(2, repository.updates.get())
            }
        } finally {
            instrumentation.runOnMainSync { store.clear() }
        }
    }

    private class ControlledRepository : WardrobeRepository {
        var item = ClothingItem(UUID.randomUUID().toString(), "Kemeja linen", WardrobeCategory.Top, "Putih", "M", createdAt = 0, updatedAt = 0)
        override val items = MutableStateFlow(listOf(item))
        val reads = AtomicInteger()
        val favoriteWrites = AtomicInteger()
        val updates = AtomicInteger()
        val gate = CompletableDeferred<Unit>()
        var failDetail = false
        var failUpdate = false
        override suspend fun refresh(force: Boolean) { reads.incrementAndGet(); gate.await() }
        override suspend fun getItem(id: String): ClothingItem {
            check(!failDetail) { "Periksa koneksi lalu coba lagi." }
            return item
        }
        override suspend fun createItem(requestId: String, draft: ClothingDraft): ClothingItem = error("Unexpected create")
        override suspend fun updateItem(id: String, draft: ClothingDraft): ClothingItem {
            updates.incrementAndGet()
            check(!failUpdate) { "Perubahan belum tersimpan. Coba lagi." }
            return item.copy(name = draft.name)
        }
        override suspend fun setFavorite(id: String, favorite: Boolean) { favoriteWrites.incrementAndGet(); gate.await() }
        override suspend fun archiveItem(id: String) = Unit
        override suspend fun restoreItem(id: String) = Unit
    }
}
