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
data class WardrobeDetailRoute(val itemId: String)

@Serializable
data class WardrobeFormRoute(val itemId: String? = null)

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

@Serializable data class InspirationDetailRoute(val postId: String)
@Serializable data object MyInspirationRoute
@Serializable data class InspirationCollectionRoute(val collectionId: String)
@Serializable data object ManageCollectionsRoute

@Serializable
data object WishlistRoute

@Serializable
data object WearHistoryRoute
