package vin.pam.closetly.feature.wearhistory

import androidx.lifecycle.ViewModelStore
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import vin.pam.closetly.feature.wearhistory.data.repository.WearHistoryRepository
import vin.pam.closetly.feature.wearhistory.model.SavedOutfit
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.ui.WearHistoryViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class WearHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    @Test fun repeatedHistoryAndOutfitReads_areCoalesced() = runTest(dispatcher) {
        val repository = ControlledRepository()
        val viewModel = WearHistoryViewModel(repository, "account-a").also { store.put("history", it) }
        repeat(20) { viewModel.refresh(); viewModel.loadSavedOutfits() }
        runCurrent()
        assertEquals(1, repository.historyReads)
        assertEquals(1, repository.outfitReads)
        repository.gate.complete(Unit)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoadingOutfits)
        assertEquals("worn", viewModel.uiState.value.histories.single().id)
    }

    @Test fun failedReload_keepsExistingHistory_andCanRetry() = runTest(dispatcher) {
        val repository = ControlledRepository().apply { gate.complete(Unit) }
        val viewModel = WearHistoryViewModel(repository, "account-a").also { store.put("history", it) }
        runCurrent()
        repository.fail = true
        viewModel.refresh()
        runCurrent()
        assertEquals("worn", viewModel.uiState.value.histories.single().id)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.errorMessage)
        repository.fail = false
        viewModel.refresh()
        runCurrent()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private class ControlledRepository : WearHistoryRepository {
        val gate = CompletableDeferred<Unit>()
        var historyReads = 0
        var outfitReads = 0
        var fail = false
        override suspend fun loadHistories(userId: String): List<WearHistory> {
            historyReads++; gate.await(); check(!fail) { "Periksa koneksi lalu coba lagi." }
            return listOf(WearHistory("worn", "outfit", "Office", LocalDate.of(2026, 11, 16), "Work"))
        }
        override suspend fun loadSavedOutfits(userId: String): List<SavedOutfit> {
            outfitReads++; gate.await(); return listOf(SavedOutfit("outfit", "Office"))
        }
    }
}
