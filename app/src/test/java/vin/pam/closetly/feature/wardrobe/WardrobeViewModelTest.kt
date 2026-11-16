package vin.pam.closetly.feature.wardrobe

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory

@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    private fun viewModel(repository: ControlledRepository) = WardrobeViewModel(repository) { 0 }
        .also { store.put("wardrobe", it) }

    @Test fun pendingMutation_blocksConflicts_andFailureAllowsRetry() = runTest(dispatcher) {
        val repository = ControlledRepository()
        val viewModel = viewModel(repository)
        repeat(10) { viewModel.toggleFavorite(repository.item) }
        viewModel.archive(repository.item.id)
        assertEquals(setOf(repository.item.id), viewModel.uiState.value.pendingItemIds)
        runCurrent()
        assertEquals(1, repository.writes)
        repository.failWrite = true
        repository.writeGate.complete(Unit)
        runCurrent()
        assertTrue(viewModel.uiState.value.pendingItemIds.isEmpty())
        assertNotNull(viewModel.uiState.value.errorMessage)
        repository.failWrite = false
        viewModel.toggleFavorite(repository.item)
        runCurrent()
        assertEquals(2, repository.writes)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.items.single().isFavorite)
    }

    @Test fun visiblePhotoRequests_coalesce_withoutBlockingCollectionOrWrites() = runTest(dispatcher) {
        val repository = ControlledRepository()
        val viewModel = viewModel(repository)
        viewModel.refresh()
        runCurrent()
        repeat(20) { viewModel.loadPhoto(repository.item.id) }
        runCurrent()
        assertEquals(1, repository.photoReads)
        assertFalse(viewModel.uiState.value.isLoading)
        repository.writeGate.complete(Unit)
        viewModel.toggleFavorite(repository.item)
        runCurrent()
        assertEquals(1, repository.writes)
        repository.photoGate.complete(Unit)
        runCurrent()
    }

    @Test fun refreshFailure_keepsItems_andReleasesLoadingForRetry() = runTest(dispatcher) {
        val repository = ControlledRepository().apply { failRefresh = true }
        val viewModel = viewModel(repository)
        viewModel.refresh()
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(repository.item), viewModel.uiState.value.items)
        assertNotNull(viewModel.uiState.value.errorMessage)
        repository.failRefresh = false
        viewModel.refresh(force = true)
        runCurrent()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private class ControlledRepository : WardrobeRepository {
        val item = ClothingItem("item", "Kemeja", WardrobeCategory.Top, "Putih", "M", createdAt = 0, updatedAt = 0)
        override val items = MutableStateFlow(listOf(item))
        val writeGate = CompletableDeferred<Unit>()
        val photoGate = CompletableDeferred<Unit>()
        var writes = 0
        var photoReads = 0
        var failWrite = false
        var failRefresh = false
        override suspend fun refresh(force: Boolean) { check(!failRefresh) { "Koleksi belum dapat dimuat." } }
        override suspend fun loadPhoto(id: String) { photoReads++; photoGate.await() }
        override suspend fun getItem(id: String) = items.value.single()
        override suspend fun createItem(requestId: String, draft: ClothingDraft) = error("Unexpected create")
        override suspend fun updateItem(id: String, draft: ClothingDraft) = error("Unexpected update")
        override suspend fun setFavorite(id: String, favorite: Boolean) {
            writes++; writeGate.await()
            check(!failWrite) { "Perubahan belum tersimpan." }
            items.value = listOf(item.copy(isFavorite = favorite))
        }
        override suspend fun archiveItem(id: String) { writes++ }
        override suspend fun restoreItem(id: String) { writes++ }
    }
}
