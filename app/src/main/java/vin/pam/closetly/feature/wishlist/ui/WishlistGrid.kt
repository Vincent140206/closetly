package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import vin.pam.closetly.feature.wishlist.ui.components.WishlistItemCard
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class WishlistCardData(
    val id: String,
    val name: String,
    val isPurchased: Boolean = false,
    val priceLabel: String? = null,
    val store: String? = null,
    val priorityLabel: String? = null,
    val imageModel: String? = null,
    val category: WishlistCategory? = null,

)

@Composable
fun WishlistGrid(
    entries: List<WishlistCardData>,
    onOpenItem: (String) -> Unit,
    modifier: Modifier = Modifier.Companion,
    onMarkPurchased: ((String) -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(ClosetlySpacing.Lg),
        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        items(entries, key = { it.id }) { entry ->
            WishlistItemCard(
                name = entry.name,
                isPurchased = entry.isPurchased,
                onOpenDetail = { onOpenItem(entry.id) },
                modifier = Modifier.fillMaxWidth(),
                imageModel = entry.imageModel,
                priceLabel = entry.priceLabel,
                store = entry.store,
                priorityLabel = entry.priorityLabel,
                onMarkPurchased = onMarkPurchased?.let { markPurchased ->
                    { markPurchased(entry.id) }
                },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 650)
@Composable
private fun WishlistGridPreview() {
    var entries by remember {
        mutableStateOf(
            listOf(
                WishlistCardData(
                    id = "denim-1",
                    name = "Jaket denim biru",
                    priceLabel = "Rp499.000",
                    store = "Erigo",
                    priorityLabel = "Tinggi",
                ),
                WishlistCardData(
                    id = "sneakers-1",
                    name = "Sneakers putih",
                    priceLabel = "Rp459.000",
                    store = "Aerostreet",
                    priorityLabel = "Sedang",
                ),
                WishlistCardData(
                    id = "shirt-1",
                    name = "pinky shirt",
                    priceLabel = "Rp699.000",
                    store = "Mawiwawi",
                    priorityLabel = "Tinggi",
                ),
            ),
        )
    }

    ClosetlyTheme {
        WishlistGrid(
            entries = entries,
            onOpenItem = { },
            onMarkPurchased = { clickedId ->
                entries = entries.map { entry ->
                    if (entry.id == clickedId) {
                        entry.copy(isPurchased = true)
                    } else {
                        entry
                    }
                }
            },
        )
    }
}