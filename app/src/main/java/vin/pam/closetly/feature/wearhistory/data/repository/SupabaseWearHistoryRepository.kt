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
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryIdDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryInsertDto
import vin.pam.closetly.feature.wearhistory.data.dto.WearHistoryUpdateDto
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.model.SavedOutfit
import java.time.LocalDate


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

    //create
    override suspend fun insertManualHistory(
        userId: String,
        recordId: String,
        outfitId: String,
        wornDate: LocalDate,
        notes: String
    ) {
        writeRequest(userId) {
            ensureOutfitOwned(userId, outfitId)

            val payload = WearHistoryInsertDto(
                id = recordId,
                userId = userId,
                outfitId = outfitId,
                dateWorn = wornDate.toString(),
                notes = notes.trim().ifBlank { null },
                sourcePlanId = null,
                isWorn = true
            )

            val inserted = client.from("wear_history")
                .insert(payload) {
                    select(columns = Columns.raw("id"))
                }
                .decodeSingle<WearHistoryIdDto>()

            check(inserted.id == recordId) {
                "ID riwayat yang tersimpan tidak sesuai."
            }
        }
    }

    //update
    override suspend fun updateManualHistory(
        userId: String,
        recordId: String,
        outfitId: String,
        wornDate: LocalDate,
        notes: String
    ) {
        writeRequest(userId) {
            ensureOutfitOwned(userId, outfitId)

            val payload = WearHistoryUpdateDto(
                outfitId = outfitId,
                dateWorn = wornDate.toString(),
                notes = notes.trim().ifBlank { null }
            )

            val updated = client.from("wear_history")
                .update(payload) {
                    select(columns = Columns.raw("id"))

                    filter {
                        eq("id", recordId)
                        eq("user_id", userId)
                        eq("is_worn", true)
                        exact("source_plan_id", null)
                    }
                }
                .decodeSingleOrNull<WearHistoryIdDto>()

            check(updated != null) {
                "Riwayat manual tidak ditemukan atau tidak boleh diubah."
            }
        }
    }

    //delete
    override suspend fun deleteManualHistory(
        userId: String,
        recordId: String
    ) {
        writeRequest(userId) {
            val deleted = client.from("wear_history")
                .delete {
                    select(columns = Columns.raw("id"))

                    filter {
                        eq("id", recordId)
                        eq("user_id", userId)
                        eq("is_worn", true)
                        exact("source_plan_id", null)
                    }
                }
                .decodeSingleOrNull<WearHistoryIdDto>()

            check(deleted != null) {
                "Riwayat manual tidak ditemukan atau tidak boleh dihapus."
            }
        }
    }

    //validasi or periksa outfit pemilik akun
    private suspend fun ensureOutfitOwned(
        userId: String,
        outfitId: String
    ) {
        requireAccount(userId)

        val outfit = client.from("outfits")
            .select(columns = Columns.raw("id")) {
                filter {
                    eq("id", outfitId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<WearHistoryIdDto>()

        require(outfit != null) {
            "Outfit tidak ditemukan pada akun ini."
        }
    }

    //user req nulis
    private suspend fun writeRequest(
        userId: String,
        action: suspend () -> Unit
    ) {
        withTimeoutOrNull(30_000) {
            requireAccount(userId)
            action()
            requireAccount(userId)
        } ?: error(
            "Koneksi terlalu lama. Muat ulang riwayat untuk memeriksa hasil operasi."
        )
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
