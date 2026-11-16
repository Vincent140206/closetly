package vin.pam.closetly.feature.wearhistory

import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitPlanLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryDto
import vin.pam.closetly.feature.wearhistory.data.repository.mapWearHistory
import vin.pam.closetly.feature.wearhistory.model.WearHistorySource

class WearHistoryMappingTest {
    private val owner = "account-a"

    @Test fun pendingDateCanBeNull_andOnlyWornRowsAppearInCalendar() {
        val pending = Json.decodeFromString<WearHistoryDto>(
            """{"id":"pending","user_id":"$owner","outfit_id":"outfit","date_worn":null,"is_worn":false,"source_plan_id":"plan"}""",
        )
        val worn = pending.copy(id = "worn", dateWorn = "2026-11-16", isWorn = true)
        val result = mapWearHistory(listOf(pending, worn),
            mapOf("outfit" to OutfitLookupDto("outfit", "Office outfit", coverImagePath = "$owner/outfit/private.png")),
            mapOf("plan" to OutfitPlanLookupDto("plan", "Work")), owner)
        assertEquals(1, result.size)
        assertEquals("Office outfit", result.single().outfitName)
        assertEquals(LocalDate.of(2026, 11, 16), result.single().wornDate)
        assertEquals("Work", result.single().activity)
        assertEquals(WearHistorySource.PLANNER, result.single().source)
        assertNull(result.single().imageUrl)
    }

    @Test fun invalidCompletedDate_hasReadableErrorInsteadOfCalendarCrash() {
        for (date in listOf(null, "bad-date")) {
            try {
                mapWearHistory(listOf(WearHistoryDto("item", owner, "outfit", date, isWorn = true)), emptyMap(), emptyMap(), owner)
                fail("Expected invalid completed date")
            } catch (error: IllegalStateException) {
                assertTrue(error.message.orEmpty().contains("Tanggal riwayat"))
                assertFalse(error.message.orEmpty().contains("bad-date"))
            }
        }
    }

    @Test fun foreignAccountRow_isRejected() {
        try {
            mapWearHistory(listOf(WearHistoryDto("item", "account-b", "outfit", "2026-11-16", isWorn = true)), emptyMap(), emptyMap(), owner)
            fail("Expected owner guard")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("akun"))
        }
    }
}
