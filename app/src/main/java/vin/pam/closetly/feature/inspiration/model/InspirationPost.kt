package vin.pam.closetly.feature.inspiration.model

/** Silhouette used by the placeholder artwork until real post images are wired in. */
enum class OutfitStyle { Coat, Suit, Dress, Tee }

/** Sample-only artwork. Real posts load their image from [InspirationPost.imagePath]. */
data class PlaceholderArt(
    val aspectRatio: Float,
    val outfit: OutfitStyle,
    val backgroundColor: Long,
    val topColor: Long,
    val bottomColor: Long,
    val skinColor: Long,
    val hatColor: Long? = null,
)

/**
 * A public Community post.
 * Maps to `community_posts` (id, user_id, caption, image_path, created_at) and the author's
 * `profiles.display_name` / avatar. Title, tags and like_count need the proposed columns on `community_posts`.
 */
data class InspirationPost(
    val id: String,
    val authorId: String,
    val authorName: String,
    /** `profiles.public_avatar_path` (public bucket). Null shows the initial instead. */
    val authorAvatarPath: String?,
    val title: String,
    val caption: String,
    val tags: List<String>,
    /** `community_posts.like_count`. */
    val likeCount: Int,
    /** Path in the public Community bucket. Never a signed URL. */
    val imagePath: String?,
    val createdAt: Long,
    val art: PlaceholderArt? = null,
) {
    /** width / height for the masonry grid; real images will size themselves. */
    val aspectRatio: Float get() = art?.aspectRatio ?: 0.75f
}