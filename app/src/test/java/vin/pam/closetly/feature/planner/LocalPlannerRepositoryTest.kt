package vin.pam.closetly.feature.planner

import java.io.File
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import vin.pam.closetly.feature.planner.data.repository.LocalPlannerRepository
import vin.pam.closetly.feature.planner.model.PlannerOutfit

class LocalPlannerRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val outfit = PlannerOutfit("outfit-from-mix-and-match", "Casual Weekend", itemCount = 3)
    private fun file() = File(temporary.root, "plans.json")
    private suspend fun add(repository: LocalPlannerRepository, id: String = UUID.randomUUID().toString()) =
        repository.add(id, outfit, "Campus day", "2026-11-15", "Kuliah", "Bring a jacket", "Direncanakan")

    @Test
    fun createReadDelete_surviveRecreation_andRetriedCreateIsIdempotent() = runBlocking {
        val id = UUID.randomUUID().toString()
        val repository = LocalPlannerRepository(file(), "account-a")
        add(repository, id)
        val restored = LocalPlannerRepository(file(), "account-a")
        add(restored, id)
        val saved = restored.list().single()
        assertEquals(id, saved.id)
        assertEquals(outfit.id, saved.outfitId)
        assertEquals(outfit.name, saved.outfitName)
        restored.delete(id)
        restored.delete(id)
        assertTrue(LocalPlannerRepository(file(), "account-a").list().isEmpty())
    }

    @Test
    fun concurrentSaves_keepAllPlans_andDeletePreservesOtherRows() = runBlocking {
        val repository = LocalPlannerRepository(file(), "account-a")
        (1..20).map { async { add(repository) } }.awaitAll()
        assertEquals(20, repository.list().size)
        repository.delete(repository.list().first().id)
        assertEquals(19, LocalPlannerRepository(file(), "account-a").list().size)
    }

    @Test
    fun corruptFile_isNotOverwrittenByAnAttemptedSave() = runBlocking {
        file().writeText("corrupt snapshot")
        try {
            add(LocalPlannerRepository(file(), "account-a"))
            throw AssertionError("Expected storage decoding failure")
        } catch (_: SerializationException) { }
        assertEquals("corrupt snapshot", file().readText())
    }

    @Test
    fun anotherAccount_cannotReadOrDeleteExistingPlans() = runBlocking {
        val first = LocalPlannerRepository(file(), "account-a")
        val saved = add(first)
        val before = file().readText()
        try {
            LocalPlannerRepository(file(), "account-b").delete(saved.id)
            throw AssertionError("Expected account isolation")
        } catch (_: IllegalStateException) { }
        assertEquals(before, file().readText())
    }

    @Test
    fun invalidInput_andUnavailableStorage_doNotReportSuccess() = runBlocking {
        val repository = LocalPlannerRepository(file(), "account-a")
        try {
            repository.add(UUID.randomUUID().toString(), null, "Campus", "2026-11-15", "Kuliah", "", "Direncanakan")
            throw AssertionError("Expected outfit validation")
        } catch (_: IllegalArgumentException) { }
        assertFalse(file().exists())
        val blocked = File(temporary.root, "blocked").apply { writeText("file, not directory") }
        try {
            add(LocalPlannerRepository(File(blocked, "plans.json"), "account-a"))
            throw AssertionError("Expected storage write failure")
        } catch (_: IllegalStateException) { }
        assertEquals("file, not directory", blocked.readText())
    }
}
