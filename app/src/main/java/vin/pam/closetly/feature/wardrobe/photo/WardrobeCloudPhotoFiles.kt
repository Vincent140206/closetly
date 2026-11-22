package vin.pam.closetly.feature.wardrobe.photo

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeImageFiles

/** An account-private cache for immutable, versioned Storage objects. */
class WardrobeCloudPhotoFiles(
    private val directory: File,
    private val uploadBytes: suspend (String) -> ByteArray,
) : WardrobeImageFiles {
    private val downloads = ConcurrentHashMap<String, Mutex>()

    override fun cachedPath(objectPath: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(objectPath.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(directory, "$digest.png").absolutePath
    }

    override fun existingPath(objectPath: String): String? = File(cachedPath(objectPath))
        .takeIf { it.isFile && it.length() in 1..MAX_IMAGE_BYTES }?.absolutePath

    override suspend fun cache(objectPath: String, download: suspend () -> ByteArray): String = withContext(Dispatchers.IO) {
        downloads.computeIfAbsent(objectPath) { Mutex() }.withLock {
            existingPath(objectPath)?.let { return@withLock it }
            val target = File(cachedPath(objectPath))
            val bytes = download()
            require(bytes.isNotEmpty()) { "Foto dari Storage kosong." }
            require(bytes.size <= MAX_IMAGE_BYTES) { "Foto maksimal 5 MB per file." }
            check(directory.isDirectory || directory.mkdirs()) { "Cache foto belum dapat disiapkan." }
            val temporary = File.createTempFile("download-", ".tmp", directory)
            try {
                FileOutputStream(temporary).use {
                    it.write(bytes)
                    it.fd.sync()
                }
                Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                temporary.delete()
            }
            target.absolutePath
        }
    }

    override suspend fun prepareUpload(localPath: String): ByteArray = uploadBytes(localPath)

    companion object {
        private const val MAX_IMAGE_BYTES = 5L * 1024 * 1024
    }
}
