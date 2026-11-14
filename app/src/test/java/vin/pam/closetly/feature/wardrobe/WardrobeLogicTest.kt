package vin.pam.closetly.feature.wardrobe

import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import vin.pam.closetly.feature.wardrobe.data.repository.LocalWardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingDraft
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.WardrobeFilter
import vin.pam.closetly.feature.wardrobe.model.matching
import vin.pam.closetly.feature.wardrobe.photo.WhiteBalance

class WardrobeLogicTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun file() = File(temporary.root, "wardrobe.json")
    private fun draft(name: String = "Kemeja linen") = ClothingDraft(
        name = name, category = WardrobeCategory.Top, color = "Putih", size = "M",
    )

    @Test
    fun createUpdateFavoriteArchiveRestore_surviveRepositoryRecreation() = runBlocking {
        val repository = LocalWardrobeRepository(file())
        val id = UUID.randomUUID().toString()
        repository.createItem(id, draft())
        repository.updateItem(id, draft("Kemeja linen baru"))
        repository.setFavorite(id, true)
        repository.archiveItem(id)

        val restored = LocalWardrobeRepository(file())
        restored.refresh()
        val item = restored.getItem(id)
        assertEquals("Kemeja linen baru", item.name)
        assertTrue(item.isFavorite)
        assertTrue(item.isArchived)
        assertTrue(restored.items.value.matching("", WardrobeFilter()).isEmpty())
        assertEquals(id, restored.items.value.matching("linen", WardrobeFilter(archivedOnly = true)).single().id)
        restored.restoreItem(id)
        assertFalse(restored.getItem(id).isArchived)
    }

    @Test
    fun retriedCreate_isIdempotent_andConcurrentWritesKeepEveryItem() = runBlocking {
        val repository = LocalWardrobeRepository(file())
        val id = UUID.randomUUID().toString()
        repository.createItem(id, draft())
        repository.createItem(id, draft())
        assertEquals(1, repository.items.value.size)
        (1..16).map { index ->
            async { repository.createItem(UUID.randomUUID().toString(), draft("Pakaian $index")) }
        }.awaitAll()
        val reloaded = LocalWardrobeRepository(file())
        reloaded.refresh()
        assertEquals(17, reloaded.items.value.size)
    }

    @Test
    fun invalidDraft_andCorruptStorage_doNotOverwriteSavedData() = runBlocking {
        val repository = LocalWardrobeRepository(file())
        val id = UUID.randomUUID().toString()
        repository.createItem(id, draft())
        val before = file().readText()
        try {
            repository.updateItem(id, draft(" "))
            throw AssertionError("Expected input validation")
        } catch (_: IllegalArgumentException) { }
        assertEquals(before, file().readText())
        Files.writeString(file().toPath(), "corrupt content")
        try {
            LocalWardrobeRepository(file()).refresh()
            throw AssertionError("Expected damaged storage to be reported")
        } catch (_: kotlinx.serialization.SerializationException) { }
        assertEquals("corrupt content", file().readText())
    }

    @Test
    fun filters_combineSearchCategoryColorAndFavorite() = runBlocking {
        val repository = LocalWardrobeRepository(file())
        val id = UUID.randomUUID().toString()
        repository.createItem(id, draft())
        repository.createItem(UUID.randomUUID().toString(), draft("Celana linen").copy(category = WardrobeCategory.Bottom))
        repository.setFavorite(id, true)
        val result = repository.items.value.matching("KEMEJA", WardrobeFilter(
            category = WardrobeCategory.Top, color = "putih", favoritesOnly = true,
        ))
        assertEquals(listOf(id), result.map { it.id })
    }

    @Test
    fun identityCalibration_preservesRgbAndAlpha() {
        listOf(0x00123456, 0x7f9acdee, 0xff000000.toInt(), 0xffffffff.toInt()).forEach { pixel ->
            assertEquals(pixel, WhiteBalance().apply(pixel))
        }
    }

    @Test
    fun calibration_neutralizesReferenceWithoutChangingAlpha() {
        val balance = WhiteBalance.fromNeutralSample(180, 150, 120)
        val output = balance.apply(0x7fb49678)
        val red = (output ushr 16) and 255
        val green = (output ushr 8) and 255
        val blue = output and 255
        assertTrue(maxOf(red, green, blue) - minOf(red, green, blue) <= 1)
        assertEquals(127, output ushr 24)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calibration_rejectsClippedReference() {
        WhiteBalance.fromNeutralSample(255, 255, 255)
    }
}
