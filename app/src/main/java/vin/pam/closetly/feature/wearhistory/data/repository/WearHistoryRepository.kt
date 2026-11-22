package vin.pam.closetly.feature.wearhistory.data.repository

import vin.pam.closetly.feature.wearhistory.model.SavedOutfit
import vin.pam.closetly.feature.wearhistory.model.WearHistory


interface WearHistoryRepository {

    // READ riwayat pemakaian
    suspend fun loadHistories(
        userId: String
    ): List<WearHistory>

    // READ daftar outfit tersimpan
    suspend fun loadSavedOutfits(
        userId: String
    ): List<SavedOutfit>
}