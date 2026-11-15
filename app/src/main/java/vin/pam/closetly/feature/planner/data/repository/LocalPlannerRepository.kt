package vin.pam.closetly.feature.planner.data.repository

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vin.pam.closetly.feature.planner.model.PlannerOutfit
import vin.pam.closetly.feature.planner.model.PlannerRecord

/** Local create/read/delete only. This store never calls Supabase. */
class LocalPlannerRepository(private val file: File, private val userId: String) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun list(): List<PlannerRecord> = mutex.withLock { read().sortedBy { it.plannedDate } }

    suspend fun add(
        requestId: String,
        outfit: PlannerOutfit?,
        name: String,
        date: String,
        category: String,
        notes: String,
        status: String,
    ): PlannerRecord = mutex.withLock {
        UUID.fromString(requestId)
        requireNotNull(outfit) { "Choose an outfit first." }
        require(name.trim().length in 1..160) { "Occasion must contain 1–160 characters." }
        LocalDate.parse(date)
        require(category.trim().length in 1..60) { "Occasion category must contain 1–60 characters." }
        require(notes.length <= 2000) { "Notes must be 2000 characters or fewer." }
        require(status in listOf("Direncanakan", "Sudah Dipakai")) { "Choose a valid outfit status." }
        val existing = read()
        existing.firstOrNull { it.id == requestId }?.let { return@withLock it }
        val plan = PlannerRecord(
            id = requestId, userId = userId, outfitId = outfit.id,
            outfitName = outfit.name, outfitImageUrl = outfit.imageUrl,
            activityName = name.trim(), plannedDate = date, category = category.trim(),
            notes = notes.trim(), status = status,
        )
        persist(existing + plan)
        plan
    }

    suspend fun delete(id: String) = mutex.withLock {
        val existing = read()
        val remaining = existing.filterNot { it.id == id }
        if (remaining.size != existing.size) persist(remaining)
    }

    private suspend fun read(): List<PlannerRecord> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        val records = json.decodeFromString<List<PlannerRecord>>(file.readText())
        check(records.all { it.userId == userId }) { "These plans belong to another account." }
        records
    }

    private suspend fun persist(records: List<PlannerRecord>) = withContext(Dispatchers.IO + NonCancellable) {
        val directory = requireNotNull(file.parentFile)
        check(directory.isDirectory || directory.mkdirs()) { "Couldn't open plan storage. Check available space." }
        val temporary = File.createTempFile("plans-", ".tmp", directory)
        try {
            FileOutputStream(temporary).use {
                it.write(json.encodeToString(records).toByteArray(Charsets.UTF_8))
                it.fd.sync()
            }
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            temporary.delete()
        }
    }
}
