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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.SortOrder
import vin.pam.closetly.feature.inspiration.ui.components.AddCollectionCard
import vin.pam.closetly.feature.inspiration.ui.components.CollectionCard
import vin.pam.closetly.feature.inspiration.ui.components.EmptyState
import vin.pam.closetly.feature.inspiration.ui.components.InspirationTopBar
import vin.pam.closetly.feature.inspiration.ui.components.SortDropdown
import vin.pam.closetly.feature.inspiration.ui.components.postItems

/** Inspirasiku: Koleksi (horizontal row) on top, Tersimpan (masonry grid + sort) directly below. */
@Composable
fun MyInspirationScreen(
    collections: List<CollectionSummary>,
    savedPosts: List<InspirationPost>,
    savedIds: Set<String>,
    sortOrder: SortOrder,
    onBack: () -> Unit,
    onManageClick: () -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onCollectionClick: (String) -> Unit,
    onAddCollectionClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(InspirationPalette.OffWhite)) {
        InspirationTopBar("Inspirasiku", onBack, onManageClick = onManageClick)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
        ) {
            item(key = "collections-title", span = StaggeredGridItemSpan.FullLine) {
                Text("Koleksi", color = InspirationPalette.Heading, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            item(key = "collections-row", span = StaggeredGridItemSpan.FullLine) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(key = "add") { AddCollectionCard(onAddCollectionClick) }
                    items(collections, key = { it.collection.id }) { summary ->
                        CollectionCard(summary) { onCollectionClick(summary.collection.id) }
                    }
                }
            }
            item(key = "saved-header", span = StaggeredGridItemSpan.FullLine) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text("Tersimpan", color = InspirationPalette.Heading, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${savedPosts.size} saved styles", color = InspirationPalette.Muted, fontSize = 12.sp)
                    }
                    SortDropdown(sortOrder, onSortSelected)
                }
            }
            if (savedPosts.isEmpty()) {
                item(key = "empty", span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(
                        "Belum Ada Inspirasi?",
                        "Simpan outfit favoritmu dari komunitas untuk melihatnya di sini.",
                        actionLabel = "Mulai Jelajahi", onAction = onBack,
                    )
                }
            } else {
                postItems(savedPosts, savedIds, onPostClick, onBookmarkClick)
            }
        }
    }
}
