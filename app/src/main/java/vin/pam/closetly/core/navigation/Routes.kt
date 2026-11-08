package vin.pam.closetly.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object OnboardingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object ProfileRoute

@Serializable
data object WardrobeRoute

@Serializable
data object OutfitRoute

@Serializable
data class OutfitDetailRoute(val outfitId: String)

@Serializable
data class OutfitFormRoute(val outfitId: String? = null)

@Serializable
data object PlannerRoute

@Serializable
data object InspirationRoute

@Serializable
data object WishlistRoute

@Serializable
data object WearHistoryRoute
