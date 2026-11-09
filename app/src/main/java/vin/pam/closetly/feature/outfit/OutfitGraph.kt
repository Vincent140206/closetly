package vin.pam.closetly.feature.outfit

import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.data.repository.OutfitRepository

/**
 * Factory lokal fitur (sesuai REPOSITORY_CONTRACTS: jangan daftarkan ke AppContainer
 * sebelum benar-benar menjadi dependency lintas fitur).
 *
 * Satu instance dibagi list dan form supaya data dummy konsisten.
 * Saat Supabase siap: ubah menjadi
 *   SupabaseOutfitRepository(container.supabaseClient)   // fallback ke Unavailable bila null
 */
internal object OutfitGraph {
    val repository: OutfitRepository by lazy { FakeOutfitRepository() }
}
