package vin.pam.closetly.feature.outfit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.data.repository.OutfitDraft
import vin.pam.closetly.feature.outfit.model.ClothingCategory
import vin.pam.closetly.feature.outfit.ui.OutfitFormUiState
import vin.pam.closetly.feature.outfit.ui.moved

class OutfitLogicTest {
    private val repository = FakeOutfitRepository()

    @Test
    fun selectableClothing_excludesArchivedItems() = runBlocking {
        val options = repository.getSelectableClothing()
        assertTrue(options.isNotEmpty())
        assertTrue(options.none { it.isArchived })
    }

    @Test
    fun createOutfit_preservesSelectedOrder() = runBlocking {
        val created = repository.createOutfit(OutfitDraft("Test", "  catatan ", listOf("w8", "w1", "w5")))
        assertEquals(listOf("w8", "w1", "w5"), created.items.map { it.id })
        assertEquals("catatan", created.description)
        assertEquals(created.id, repository.getOutfits().first().id)
    }

    @Test
    fun createOutfit_rejectsBlankNameAndEmptySelection() = runBlocking {
        assertRejected { repository.createOutfit(OutfitDraft("  ", "", listOf("w1"))) }
        assertRejected { repository.createOutfit(OutfitDraft("Nama", "", emptyList())) }
    }

    @Test
    fun createOutfit_rejectsNewlyChosenArchivedItem() = runBlocking {
        assertRejected { repository.createOutfit(OutfitDraft("Nama", "", listOf("w7"))) }
    }

    @Test
    fun updateOutfit_changesExistingOutfitOnly() = runBlocking {
        val before = repository.getOutfits().size
        val updated = repository.updateOutfit("o1", OutfitDraft("Baru", "", listOf("w2")))
        assertEquals("Baru", updated.name)
        assertEquals(before, repository.getOutfits().size)
    }

    @Test
    fun deleteOutfit_removesItFromList() = runBlocking {
        repository.deleteOutfit("o1")
        assertTrue(repository.getOutfits().none { it.id == "o1" })
    }

    @Test
    fun formState_requiresNameAndAtLeastOneItem() {
        val item = FakeOutfitRepository.SampleWardrobe.first()
        assertFalse(OutfitFormUiState().canSave)
        assertFalse(OutfitFormUiState(name = "Look").canSave)
        assertFalse(OutfitFormUiState(selectedItems = listOf(item)).canSave)
        assertTrue(OutfitFormUiState(name = "Look", selectedItems = listOf(item)).canSave)
    }

    @Test
    fun formState_showsNameErrorOnlyAfterTouched() {
        assertNull(OutfitFormUiState().nameError)
        assertEquals("Nama outfit wajib diisi", OutfitFormUiState(nameTouched = true).nameError)
    }

    @Test
    fun formState_filtersVisibleItemsByCategory() {
        val state = OutfitFormUiState(
            availableItems = FakeOutfitRepository.SampleWardrobe.filterNot { it.isArchived },
            activeCategory = ClothingCategory.Shoes,
        )
        assertTrue(state.visibleItems.isNotEmpty())
        assertTrue(state.visibleItems.all { it.category == ClothingCategory.Shoes })
    }

    @Test
    fun moved_reordersItems() {
        val list = listOf("a", "b", "c", "d")
        assertEquals(listOf("b", "c", "a", "d"), list.moved(0, 2))
        assertEquals(listOf("d", "a", "b", "c"), list.moved(3, 0))
        assertEquals(list, list.moved(1, 1))
    }

    @Test
    fun moved_ignoresOutOfRangeIndices() {
        val list = listOf("a", "b")
        assertEquals(list, list.moved(0, 5))
        assertEquals(list, list.moved(-1, 1))
    }

    @Test
    fun updateOutfit_keepsReorderedItemsAndCreatedAt() = runBlocking {
        val original = repository.getOutfit("o1")
        val reversed = original.items.map { it.id }.reversed()
        val updated = repository.updateOutfit("o1", OutfitDraft(original.name, original.description, reversed))
        assertEquals(reversed, updated.items.map { it.id })
        assertEquals(original.createdAt, updated.createdAt)
    }

    @Test
    fun updateOutfit_allowsSwappingOneItem() = runBlocking {
        val updated = repository.updateOutfit("o1", OutfitDraft("Casual Weekend", "", listOf("w1", "w5", "w9")))
        assertEquals(listOf("w1", "w5", "w9"), updated.items.map { it.id })
    }

    private suspend fun assertRejected(block: suspend () -> Unit) {
        try {
            block()
            fail("Seharusnya ditolak")
        } catch (_: IllegalArgumentException) {
            // diharapkan
        }
    }
}
