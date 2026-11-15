package vin.pam.closetly.feature.wardrobe.photo

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeImageFiles

/** An account-private cache for immutable, versioned Storage objects. */
class WardrobeCloudPhotoFiles(
    private val directory: File,
    private val photoStore: WardrobePhotoStore,
) : WardrobeImageFiles {
    override fun cachedPath(objectPath: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(objectPath.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(directory, "$digest.png").absolutePath
    }

    override suspend fun cache(objectPath: String, download: suspend () -> ByteArray): String = withContext(Dispatchers.IO) {
        val target = File(cachedPath(objectPath))
        if (target.isFile && target.length() > 0) return@withContext target.absolutePath
        val bytes = download()
        require(bytes.isNotEmpty()) { "Foto dari Storage kosong." }
        directory.mkdirs()
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

    override suspend fun prepareUpload(localPath: String): ByteArray = photoStore.prepareUpload(localPath)
}
