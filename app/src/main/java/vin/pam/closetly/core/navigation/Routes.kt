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
