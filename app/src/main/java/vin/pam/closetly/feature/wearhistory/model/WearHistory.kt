package vin.pam.closetly.feature.wearhistory.model

import java.time.LocalDate

enum class WearHistorySource { MANUAL, PLANNER }

data class WearHistory(
    val id: String,
    val outfitId: String,
    val outfitName: String,
    val wornDate: LocalDate,
    val activity: String,
    val notes: String = "",
    val imageUrl: String? = null,
    val source: WearHistorySource = WearHistorySource.MANUAL,
    val plannerId: String? = null,
)