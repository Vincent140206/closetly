package vin.pam.closetly.feature.wearhistory.data.repository

import java.time.LocalDate
import java.time.format.DateTimeParseException
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitPlanLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryDto
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.model.WearHistorySource

internal fun mapWearHistory(
    rows: List<WearHistoryDto>,
    outfitsById: Map<String, OutfitLookupDto>,
    plansById: Map<String, OutfitPlanLookupDto>,
    userId: String,
): List<WearHistory> = rows.mapNotNull { row ->
    check(row.userId == userId) { "Riwayat bukan milik akun ini." }
    // The supplied schema inventory allows null dates for pending Planner entries.
    if (!row.isWorn) return@mapNotNull null
    val date = try {
        LocalDate.parse(requireNotNull(row.dateWorn))
    } catch (error: Exception) {
        if (error !is DateTimeParseException && error !is IllegalArgumentException) throw error
        throw IllegalStateException("Tanggal riwayat belum valid. Muat ulang atau hubungi pengelola.", error)
    }
    val outfit = outfitsById[row.outfitId]
    val plan = row.sourcePlanId?.let(plansById::get)
    WearHistory(
        id = row.id, outfitId = row.outfitId, outfitName = outfit?.name ?: "Outfit tidak ditemukan",
        wornDate = date, activity = plan?.activity?.takeIf(String::isNotBlank) ?: "Manual",
        notes = row.notes.orEmpty(),
        imageUrl = outfit?.coverImagePath?.takeIf { it.startsWith("https://") || it.startsWith("http://") },
        source = if (row.sourcePlanId == null) WearHistorySource.MANUAL else WearHistorySource.PLANNER,
        plannerId = row.sourcePlanId,
    )
}.sortedWith(compareByDescending<WearHistory> { it.wornDate }.thenByDescending { it.id })
