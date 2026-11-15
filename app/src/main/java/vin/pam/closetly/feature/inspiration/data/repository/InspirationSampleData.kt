package vin.pam.closetly.feature.inspiration.data.repository

import vin.pam.closetly.feature.inspiration.model.InspirationCollection
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.OutfitStyle
import vin.pam.closetly.feature.inspiration.model.PlaceholderArt
import vin.pam.closetly.feature.inspiration.model.SavedPost

/** Sample content for development. Replace with Supabase data when the backend is ready. */
object InspirationSampleData {

    private val now = System.currentTimeMillis()
    private const val DAY = 86_400_000L

    private fun post(
        n: Int, author: String, name: String, title: String, caption: String, tags: List<String>,
        height: Int, outfit: OutfitStyle, bg: Long, top: Long, bottom: Long, skin: Long, hat: Long? = null,
    ) = InspirationPost(
        id = "post-$n", authorId = "user-$author", authorName = name, authorAvatarPath = null,
        title = title, caption = caption, tags = tags, likeCount = 1000 + n * 137,
        imagePath = "user-$author/post-$n.jpg", createdAt = now - n * DAY,
        art = PlaceholderArt(100f / height, outfit, bg, top, bottom, skin, hat),
    )

    val posts: List<InspirationPost> = listOf(
        post(1, "elena", "Elena S.", "Trench & Denim", "Trench camel dipadukan jeans biru muda dan sneakers putih. Cocok untuk hari berangin.",
            listOf("OOTD", "Casual"), 150, OutfitStyle.Coat, 0xFFF3E3CF, 0xFFD9A769, 0xFF9EC3DB, 0xFFE8C3A0),
        post(2, "marcus", "Marcus K.", "Business Casual", "Blazer charcoal, kaos putih polos, dan celana slim. Rapi tanpa terlihat kaku.",
            listOf("Formal Look", "Minimalist"), 165, OutfitStyle.Suit, 0xFFDDE2E0, 0xFF3E4549, 0xFF2B2F32, 0xFFD9AE8A),
        post(3, "sarah", "Sarah F.", "Summer Linen Dress", "Dress linen krem dengan topi jerami lebar untuk jalan santai di akhir pekan.",
            listOf("OOTD", "Casual"), 140, OutfitStyle.Dress, 0xFFCFE3E8, 0xFFF4EAD2, 0xFFF4EAD2, 0xFFE3B592, 0xFFE2B34C),
        post(4, "james", "James W.", "Navy Smart Suit", "Setelan navy dengan kemeja putih. Pilihan aman untuk rapat penting.",
            listOf("Formal Look"), 160, OutfitStyle.Suit, 0xFFE9E6DD, 0xFF26364F, 0xFF26364F, 0xFFC99B7A),
        post(5, "dina", "Dina A.", "Neutral Tailoring", "Setelan blazer dan celana palazzo krem. Satu palet warna, tampil rapi dan tenang.",
            listOf("Formal Look", "Minimalist"), 155, OutfitStyle.Suit, 0xFFEFE9DD, 0xFFC9B08A, 0xFFC9B08A, 0xFFE6BE9B),
        post(6, "rafi", "Rafi P.", "Denim on Denim", "Jaket denim di atas kaos putih. Klasik yang tidak pernah salah.",
            listOf("Casual", "OOTD"), 135, OutfitStyle.Tee, 0xFFE6EAEE, 0xFF6C95B8, 0xFF3C5878, 0xFFD2A07C),
        post(7, "maya", "Maya L.", "All Black Evening", "Gaun hitam sederhana dengan aksesori emas. Elegan untuk makan malam.",
            listOf("Minimalist", "Formal Look"), 150, OutfitStyle.Dress, 0xFFD8D8D6, 0xFF1C1C1C, 0xFF1C1C1C, 0xFFD8AE8C),
        post(8, "bayu", "Bayu N.", "Sunday Coffee Run", "Hoodie abu, celana chino, dan sneakers. Nyaman untuk seharian.",
            listOf("OOTD", "Casual"), 145, OutfitStyle.Tee, 0xFFFDE9B0, 0xFF8E949B, 0xFFB59B72, 0xFFD4A582),
        post(9, "luna", "Luna H.", "Sunset Boho", "Dress oranye dengan topi cokelat. Pas untuk liburan pantai.",
            listOf("OOTD"), 160, OutfitStyle.Dress, 0xFFF7D5B5, 0xFFE9792F, 0xFFE9792F, 0xFFE0B08C, 0xFF7A4A2A),
        post(10, "marcus", "Marcus K.", "Grey Flannel Day", "Setelan flanel abu dengan dasi rajut. Hangat dan tegas.",
            listOf("Formal Look"), 150, OutfitStyle.Suit, 0xFFE8EBEA, 0xFF7D838A, 0xFF6A7076, 0xFFD6AD8C),
        post(11, "elena", "Elena S.", "Sage Weekend", "Cardigan hijau sage dengan celana putih tulang. Tenang dan lembut.",
            listOf("Casual", "Minimalist"), 140, OutfitStyle.Coat, 0xFFEDEFE2, 0xFF9DB394, 0xFFF3EFE4, 0xFFE5BD9A),
        post(12, "bayu", "Bayu N.", "Rust Tee, Black Pants", "Kaos terakota dengan celana hitam lurus. Warna hangat untuk kantor santai.",
            listOf("OOTD", "Minimalist"), 148, OutfitStyle.Tee, 0xFFF1E1D3, 0xFFB5593A, 0xFF222222, 0xFFD1A07B),
    )

    val collections: List<InspirationCollection> = listOf(
        InspirationCollection("col-ootd", "OOTD Harian"),
        InspirationCollection("col-work", "Kerja/Formal"),
        InspirationCollection("col-date", "Kencan Malam"),
        InspirationCollection("col-holiday", "Liburan"), // intentionally empty
    )

    /** (postNumber, collectionId, daysAgo) */
    fun seedSaved(now: Long): List<SavedPost> {
        val day = 86_400_000L
        return listOf(
            Triple(1, "col-ootd", 9), Triple(2, "col-ootd", 8), Triple(3, "col-ootd", 7),
            Triple(9, "col-ootd", 6), Triple(4, "col-work", 5), Triple(5, "col-work", 4),
            Triple(7, "col-date", 3), Triple(6, "col-ootd", 2), Triple(12, "col-ootd", 1),
        ).map { (n, col, ago) -> SavedPost("post-$n", col, now - ago * day) }
    }
}