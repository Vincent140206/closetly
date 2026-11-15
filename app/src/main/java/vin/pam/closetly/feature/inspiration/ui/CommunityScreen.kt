@file:OptIn(ExperimentalFoundationApi::class)

package vin.pam.closetly.feature.inspiration.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.ui.components.EmptyState
import vin.pam.closetly.feature.inspiration.ui.components.InspirationIcons
import vin.pam.closetly.feature.inspiration.ui.components.postItems

/**
 * Root screen of `InspirationRoute` (Community in the navigation contract): the public feed
 * as a masonry grid, with the "Inspirasiku" entry point to the personal bookmark board.
 */
@Composable
fun CommunityScreen(
    posts: List<InspirationPost>,
    savedIds: Set<String>,
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onMyInspirationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize().background(InspirationPalette.OffWhite),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
    ) {
        item(key = "header", span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.padding(top = 20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Papan Inspirasi", color = InspirationPalette.Heading, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("Cari style inspirasimu", color = InspirationPalette.Muted, fontSize = 13.sp)
                    }
                    MyInspirationButton(onMyInspirationClick)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query, onValueChange = onQueryChange,
                    placeholder = { Text("Cari dalam inspirasi...", color = InspirationPalette.Muted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = InspirationPalette.Muted) },
                    singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InspirationPalette.Pressed, unfocusedBorderColor = InspirationPalette.Border,
                        focusedContainerColor = InspirationPalette.CardWhite, unfocusedContainerColor = InspirationPalette.CardWhite,
                        cursorColor = InspirationPalette.Pressed,
                    ),
                )
                Spacer(Modifier.height(14.dp))
            }
        }
        when {
            isLoading -> item(key = "loading", span = StaggeredGridItemSpan.FullLine) {
                Text("Memuat inspirasi...", Modifier.padding(32.dp), color = InspirationPalette.Muted, fontSize = 13.sp)
            }
            posts.isEmpty() -> item(key = "empty", span = StaggeredGridItemSpan.FullLine) {
                EmptyState("Tidak ada hasil", "Coba kata kunci lain.")
            }
            else -> postItems(posts, savedIds, onPostClick, onBookmarkClick)
        }
    }
}

@Composable
private fun MyInspirationButton(onClick: () -> Unit) {
    Row(
        Modifier.clip(CircleShape).background(InspirationPalette.Golden).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(InspirationIcons.BookmarkFilled, null, Modifier.size(13.dp), InspirationPalette.Heading)
        Text("Inspirasiku", color = InspirationPalette.Heading, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
