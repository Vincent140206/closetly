package vin.pam.closetly.feature.wardrobe

import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import vin.pam.closetly.feature.wardrobe.photo.WardrobeCloudPhotoFiles

class WardrobeCloudPhotoFilesTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun concurrentReaders_shareDownload_andEvictionCanRefetch() = runBlocking {
        val files = WardrobeCloudPhotoFiles(temporary.newFolder()) { byteArrayOf(9) }
        val downloads = AtomicInteger()
        val paths = (1..20).map { async {
            files.cache("owner/wardrobe/photo.png") { downloads.incrementAndGet(); delay(10); byteArrayOf(1) }
        } }.awaitAll()
        assertEquals(1, downloads.get())
        assertEquals(1, paths.distinct().size)
        assertEquals(paths.first(), files.existingPath("owner/wardrobe/photo.png"))
        File(paths.first()).delete()
        assertNull(files.existingPath("owner/wardrobe/photo.png"))
        files.cache("owner/wardrobe/photo.png") { downloads.incrementAndGet(); byteArrayOf(2) }
        assertEquals(2, downloads.get())
    }

    @Test fun failedOrOversizedDownload_leavesNoPartialFile_andCanRetry() = runBlocking {
        val directory = temporary.newFolder()
        val files = WardrobeCloudPhotoFiles(directory) { byteArrayOf(1) }
        try { files.cache("photo") { throw IOException("offline") }; fail("Expected failure") }
        catch (_: IOException) { }
        try { files.cache("photo") { ByteArray(5 * 1024 * 1024 + 1) }; fail("Expected size limit") }
        catch (_: IllegalArgumentException) { }
        assertTrue(directory.listFiles().orEmpty().isEmpty())
        val path = files.cache("photo") { byteArrayOf(7) }
        assertArrayEquals(byteArrayOf(7), File(path).readBytes())
    }
}
