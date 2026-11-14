package vin.pam.closetly

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoProcessor
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoStore
import vin.pam.closetly.feature.wardrobe.photo.WhiteBalance

class WardrobePhotoTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun cameraUri_isWritable_andImportHonorsExifOrientation() = runBlocking {
        val store = WardrobePhotoStore(context, File(context.cacheDir, "wardrobe-test/${UUID.randomUUID()}/images"))
        val uri = store.createCameraUri()
        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff987654.toInt()) }
        context.contentResolver.openOutputStream(uri).use { output ->
            requireNotNull(output)
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output))
        }
        context.contentResolver.openFileDescriptor(uri, "rw").use { descriptor ->
            val exif = ExifInterface(requireNotNull(descriptor).fileDescriptor)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            exif.saveAttributes()
        }
        val imported = store.importPhoto(uri)
        val result = store.load(imported)
        assertEquals(480, result.width)
        assertEquals(640, result.height)
        val saved = store.persist(imported, result)
        assertTrue(File(saved.imagePath).exists())
        assertTrue(File(saved.originalPath).exists())
        assertEquals(480, store.load(saved.imagePath).width)
    }

    @Test
    fun uploadPreparation_fitsBucketLimit_andRetainsTransparency() = runBlocking {
        // Android may evict cache files immediately on a device with little free storage.
        val directory = File(context.filesDir, "wardrobe-upload-test/${UUID.randomUUID()}").apply { mkdirs() }
        val store = WardrobePhotoStore(context, File(directory, "images"))
        val side = 1536
        val random = java.util.Random(42)
        val pixels = IntArray(side * side) { 0xff000000.toInt() or random.nextInt(0x1000000) }
        // Broad transparent and opaque areas survive any resize needed for the bucket limit.
        for (y in 0 until 64) for (x in 0 until 64) pixels[y * side + x] = 0
        val bitmap = Bitmap.createBitmap(pixels, side, side, Bitmap.Config.ARGB_8888)
        try {
            val input = File(directory, "source.png")
            input.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            assertTrue("Fixture must exercise resizing", input.length() > WardrobePhotoStore.MAX_UPLOAD_BYTES)
            val saved = store.persist(input.absolutePath, bitmap)
            val bytes = store.prepareUpload(saved.imagePath)
            assertTrue(bytes.size <= WardrobePhotoStore.MAX_UPLOAD_BYTES)
            assertEquals(0x89, bytes[0].toInt() and 255)
            val result = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            assertTrue(result.width < side)
            assertEquals(0, result.getPixel(1, 1) ushr 24)
            assertEquals(255, result.getPixel(result.width / 2, result.height / 2) ushr 24)
            result.recycle()
        } finally {
            bitmap.recycle()
            directory.deleteRecursively()
        }
        Unit
    }

    @Test
    fun maskChangesTransparency_andPreservesOpaqueGarmentColor() = runBlocking {
        val source = Bitmap.createBitmap(intArrayOf(0xff345678.toInt(), 0xff987654.toInt()), 2, 1, Bitmap.Config.ARGB_8888)
        val output = WardrobePhotoProcessor(context).render(source, WhiteBalance(), floatArrayOf(0f, 1f))
        assertEquals(0, output.getPixel(0, 0) ushr 24)
        assertEquals(source.getPixel(1, 0), output.getPixel(1, 0))
    }

    /** Device smoke test. Requires Google Play services and network for the first model download. */
    @Test
    fun subjectSegmentation_cutsOutReferenceGarment() = runBlocking {
        assumeTrue(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)
        val source = BitmapFactory.decodeResource(context.resources, R.drawable.wardrobe_demo_denim)
        val processor = WardrobePhotoProcessor(context)
        val mask = withTimeout(150_000) { processor.foregroundMask(source) }
        val output = processor.render(source, WhiteBalance(), mask)
        val pixels = IntArray(output.width * output.height)
        output.getPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
        assertTrue(pixels.any { (it ushr 24) == 0 })
        assertTrue(pixels.any { (it ushr 24) > 240 })
        File(context.cacheDir, "wardrobe-cutout.png").outputStream().use {
            output.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        Unit
    }
}
