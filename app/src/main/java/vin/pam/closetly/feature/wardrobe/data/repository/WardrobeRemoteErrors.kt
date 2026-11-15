package vin.pam.closetly.feature.wardrobe.data.repository

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException

/** Keep transport details out of the UI; callers can retry explicitly. */
internal class WardrobeRemoteException(message: String, cause: Throwable) : IllegalStateException(message, cause)

internal suspend fun <T> wardrobeRequest(timeoutMillis: Long = 30_000, block: suspend () -> T): T = try {
    withTimeout(timeoutMillis) { block() }
} catch (error: TimeoutCancellationException) {
    throw WardrobeRemoteException("Koneksi terlalu lama merespons. Periksa internet lalu coba lagi.", error)
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    val message = when (error) {
        is RestException -> wardrobeHttpErrorMessage(error.statusCode)
        is SerializationException -> "Data koleksi belum dapat dibaca. Perbarui aplikasi atau hubungi pengelola."
        is SocketTimeoutException, is HttpRequestTimeoutException -> "Koneksi terlalu lama merespons. Periksa internet lalu coba lagi."
        is IOException -> "Tidak dapat terhubung. Periksa koneksi internet lalu coba lagi."
        else -> "Permintaan koleksi belum berhasil. Coba lagi."
    }
    throw WardrobeRemoteException(message, error)
}

internal fun wardrobeHttpErrorMessage(status: Int): String = when (status) {
    401 -> "Sesi login tidak berlaku. Masuk kembali untuk mengakses koleksi."
    403 -> "Akun ini belum memiliki akses ke koleksi atau foto. Hubungi pengelola jika masalah berlanjut."
    404 -> "Pakaian atau foto tidak ditemukan. Muat ulang koleksi."
    409 -> "Data tidak dapat disimpan karena konflik. Muat ulang koleksi lalu coba lagi."
    413 -> "Foto terlalu besar. Pilih ulang foto dengan ukuran maksimal 5 MB."
    429 -> "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi."
    400, 422 -> "Data pakaian atau foto ditolak. Periksa isian lalu coba lagi."
    in 500..599 -> "Layanan koleksi sedang bermasalah. Coba lagi beberapa saat."
    else -> "Permintaan koleksi belum berhasil. Coba lagi."
}
