package vin.pam.closetly.feature.wearhistory.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WearHistoryDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: String,
    @SerialName("date_worn") val dateWorn: String,
    val notes: String? = null,
    @SerialName("source_plan_id") val sourcePlanId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

/** Hanya kolom yang diminta dari public.outfits. */
@Serializable
data class OutfitLookupDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("cover_image_path") val coverImagePath: String? = null,
)

/** Hanya kolom yang diminta dari public.outfit_plans. */
@Serializable
data class OutfitPlanLookupDto(
    val id: String,
    val activity: String? = null,
)
