package vin.pam.closetly.feature.wearhistory.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitPlanLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryDto
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.model.WearHistorySource
import java.time.LocalDate
import vin.pam.closetly.feature.wearhistory.model.SavedOutfit


class SupabaseWearHistoryRepository(
    private val client: SupabaseClient,
) : WearHistoryRepository {

    override suspend fun loadHistories(userId: String): List<WearHistory> {
        require(userId.isNotBlank()) { "Pengguna harus login terlebih dahulu" }

        val historyRows = client.from("wear_history")
            .select(columns = Columns.raw(
                "id,user_id,outfit_id,date_worn,notes,source_plan_id,created_at,updated_at"
            )) {
                filter { eq("user_id", userId) }
            }
            .decodeList<WearHistoryDto>()

        // Jika belum ada riwayat, jangan menjalankan query tambahan.
        if (historyRows.isEmpty()) return emptyList()

        val outfitsById = client.from("outfits")
            .select(columns = Columns.raw("id,name,cover_image_path")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<OutfitLookupDto>()
            .associateBy { it.id }

        val plansById = client.from("outfit_plans")
            .select(columns = Columns.raw("id,activity")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<OutfitPlanLookupDto>()
            .associateBy { it.id }

        return historyRows.map { row ->
            val outfit = outfitsById[row.outfitId]
            val plan = row.sourcePlanId?.let(plansById::get)
            val source = if (row.sourcePlanId == null) {
                WearHistorySource.MANUAL
            } else {
                WearHistorySource.PLANNER
            }
            WearHistory(
                id = row.id,
                outfitId = row.outfitId,
                outfitName = outfit?.name ?: "Outfit tidak ditemukan",
                wornDate = LocalDate.parse(row.dateWorn),
                activity = plan?.activity?.takeIf(String::isNotBlank) ?: "Manual",
                notes = row.notes.orEmpty(),
                // Gambar Supabase Storage membutuhkan nama bucket + kebijakan akses.
                // URL penuh langsung dikenali Coil; path relatif belum bisa dipakai.
                imageUrl = outfit?.coverImagePath?.takeIf {
                    it.startsWith("https://") || it.startsWith("http://")
                },
                source = source,
                plannerId = row.sourcePlanId,
            )
        }.sortedWith(compareByDescending<WearHistory> { it.wornDate }.thenByDescending { it.id })
    }


    override suspend fun loadSavedOutfits(
        userId: String
    ): List<SavedOutfit> {

        require(userId.isNotBlank()) {
            "Pengguna harus login terlebih dahulu"
        }

        val outfitRows = client.from("outfits")
            .select(
                columns = Columns.raw(
                    "id,name,description,cover_image_path"
                )
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<OutfitLookupDto>()

        return outfitRows.map { outfit ->
            SavedOutfit(
                id = outfit.id,
                name = outfit.name,
                description = outfit.description.orEmpty(),
                imageUrl = outfit.coverImagePath?.takeIf {
                    it.startsWith("https://") ||
                            it.startsWith("http://")
                }
            )
        }.sortedBy { it.name.lowercase() }
    }

}
