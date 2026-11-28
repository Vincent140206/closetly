package vin.pam.closetly.feature.wearhistory.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// DTO untuk INSERT riwayat manual
@Serializable
data class WearHistoryInsertDto(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("outfit_id")
    val outfitId: String,

    @SerialName("date_worn")
    val dateWorn: String,

    val notes: String? = null,

    @SerialName("source_plan_id")
    val sourcePlanId: String? = null,

    @SerialName("is_worn")
    val isWorn: Boolean
)

// DTO untuk UPDATE riwayat manual
@Serializable
data class WearHistoryUpdateDto(
    @SerialName("outfit_id")
    val outfitId: String,

    @SerialName("date_worn")
    val dateWorn: String,

    val notes: String? = null
)

// DTO untuk membaca ID dari hasil query
@Serializable
data class WearHistoryIdDto(
    val id: String
)