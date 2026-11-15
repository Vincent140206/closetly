@file:OptIn(ExperimentalFoundationApi::class)

package vin.pam.closetly.feature.inspiration.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.OutfitStyle
import vin.pam.closetly.feature.inspiration.model.PlaceholderArt
import vin.pam.closetly.feature.inspiration.ui.components.Avatar
import vin.pam.closetly.feature.inspiration.ui.components.EmptyState
import vin.pam.closetly.feature.inspiration.ui.components.InspirationIcons
import vin.pam.closetly.feature.inspiration.ui.components.InspirationTopBar
import vin.pam.closetly.feature.inspiration.ui.components.PostImage
import vin.pam.closetly.feature.inspiration.ui.components.postItems

private val HeroShape = RoundedCornerShape(24.dp)

/** Style Detail (Figma screen 4). "Inspirasi Terkait" sits below and opens other details on tap. */
@Composable
fun StyleDetailScreen(
    post: InspirationPost?,
    related: List<InspirationPost>,
    savedIds: Set<String>,
    likedIds: Set<String>,
    onBack: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onShareClick: (InspirationPost) -> Unit,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(InspirationPalette.OffWhite)) {
        InspirationTopBar("Detail Style", onBack)
        if (post == null) {
            EmptyState("Post tidak ditemukan", "Inspirasi ini mungkin sudah dihapus.", actionLabel = "Kembali", onAction = onBack)
            return@Column
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
        ) {
            item(key = "hero", span = StaggeredGridItemSpan.FullLine) {
                Hero(post, post.id in savedIds, post.id in likedIds, onBookmarkClick, onLikeClick, onShareClick)
            }
            item(key = "owner", span = StaggeredGridItemSpan.FullLine) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(InspirationPalette.CardWhite)
                        .border(1.dp, InspirationPalette.Border, RoundedCornerShape(16.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(post.authorName, 40.dp, 15.sp)
                    Text(post.authorName, color = InspirationPalette.Heading, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            item(key = "caption", span = StaggeredGridItemSpan.FullLine) {
                Text(post.caption, color = InspirationPalette.Body, fontSize = 14.sp, lineHeight = 21.sp)
            }
            item(key = "related-title", span = StaggeredGridItemSpan.FullLine) {
                Text(
                    "Inspirasi Terkait", Modifier.padding(top = 8.dp),
                    color = InspirationPalette.Heading, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                )
            }
            postItems(related, savedIds, onPostClick, onBookmarkClick)
        }
    }
}

@Composable
private fun Hero(
    post: InspirationPost,
    isSaved: Boolean,
    isLiked: Boolean,
    onBookmarkClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onShareClick: (InspirationPost) -> Unit,
) {
    Box(
        Modifier.fillMaxWidth()
            .shadow(10.dp, HeroShape, ambientColor = Color(0x26000000), spotColor = Color(0x26000000))
            .clip(HeroShape),
    ) {
        PostImage(post, Modifier.fillMaxWidth().aspectRatio(0.72f))

        // Tags, top-left: white 60% pills, small bold caps.
        Row(Modifier.align(Alignment.TopStart).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            post.tags.take(2).forEach { tag ->
                Text(
                    tag.uppercase(),
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.50f))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    color = InspirationPalette.Heading,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                )
            }
        }

        // Bottom scrim: transparent to 72% black, carrying title, like, share and save.
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.72f)))
                .padding(start = 14.dp, end = 14.dp, top = 56.dp, bottom = 14.dp),
        ) {
            //title
            Text(post.title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))

            //3 buttons
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                //like
                RoundAction({ onLikeClick(post.id) }) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        if (isLiked) "Batal suka" else "Suka", Modifier.size(18.dp),
                        if (isLiked) InspirationPalette.Danger else InspirationPalette.Heading,
                    )
                }
                Text(formatCount(post.likeCount), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                //share
                RoundAction({ onShareClick(post) }) {
                    Icon(Icons.Default.Share, "Bagikan", Modifier.size(17.dp), InspirationPalette.Heading)
                }
                Spacer(Modifier.weight(1f))

                //bookmark
                Row(
                    Modifier.clip(CircleShape).background(InspirationPalette.Golden)
                        .clickable { onBookmarkClick(post.id) }.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        if (isSaved) InspirationIcons.BookmarkFilled else InspirationIcons.BookmarkOutline,
                        null, Modifier.size(14.dp), InspirationPalette.Heading,
                    )
                    Text(if (isSaved) "Tersimpan" else "Simpan", color = InspirationPalette.Heading, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RoundAction(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** 1137 -> "1.1k", 950 -> "950". */
private fun formatCount(n: Int): String =
    if (n < 1000) n.toString() else String.format(Locale.US, "%.1fk", n / 1000f).replace(".0k", "k")