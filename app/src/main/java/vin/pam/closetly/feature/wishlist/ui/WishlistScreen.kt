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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.layout.Box
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.outlined.FilterAlt

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

    val activeFilterCount = listOf(
        categoryFilter != null,
        priorityFilter != null,
        purchaseFilter != PurchaseFilter.All,
    ).count { active -> active }

    val hasActiveFilters = activeFilterCount > 0

    val purchasedCount = entries.count { it.isPurchased }
    val unpurchasedEntries = entries.filterNot { it.isPurchased }

    val unpurchasedPrices = unpurchasedEntries.mapNotNull {
        it.estimatedPrice
    }

    val estimatedTotalLabel = when {
        unpurchasedEntries.isEmpty() -> "Rp0"

        unpurchasedPrices.isEmpty() -> "—"

        else -> {
            val total = unpurchasedPrices.sum()

            val formatter = NumberFormat.getIntegerInstance(
                Locale.forLanguageTag("id-ID"),
            )

            "Rp${formatter.format(total)}"
        }
    }
    val searchTerm = query.trim()

    val visibleEntries = entries.filter { entry ->
        val matchesQuery = entry.name.contains(searchTerm, ignoreCase = true)
        val matchesCategory = categoryFilter == null || entry.category == categoryFilter
        val matchesPriority = priorityFilter == null || entry.priorityLabel == priorityFilter?.label

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    ClosetlySpacing.Sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Wishlist Saya",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (entries.isNotEmpty()) {
                Text(
                    text = "Simpan incaran, rencanakan pembelianmu.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (entries.isNotEmpty()) {
                if (searchTerm.isEmpty() && !hasActiveFilters) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .padding(ClosetlySpacing.Lg),
                            horizontalArrangement = Arrangement.spacedBy(
                                ClosetlySpacing.Sm,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(0.7f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "${entries.size} barang",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Text(
                                    text = "$purchasedCount sudah dibeli",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )

                            Column(
                                modifier = Modifier.weight(1.3f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "Estimasi belum dibeli",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Text(
                                    text = estimatedTotalLabel,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Cari nama barang") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Hapus pencarian",
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    )

                    OutlinedIconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.size(56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Buka filter Wishlist",
                        )
                    }
                }

                if (hasActiveFilters) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                    ) {
                        categoryFilter?.let { category ->
                            FilterChip(
                                selected = true,
                                onClick = { categoryFilter = null },
                                label = {
                                    Text(
                                        text = category.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }

                        priorityFilter?.let { priority ->
                            FilterChip(
                                selected = true,
                                onClick = { priorityFilter = null },
                                label = {
                                    Text(
                                        text = priority.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }

                        if (purchaseFilter != PurchaseFilter.All) {
                            FilterChip(
                                selected = true,
                                onClick = { purchaseFilter = PurchaseFilter.All },
                                label = {
                                    Text(
                                        text = purchaseFilter.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }

                if (searchTerm.isNotEmpty() || hasActiveFilters) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${visibleEntries.size} barang ditemukan",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        if (hasActiveFilters && visibleEntries.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    categoryFilter = null
                                    priorityFilter = null
                                    purchaseFilter = PurchaseFilter.All
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                Text(
                                    text = "Reset filter",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                    if (visibleEntries.isNotEmpty()) {
                        Text(
                            text = when {
                                searchTerm.isNotEmpty() && hasActiveFilters ->
                                    "Hasil untuk \"$searchTerm\" dengan $activeFilterCount filter aktif."

                                searchTerm.isNotEmpty() ->
                                    "Hasil untuk \"$searchTerm\"."

                                else ->
                                    "Hasil dengan $activeFilterCount filter aktif."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Semua barang",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        Text(
                            text = "${visibleEntries.size} barang",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = ClosetlySpacing.Lg,
                            vertical = ClosetlySpacing.Md,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Lg,
                    ),
                ) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }

                    Text(
                        text = "Belum ada barang incaran",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Ada pakaian, sepatu, atau aksesori yang kamu suka? Simpan dulu di sini, beli saat waktunya tepat.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    ClosetlyButton(
                        text = "+ Tambah barang pertama",
                        onClick = onAddItem,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Atur prioritasmu, tanpa perlu buru-buru beli.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else if (visibleEntries.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = ClosetlySpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(
                    space = ClosetlySpacing.Md,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Text(
                    text = "Belum ada yang cocok",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Coba kata lain atau reset filter.")

                if (hasActiveFilters) {
                    ClosetlyButton(
                        text = "Reset filter",
                        onClick = {
                            categoryFilter = null
                            priorityFilter = null
                            purchaseFilter = PurchaseFilter.All
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (searchTerm.isNotEmpty()) {
                    ClosetlyButton(
                        text = "Hapus pencarian",
                        onClick = { query = "" },
                        modifier = Modifier.fillMaxWidth(),
                        style = ClosetlyButtonStyle.Secondary,
                    )
                }
                Text(
                    text = "${entries.size} barang di wishlist kamu tetap tersimpan.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ClosetlySpacing.Sm),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            WishlistGrid(
                entries = visibleEntries,
                onOpenItem = onOpenItem,
                modifier = Modifier.weight(1f),
            )
        }

        if (entries.isNotEmpty() && visibleEntries.isNotEmpty()) {
                ClosetlyButton(
                text = if (entries.isEmpty()) {
                    "Tambah barang pertama"
                } else {
                    "Tambah barang"
                },
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
    }
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