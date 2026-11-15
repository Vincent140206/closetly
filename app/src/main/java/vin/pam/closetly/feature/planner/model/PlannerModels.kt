package vin.pam.closetly.feature.planner.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Local Planner data. Supabase mapping will be added separately against the verified schema. */
@Serializable
data class PlannerRecord(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: String? = null,
    @SerialName("outfit_name") val outfitName: String = "",
    @SerialName("outfit_image_url") val outfitImageUrl: String? = null,
    @SerialName("activity_name") val activityName: String,
    @SerialName("planned_date") val plannedDate: String,
    val category: String = "Lainnya",
    val notes: String = "",
    val status: String = "Direncanakan",
)

data class PlannerOutfit(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val itemCount: Int = 0,
)
