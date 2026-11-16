package vin.pam.closetly.feature.wearhistory.model

data class SavedOutfit(
    val id: String,
    val name: String,
    val description: String = "",
    val imageUrl: String? = null
)