package vin.pam.closetly.feature.wardrobe.photo

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class WardrobePhotoProcessor(private val context: Context) {
    suspend fun foregroundMask(original: Bitmap): FloatArray = withTimeout(120_000) {
        val segmenter = SubjectSegmentation.getClient(
            SubjectSegmenterOptions.Builder().enableForegroundConfidenceMask().build(),
        )
        try {
            ensureModelInstalled(segmenter)
            val result = segmenter.process(InputImage.fromBitmap(original, 0)).awaitResult()
            val buffer = requireNotNull(result.foregroundConfidenceMask) { "Objek pakaian belum terdeteksi." }
            val mask = FloatArray(original.width * original.height)
            buffer.rewind()
            buffer.get(mask)
            require(mask.any { it > 0.7f } && mask.any { it < 0.3f }) {
                "Objek belum terpisah jelas dari background. Coba foto dengan latar yang lebih sederhana."
            }
            mask
        } finally {
            segmenter.close()
        }
    }

    suspend fun render(original: Bitmap, balance: WhiteBalance, mask: FloatArray?): Bitmap =
        withContext(Dispatchers.Default) {
            val pixels = IntArray(original.width * original.height)
            original.getPixels(pixels, 0, original.width, 0, 0, original.width, original.height)
            val needsCalibration = balance != WhiteBalance()
            for (index in pixels.indices) {
                if (index % original.width == 0) ensureActive()
                val calibrated = if (needsCalibration) balance.apply(pixels[index]) else pixels[index]
                val alpha = ((calibrated ushr 24) * (mask?.get(index)?.coerceIn(0f, 1f) ?: 1f)).toInt()
                pixels[index] = (calibrated and 0x00ffffff) or (alpha shl 24)
            }
            Bitmap.createBitmap(pixels, original.width, original.height, Bitmap.Config.ARGB_8888)
        }

    private suspend fun ensureModelInstalled(segmenter: SubjectSegmenter) = withTimeout(90_000) {
        val client = ModuleInstall.getClient(context)
        if (client.areModulesAvailable(segmenter).awaitResult().areModulesAvailable()) return@withTimeout
        suspendCancellableCoroutine<Unit> { continuation ->
            lateinit var listener: InstallStatusListener
            listener = InstallStatusListener { update ->
                when (update.installState) {
                    InstallState.STATE_COMPLETED -> {
                        client.unregisterListener(listener)
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    InstallState.STATE_FAILED, InstallState.STATE_CANCELED -> {
                        client.unregisterListener(listener)
                        if (continuation.isActive) continuation.resumeWithException(
                            IllegalStateException("Model penghapus background belum siap. Hubungkan internet lalu coba lagi."),
                        )
                    }
                }
            }
            continuation.invokeOnCancellation { client.unregisterListener(listener) }
            val request = ModuleInstallRequest.newBuilder().addApi(segmenter).setListener(listener).build()
            client.installModules(request).addOnSuccessListener { response ->
                if (response.areModulesAlreadyInstalled()) {
                    client.unregisterListener(listener)
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }.addOnFailureListener { error ->
                client.unregisterListener(listener)
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
    addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
    addOnCanceledListener {
        if (continuation.isActive) continuation.resumeWithException(
            IllegalStateException("Pemrosesan foto terhenti. Silakan coba lagi."),
        )
    }
}
