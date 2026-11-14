package vin.pam.closetly.feature.wardrobe.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SavedWardrobePhoto(val originalPath: String, val imagePath: String)

class WardrobePhotoStore(private val context: Context, private val directory: File) {
    private val staging = File(context.cacheDir, "wardrobe-edit/${directory.parentFile?.name}")

    fun createCameraUri(): Uri {
        val cameraDirectory = File(context.cacheDir, "wardrobe-capture").apply { mkdirs() }
        val image = File.createTempFile("capture-", ".jpg", cameraDirectory)
        return FileProvider.getUriForFile(context, "${context.packageName}.wardrobe.photos", image)
    }

    suspend fun importPhoto(uri: Uri): String = withContext(Dispatchers.IO) {
        staging.mkdirs()
        val input = File.createTempFile("input-", ".image", staging)
        try {
            context.contentResolver.openInputStream(uri).use { source ->
                requireNotNull(source) { "Foto tidak dapat dibuka. Pilih ulang dari galeri." }
                input.outputStream().use { destination ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 30 * 1024 * 1024) { "Ukuran foto maksimal 30 MB." }
                        destination.write(buffer, 0, count)
                    }
                }
            }
            val bitmap = decodeFile(input)
            val normalized = File(staging, "original-${UUID.randomUUID()}.png")
            writePng(bitmap, normalized)
            bitmap.recycle()
            normalized.absolutePath
        } finally {
            input.delete()
        }
    }

    suspend fun load(path: String): Bitmap = withContext(Dispatchers.IO) {
        if (path.startsWith("android.resource://")) {
            val imported = importPhoto(path.toUri())
            decodeFile(File(imported))
        } else {
            decodeFile(File(path))
        }
    }

    /** Files become durable before a repository write; old files remain for existing outfit references. */
    suspend fun persist(originalPath: String, result: Bitmap): SavedWardrobePhoto = withContext(Dispatchers.IO) {
        directory.mkdirs()
        val id = UUID.randomUUID().toString()
        val original = File(directory, "$id-original.png")
        val processed = File(directory, "$id.png")
        try {
            File(originalPath).copyTo(original)
            writePng(result, processed)
            SavedWardrobePhoto(original.absolutePath, processed.absolutePath)
        } catch (error: Exception) {
            original.delete()
            processed.delete()
            throw error
        }
    }

    private fun decodeFile(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Format foto tidak didukung atau file rusak." }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1536) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IllegalArgumentException("Foto tidak dapat dibaca.")
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        }
        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    private fun writePng(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Foto gagal disimpan." }
            stream.fd.sync()
        }
    }
}
