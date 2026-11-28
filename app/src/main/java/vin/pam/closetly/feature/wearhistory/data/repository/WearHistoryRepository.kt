package vin.pam.closetly.feature.wearhistory.data.repository

import vin.pam.closetly.feature.wearhistory.model.SavedOutfit
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import java.time.LocalDate


interface WearHistoryRepository {

    // READ riwayat pemakaian
    suspend fun loadHistories(
        userId: String
    ): List<WearHistory>

    // READ daftar outfit tersimpan
    suspend fun loadSavedOutfits(
        userId: String
    ): List<SavedOutfit>

    suspend fun insertManualHistory(
        userId: String,
        recordId: String,
        outfitId: String,
        wornDate: LocalDate,
        notes: String
    )

    suspend fun updateManualHistory(
        userId: String,
        recordId: String,
        outfitId: String,
        wornDate: LocalDate,
        notes: String
    )

    suspend fun deleteManualHistory(
        userId: String,
        recordId: String
    )
}