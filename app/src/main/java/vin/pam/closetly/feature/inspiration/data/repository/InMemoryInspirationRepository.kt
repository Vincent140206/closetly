package vin.pam.closetly.feature.inspiration.data.repository

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vin.pam.closetly.feature.inspiration.model.InspirationCollection
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SavedPost

/** Local implementation backed by sample data. Used until the Supabase repository exists. */
class InMemoryInspirationRepository(
    initialPosts: List<InspirationPost> = InspirationSampleData.posts,
    initialCollections: List<InspirationCollection> = InspirationSampleData.collections,
    initialSaved: List<SavedPost> = InspirationSampleData.seedSaved(System.currentTimeMillis()),
    initialLiked: Set<String> = emptySet(),
    private val clock: () -> Long = System::currentTimeMillis,
) : InspirationRepository {

    private val _posts = MutableStateFlow(initialPosts)
    private val _collections = MutableStateFlow(initialCollections)
    private val _saved = MutableStateFlow(initialSaved)
    private val _liked = MutableStateFlow(initialLiked)

    override val posts: StateFlow<List<InspirationPost>> = _posts.asStateFlow()
    override val collections: StateFlow<List<InspirationCollection>> = _collections.asStateFlow()
    override val savedPosts: StateFlow<List<SavedPost>> = _saved.asStateFlow()
    override val likedPostIds: StateFlow<Set<String>> = _liked.asStateFlow()

    override suspend fun createCollection(name: String): InspirationCollection {
        val collection = InspirationCollection(id = UUID.randomUUID().toString(), name = name.trim())
        _collections.update { it + collection }
        return collection
    }

    override suspend fun renameCollection(collectionId: String, name: String) {
        _collections.update { list ->
            list.map { if (it.id == collectionId) it.copy(name = name.trim()) else it }
        }
    }

    override suspend fun deleteCollection(collectionId: String) {
        _saved.update { list -> list.filterNot { it.collectionId == collectionId } }
        _collections.update { list -> list.filterNot { it.id == collectionId } }
    }

    override suspend fun savePost(postId: String, collectionId: String) {
        val saved = SavedPost(postId, collectionId, clock())
        _saved.update { list -> list.filterNot { it.postId == postId } + saved }
    }

    override suspend fun removeSavedPost(postId: String) {
        _saved.update { list -> list.filterNot { it.postId == postId } }
    }

    override suspend fun toggleLike(postId: String) {
        val wasLiked = postId in _liked.value
        _liked.update { if (wasLiked) it - postId else it + postId }
        _posts.update { list ->
            list.map {
                if (it.id == postId) it.copy(likeCount = (it.likeCount + if (wasLiked) -1 else 1).coerceAtLeast(0)) else it
            }
        }
    }
}