package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun WishlistScreen(
    entries: List<WishlistCardData>,
    onOpenItem: (String) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var categoryFilter by rememberSaveable { mutableStateOf<WishlistCategory?>(null) }
    var priorityFilter by rememberSaveable { mutableStateOf<WishlistPriority?>(null) }
    var purchaseFilter by rememberSaveable { mutableStateOf(PurchaseFilter.All) }

    val hasActiveFilters = categoryFilter != null ||
            priorityFilter != null ||
            purchaseFilter != PurchaseFilter.All

    val purchasedCount = entries.count { it.isPurchased }
    val searchTerm = query.trim()

    val visibleEntries = entries.filter { entry ->
        val matchesQuery = entry.name.contains(searchTerm, ignoreCase = true)
        val matchesCategory = categoryFilter == null || entry.category == categoryFilter
        val matchesPriority = priorityFilter == null ||
                entry.priorityLabel == priorityFilter?.label

        val matchesStatus = when (purchaseFilter) {
            PurchaseFilter.All -> true
            PurchaseFilter.Unpurchased -> !entry.isPurchased
            PurchaseFilter.Purchased -> entry.isPurchased
        }

        matchesQuery && matchesCategory && matchesPriority && matchesStatus
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ClosetlySpacing.Lg,
                vertical = ClosetlySpacing.Sm,
            ),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text("Wishlist Saya", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Simpan incaran, rencanakan pembelianmu.",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (entries.isNotEmpty()) {
                ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text("${entries.size} barang")
                    Text("$purchasedCount sudah dibeli")
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Cari nama barang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ClosetlyButton(
                    text = "Filter Wishlist",
                    onClick = { showFilterSheet = true },
                )

                if (hasActiveFilters) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                    ) {
                        categoryFilter?.let { category ->
                            FilterChip(
                                selected = true,
                                onClick = { categoryFilter = null },
                                label = { Text("${category.label} ×") },
                            )
                        }

                        priorityFilter?.let { priority ->
                            FilterChip(
                                selected = true,
                                onClick = { priorityFilter = null },
                                label = { Text("${priority.label} ×") },
                            )
                        }

                        if (purchaseFilter != PurchaseFilter.All) {
                            FilterChip(
                                selected = true,
                                onClick = { purchaseFilter = PurchaseFilter.All },
                                label = { Text("${purchaseFilter.label} ×") },
                            )
                        }
                    }
                }

                if (searchTerm.isNotEmpty() || hasActiveFilters) {
                    Text(
                        text = "${visibleEntries.size} barang ditemukan",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ClosetlySpacing.Lg),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Belum ada barang incaran",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Simpan barang yang kamu suka di sini.")
            }
        } else if (visibleEntries.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ClosetlySpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(
                    space = ClosetlySpacing.Sm,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                Text(
                    text = "Belum ada yang cocok",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Coba kata lain atau reset filter.")

                if (searchTerm.isNotEmpty()) {
                    ClosetlyButton(
                        text = "Hapus pencarian",
                        onClick = { query = "" },
                    )
                }

                if (hasActiveFilters) {
                    ClosetlyButton(
                        text = "Reset filter",
                        onClick = {
                            categoryFilter = null
                            priorityFilter = null
                            purchaseFilter = PurchaseFilter.All
                        },
                    )
                }
            }
        } else {
            WishlistGrid(
                entries = visibleEntries,
                onOpenItem = onOpenItem,
                modifier = Modifier.weight(1f),
            )
        }

        ClosetlyButton(
            text = if (entries.isEmpty()) "Tambah barang pertama" else "Tambah barang",
            onClick = onAddItem,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ClosetlySpacing.Lg,
                    end = ClosetlySpacing.Lg,
                    bottom = ClosetlySpacing.Lg,
                ),
        )
    }

    // Di luar Column, tetapi masih di dalam WishlistScreen.
    if (showFilterSheet) {
        WishlistFilterSheet(
            activeCategory = categoryFilter,
            activePriority = priorityFilter,
            activeStatus = purchaseFilter,
            onDismiss = { showFilterSheet = false },
            onApply = { category, priority, status ->
                categoryFilter = category
                priorityFilter = priority
                purchaseFilter = status
                showFilterSheet = false
            },
        )
    }
}

@Preview(name = "Wishlist dengan barang", showBackground = true, widthDp = 393, heightDp = 750)
@Composable
private fun WishlistScreenPreview() {
    ClosetlyTheme {
        WishlistScreen(
            entries = listOf(
                WishlistCardData(
                    id = "denim-1",
                    name = "Jaket denim biru",
                    priceLabel = "Rp499.000",
                    store = "Erigo",
                    priorityLabel = WishlistPriority.High.label,
                    category = WishlistCategory.Clothing,
                ),
                WishlistCardData(
                    id = "sneakers-1",
                    name = "Sneakers putih",
                    isPurchased = true,
                    priceLabel = "Rp459.000",
                    store = "Aerostreet",
                    priorityLabel = WishlistPriority.Medium.label,
                    category = WishlistCategory.Shoes,
                ),
            ),
            onOpenItem = { },
            onAddItem = { },
        )
    }
}

@Preview(name = "Wishlist kosong", showBackground = true, widthDp = 393, heightDp = 750)
@Composable
private fun EmptyWishlistScreenPreview() {
    ClosetlyTheme {
        WishlistScreen(
            entries = emptyList(),
            onOpenItem = { },
            onAddItem = { },
        )
    }
}