@file:OptIn(ExperimentalFoundationApi::class)

package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette

/** Adds posts to a staggered (masonry) grid. Shared by every screen that lists posts. */
fun LazyStaggeredGridScope.postItems(
    posts: List<InspirationPost>,
    savedIds: Set<String>,
    onPostClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
) {
    items(posts, key = { it.id }) { post ->
        PostCard(
            post = post,
            isSaved = post.id in savedIds,
            onClick = { onPostClick(post.id) },
            onBookmarkClick = { onBookmarkClick(post.id) },
        )
    }
}

@Composable
fun PostCard(
    post: InspirationPost,
    isSaved: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(InspirationPalette.Vanilla)
            .clickable(onClick = onClick),
    ) {
        PostImage(post, Modifier.fillMaxWidth().aspectRatio(post.aspectRatio))
        BookmarkButton(isSaved, onBookmarkClick, Modifier.align(Alignment.TopEnd).padding(8.dp))
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color(0x8C171717))
                .padding(start = 3.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Avatar(post.authorName, 20.dp, 10.sp)
            Text(post.authorName, color = Color.White, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
fun BookmarkButton(isSaved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (isSaved) InspirationPalette.Golden else Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isSaved) InspirationIcons.BookmarkFilled else InspirationIcons.BookmarkOutline,
            contentDescription = if (isSaved) "Hapus dari tersimpan" else "Simpan ke koleksi",
            tint = InspirationPalette.Heading,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
fun Avatar(name: String, size: Dp, textSize: TextUnit) {
    Box(
        Modifier.size(size).clip(CircleShape).background(InspirationPalette.SoftYellow),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1), fontSize = textSize, fontWeight = FontWeight.Bold, color = InspirationPalette.Heading)
    }
}
