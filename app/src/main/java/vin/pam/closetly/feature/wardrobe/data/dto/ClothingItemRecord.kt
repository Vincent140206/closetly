package vin.pam.closetly.feature.wardrobe.data.dto

import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.PhotoAdjustments
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory

/** Columns verified from the live schema and the additive wardrobe migration. */
@Serializable
data class ClothingItemRecord(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val category: String,
    val color: String,
    val size: String,
    val style: String = "",
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("original_image_path") val originalImagePath: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("archived_at") val archivedAt: String? = null,
    @SerialName("photo_adjustments") val photoAdjustments: PhotoAdjustments = PhotoAdjustments(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
) {
    /** UI and the photo editor receive private local cache files; database paths stay in this DTO. */
    fun toDomain(localImage: String?, localOriginal: String?): ClothingItem {
        val knownCategory = WardrobeCategory.entries.firstOrNull {
            it.key.equals(category.trim(), ignoreCase = true) || it.label.equals(category.trim(), ignoreCase = true)
        }
        return ClothingItem(
            id = id, name = name, category = knownCategory ?: WardrobeCategory.Other,
            color = color, size = size, style = style,
            imagePath = localImage, originalImagePath = localOriginal,
            photoAdjustments = photoAdjustments, isFavorite = isFavorite,
            isArchived = archivedAt != null,
            createdAt = OffsetDateTime.parse(createdAt).toInstant().toEpochMilli(),
            updatedAt = OffsetDateTime.parse(updatedAt).toInstant().toEpochMilli(),
            originalCategory = if (knownCategory == null) category else null,
        )
    }
}

@Serializable
data class ClothingItemInsert(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val category: String,
    val color: String,
    val size: String,
    val style: String,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("original_image_path") val originalImagePath: String? = null,
    @SerialName("photo_adjustments") val photoAdjustments: PhotoAdjustments = PhotoAdjustments(),
)
