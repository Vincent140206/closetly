package vin.pam.closetly.feature.wearhistory.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.OutfitPlanLookupDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryDto
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.model.SavedOutfit


class SupabaseWearHistoryRepository(
    private val client: SupabaseClient,
) : WearHistoryRepository {

    override suspend fun loadHistories(userId: String): List<WearHistory> = accountRequest(userId) {
        require(userId.isNotBlank()) { "Pengguna harus login terlebih dahulu" }

        val historyRows = client.from("wear_history")
            .select(columns = Columns.raw(
                "id,user_id,outfit_id,date_worn,notes,source_plan_id,created_at,updated_at,is_worn"
            )) {
                filter { eq("user_id", userId); eq("is_worn", true) }
            }
            .decodeList<WearHistoryDto>()

        // Jika belum ada riwayat, jangan menjalankan query tambahan.
        if (historyRows.isEmpty()) return@accountRequest emptyList()

        requireAccount(userId)
        val outfitsById = client.from("outfits")
            .select(columns = Columns.raw("id,name,cover_image_path")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<OutfitLookupDto>()
            .associateBy { it.id }

        requireAccount(userId)
        val plansById = if (historyRows.none { it.sourcePlanId != null }) emptyMap() else client.from("outfit_plans")
            .select(columns = Columns.raw("id,activity")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<OutfitPlanLookupDto>()
            .associateBy { it.id }

        mapWearHistory(historyRows, outfitsById, plansById, userId)
    }


    override suspend fun loadSavedOutfits(
        userId: String
    ): List<SavedOutfit> = accountRequest(userId) {

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

        outfitRows.map { outfit ->
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

    private fun requireAccount(userId: String) {
        check(client.auth.currentUserOrNull()?.id == userId) { "Sesi berubah. Masuk kembali untuk mengakses riwayat." }
    }

    private suspend fun <T : Any> accountRequest(userId: String, block: suspend () -> T): T =
        withTimeoutOrNull(30_000) {
            requireAccount(userId)
            try {
                block().also { requireAccount(userId) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                requireAccount(userId)
                throw IllegalStateException("Riwayat belum dapat dimuat. Periksa koneksi lalu coba lagi.", error)
            }
        } ?: error("Koneksi terlalu lama merespons. Periksa internet lalu coba lagi.")
}
