package vin.pam.closetly.feature.inspiration.data.repository

import kotlinx.coroutines.flow.StateFlow
import vin.pam.closetly.feature.inspiration.model.InspirationCollection
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SavedPost

/**
 * Contract for the Inspiration feature. Operations are suspend so a Supabase-backed
 * implementation (PostgREST + Storage) can replace the in-memory one without UI changes.
 */
interface InspirationRepository {
    val posts: StateFlow<List<InspirationPost>>
    val collections: StateFlow<List<InspirationCollection>>
    val savedPosts: StateFlow<List<SavedPost>>

    /** Posts the current user liked (`community_post_likes`). */
    val likedPostIds: StateFlow<Set<String>>

    suspend fun createCollection(name: String): InspirationCollection
    suspend fun renameCollection(collectionId: String, name: String)

    /** Deletes the collection and every saved post inside it. */
    suspend fun deleteCollection(collectionId: String)

    /** Saves (or moves) a post into a collection. */
    suspend fun savePost(postId: String, collectionId: String)
    suspend fun removeSavedPost(postId: String)

    /** Likes or unlikes a post and keeps its like count in sync. */
    suspend fun toggleLike(postId: String)
}