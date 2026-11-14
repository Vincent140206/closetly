package vin.pam.closetly.feature.wardrobe

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import io.github.jan.supabase.SupabaseClient
import vin.pam.closetly.R
import vin.pam.closetly.feature.wardrobe.data.repository.LocalWardrobeRepository
import vin.pam.closetly.feature.wardrobe.data.repository.WardrobeRepository
import vin.pam.closetly.feature.wardrobe.data.repository.SupabaseWardrobeRepository
import vin.pam.closetly.feature.wardrobe.data.repository.SupabaseWardrobeDataSource
import vin.pam.closetly.feature.wardrobe.data.repository.UnavailableWardrobeRepository
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoProcessor
import vin.pam.closetly.feature.wardrobe.photo.WardrobePhotoStore
import vin.pam.closetly.feature.wardrobe.photo.WardrobeCloudPhotoFiles

/** Account-scoped bindings for the shared Supabase client and private image files. */
class WardrobeGraph private constructor(context: Context, accountId: String?, supabase: SupabaseClient?) {
    private val scope = MessageDigest.getInstance("SHA-256")
        .digest((accountId ?: "development").toByteArray()).joinToString("") { "%02x".format(it) }
    private val directory = File(context.filesDir, "wardrobe/$scope")

    val photoStore = WardrobePhotoStore(context.applicationContext, File(directory, "images"))
    val photoProcessor = WardrobePhotoProcessor(context.applicationContext)
    val repository: WardrobeRepository = when {
        accountId != null && supabase != null -> SupabaseWardrobeRepository(
            remote = SupabaseWardrobeDataSource(supabase),
            userId = accountId,
            images = WardrobeCloudPhotoFiles(File(directory, "remote-images"), photoStore),
        )
        accountId != null -> UnavailableWardrobeRepository("Konfigurasi Supabase belum tersedia untuk koleksi akun ini.")
        supabase != null -> UnavailableWardrobeRepository("Masuk ke akun untuk mengakses koleksi pakaian.")
        else -> LocalWardrobeRepository(
            file = File(directory, "collection.json"),
            initialItems = developmentItems(context),
        )
    }

    private fun developmentItems(context: Context): List<ClothingItem> {
        val now = System.currentTimeMillis()
        fun item(key: String, name: String, category: WardrobeCategory, color: String, size: String, image: Int, style: String = "Casual") =
            ClothingItem(
                id = UUID.nameUUIDFromBytes("wardrobe-demo-$key".toByteArray()).toString(),
                name = name, category = category, color = color, size = size, style = style,
                imagePath = "android.resource://${context.packageName}/drawable/${context.resources.getResourceEntryName(image)}",
                createdAt = now, updatedAt = now,
            )
        return listOf(
            item("denim", "Jaket denim biru", WardrobeCategory.Top, "Biru", "L", R.drawable.wardrobe_demo_denim),
            item("sneakers", "Sneakers putih", WardrobeCategory.Shoes, "Putih", "40", R.drawable.wardrobe_demo_sneakers),
            item("sweater", "Sweater rajut cokelat", WardrobeCategory.Top, "Cokelat", "M", R.drawable.wardrobe_demo_sweater),
            item("skirt", "Rok plisket krem", WardrobeCategory.Bottom, "Krem", "M", R.drawable.wardrobe_demo_skirt, "Formal"),
        )
    }

    companion object {
        private val accounts = ConcurrentHashMap<String, WardrobeGraph>()

        /** Retained ViewModels and newly created form destinations must share the same write lock. */
        fun forAccount(context: Context, accountId: String?, supabase: SupabaseClient? = null): WardrobeGraph {
            val application = context.applicationContext
            val key = "${application.filesDir.absolutePath}:${accountId ?: "development"}:${System.identityHashCode(supabase)}"
            return accounts.computeIfAbsent(key) { WardrobeGraph(application, accountId, supabase) }
        }
    }
}
