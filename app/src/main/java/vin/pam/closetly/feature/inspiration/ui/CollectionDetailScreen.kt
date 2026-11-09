@file:OptIn(ExperimentalFoundationApi::class)

package vin.pam.closetly.feature.inspiration.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SortOrder
import vin.pam.closetly.feature.inspiration.ui.components.InspirationTopBar
import vin.pam.closetly.feature.inspiration.ui.components.EmptyState
import vin.pam.closetly.feature.inspiration.ui.components.SortDropdown
import vin.pam.closetly.feature.inspiration.ui.components.postItems

/** Shows only the posts belonging to one collection. Tapping a bookmark asks to remove the post. */
@Composable
fun CollectionDetailScreen(
    collection: CollectionSummary,
    posts: List<InspirationPost>,
    savedIds: Set<String>,
    sortOrder: SortOrder,
    onBack: () -> Unit,
    onManageClick: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onPostClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(InspirationPalette.OffWhite)) {
        InspirationTopBar(collection.collection.name, onBack, onManageClick = onManageClick)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
        ) {
            item(key = "meta", span = StaggeredGridItemSpan.FullLine) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${posts.size} items", color = InspirationPalette.Muted, fontSize = 12.sp)
                    SortDropdown(sortOrder, onSortSelected)
                }
            }
            if (posts.isEmpty()) {
                item(key = "empty", span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(
                        "Koleksi masih kosong",
                        "Simpan inspirasi ke koleksi ini dari Papan Inspirasi.",
                        actionLabel = "Jelajahi Inspirasi", onAction = onBack,
                    )
                }
            } else {
                postItems(posts, savedIds, onPostClick, onBookmarkClick)
            }
        }
    }
}
