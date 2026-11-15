package vin.pam.closetly.feature.inspiration.ui

import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SortOrder

/** The single bottom sheet. Save and create share one sheet: no nested popups. */
sealed interface SheetState {
    data class SaveToCollection(
        val postId: String,
        val selectedCollectionId: String? = null,
        val isCreating: Boolean = false,
    ) : SheetState

    data object CreateCollection : SheetState
    data class RenameCollection(val collectionId: String, val currentName: String) : SheetState
}

sealed interface DialogState {
    data class RemoveSaved(val postId: String) : DialogState
    data class DeleteCollection(val collectionId: String) : DialogState
}

data class InspirationUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allPosts: List<InspirationPost> = emptyList(),
    val feedPosts: List<InspirationPost> = emptyList(),
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.RecentlyAdded,
    val savedPosts: List<InspirationPost> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    val likedIds: Set<String> = emptySet(),
    val collections: List<CollectionSummary> = emptyList(),
    val collectionPosts: Map<String, List<InspirationPost>> = emptyMap(),
    val sheet: SheetState? = null,
    val dialog: DialogState? = null,
    val message: String? = null,
) {
    fun postById(id: String): InspirationPost? = allPosts.firstOrNull { it.id == id }

    fun collectionById(id: String): CollectionSummary? = collections.firstOrNull { it.collection.id == id }

    /** Most shared tags first, then newest. */
    fun relatedTo(post: InspirationPost): List<InspirationPost> =
        allPosts.filter { it.id != post.id }
            .sortedWith(
                compareByDescending<InspirationPost> { other -> other.tags.count { it in post.tags } }
                    .thenByDescending { it.createdAt },
            )
            .take(6)
}