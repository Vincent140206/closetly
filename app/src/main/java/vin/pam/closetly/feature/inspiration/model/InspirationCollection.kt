package vin.pam.closetly.feature.inspiration.model

data class InspirationCollection(
    val id: String,
    val name: String,
)

/** A bookmarked post. A post is saved into exactly one collection. */
data class SavedPost(
    val postId: String,
    val collectionId: String,
    val savedAt: Long,
)

/** Collection plus the data needed to render its card. */
data class CollectionSummary(
    val collection: InspirationCollection,
    val postCount: Int,
    val cover: InspirationPost?,
)
