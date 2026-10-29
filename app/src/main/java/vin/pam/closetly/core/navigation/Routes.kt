package vin.pam.closetly.core.navigation

import kotlinx.serialization.Serializable

@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object HomeRoute
@Serializable data object ProfileRoute
@Serializable data object EditProfileRoute

@Serializable data object WardrobeRoute
@Serializable data class ClothingDetailRoute(val clothingId: String)
@Serializable data class ClothingFormRoute(val clothingId: String? = null)

@Serializable data object OutfitCollectionRoute
@Serializable data class OutfitDetailRoute(val outfitId: String)
@Serializable data class OutfitFormRoute(val outfitId: String? = null)

@Serializable data object PlannerRoute
@Serializable data class PlanDetailRoute(val planId: String)
@Serializable data class PlanFormRoute(val planId: String? = null)

@Serializable data object InspirationRoute
@Serializable data class InspirationDetailRoute(val inspirationId: String)
@Serializable data class InspirationFormRoute(val inspirationId: String? = null)

@Serializable data object WishlistRoute
@Serializable data class WishlistDetailRoute(val wishlistId: String)
@Serializable data class WishlistFormRoute(val wishlistId: String? = null)

@Serializable data object WearHistoryRoute
@Serializable data class WearHistoryDetailRoute(val historyId: String)
@Serializable data class WearHistoryFormRoute(val historyId: String? = null)
