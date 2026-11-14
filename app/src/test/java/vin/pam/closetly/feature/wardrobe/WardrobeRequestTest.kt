package vin.pam.closetly.feature.wardrobe

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRemoteException
import vin.pam.closetly.feature.wardrobe.data.repository.wardrobeRequest

class WardrobeRequestTest {
    @Test
    fun failedTransportAndDecoding_hideRawDetails_andRetainCause() = runBlocking {
        listOf(IOException("private/path"), SerializationException("raw response"), IllegalStateException("request headers")).forEach { cause ->
            try {
                wardrobeRequest { throw cause }
                throw AssertionError("Expected readable failure")
            } catch (error: WardrobeRemoteException) {
                // Coroutine stack recovery can copy an exception while retaining its original cause.
                assertTrue(generateSequence(error.cause) { it.cause }.any { it === cause })
                assertFalse(requireNotNull(error.message).contains(requireNotNull(cause.message)))
            }
        }
    }

    @Test
    fun timedOutRequest_becomesRetryableError() = runBlocking {
        try {
            wardrobeRequest(timeoutMillis = 20) { delay(10_000) }
            throw AssertionError("Expected timeout")
        } catch (error: WardrobeRemoteException) {
            assertTrue(requireNotNull(error.message).contains("coba lagi"))
        }
    }

    @Test
    fun navigationCancellation_isNotReportedAsNetworkFailure() = runBlocking {
        val cancellation = CancellationException("Destination left")
        try {
            wardrobeRequest { throw cancellation }
            throw AssertionError("Expected cancellation")
        } catch (error: CancellationException) {
            assertEquals(cancellation.message, error.message)
        }
    }
}
