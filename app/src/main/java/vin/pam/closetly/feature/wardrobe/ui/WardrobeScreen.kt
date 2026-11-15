package vin.pam.closetly.feature.wardrobe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.feature.wardrobe.model.ClothingItem
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.WardrobeFilter
import vin.pam.closetly.feature.wardrobe.ui.components.WardrobeBrandHeader
import vin.pam.closetly.feature.wardrobe.ui.components.WardrobePhoto

@Composable
fun WardrobeScreen(
    state: WardrobeUiState,
    onSearch: (String) -> Unit,
    onCategory: (WardrobeCategory?) -> Unit,
    onFilter: (WardrobeFilter) -> Unit,
    onCreate: () -> Unit,
    onOpenItem: (String) -> Unit,
    onEditItem: (String) -> Unit,
    onFavorite: (ClothingItem) -> Unit,
    onWishlist: () -> Unit,
    onOutfits: () -> Unit,
    onProfile: () -> Unit,
    onRetry: () -> Unit,
    showClosetTabs: Boolean = true,
    showBrandHeader: Boolean = true,
) {
    var showFilter by remember { mutableStateOf(false) }
    val visible = state.visibleItems
    val colors = MaterialTheme.colorScheme
    val largeText = LocalDensity.current.fontScale > 1.2f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (largeText) 220.dp else 144.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showBrandHeader) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    WardrobeBrandHeader(onProfile)
                }
            }

            if (showClosetTabs) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.outlineVariant),
                    ) {
                        Row {
                            Text(
                                text = "Koleksi",
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(colors.onBackground)
                                    .selectable(
                                        selected = true,
                                        role = Role.Tab,
                                        onClick = {},
                                    )
                                    .padding(vertical = 12.dp),
                                color = colors.background,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Wishlist",
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .selectable(
                                        selected = false,
                                        role = Role.Tab,
                                        onClick = onWishlist,
                                    )
                                    .padding(vertical = 12.dp),
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Koleksi Lemari Saya",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Semua pakaian dan outfit yang tersimpan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CollectionStat(
                        title = "${state.activeCount} pakaian",
                        subtitle = "Di lemari digitalmu",
                        icon = Icons.Outlined.Checkroom,
                        modifier = Modifier.weight(1f),
                    )
                    CollectionStat(
                        title = "${state.outfitCount} outfit",
                        subtitle = "Siap dipakai lagi",
                        icon = Icons.Outlined.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = onOutfits,
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onSearch,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Cari nama pakaian",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (state.filter.isActive) {
                            colors.primaryContainer
                        } else {
                            colors.surface
                        },
                        border = BorderStroke(1.dp, colors.outlineVariant),
                    ) {
                        IconButton(
                            onClick = { showFilter = true },
                            modifier = Modifier.size(54.dp),
                        ) {
                            Icon(Icons.Outlined.Tune, "Filter koleksi")
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CategoryChip("Semua", state.filter.category == null) {
                            onCategory(null)
                        }
                    }
                    items(WardrobeCategory.entries) { category ->
                        CategoryChip(category.label, state.filter.category == category) {
                            onCategory(category)
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.filter.archivedOnly) {
                            "Arsip pakaian"
                        } else {
                            "Semua pakaian"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${visible.size} pakaian",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    IconButton(onClick = onRetry, enabled = !state.isLoading) {
                        Icon(Icons.Outlined.Refresh, "Muat ulang koleksi")
                    }
                }
            }

            val errorMessage = state.errorMessage
            if (errorMessage != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = errorMessage,
                            color = colors.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        ClosetlyTextButton("Coba lagi", onRetry)
                    }
                }
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(Modifier.padding(24.dp))
                    }
                }
            } else if (visible.isEmpty() && errorMessage == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val hasSearchOrFilter = state.query.isNotBlank() || state.filter.isActive

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = colors.onSurfaceVariant,
                        )
                        Text(
                            text = if (hasSearchOrFilter) "Tidak ada hasil" else "Lemari masih kosong",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (state.filter.archivedOnly) {
                                "Pakaian yang dihapus dari koleksi akan muncul di sini."
                            } else {
                                "Tambahkan pakaian atau sesuaikan pencarian dan filter."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                        if (hasSearchOrFilter) {
                            ClosetlyTextButton("Reset pencarian dan filter", {
                                onSearch("")
                                onFilter(WardrobeFilter())
                            })
                        }
                    }
                }
            } else {
                items(visible, key = { it.id }) { item ->
                    ClothingCard(
                        item = item,
                        onOpen = { onOpenItem(item.id) },
                        onEdit = { onEditItem(item.id) },
                        onFavorite = { onFavorite(item) },
                    )
                }
            }
        }

        ClosetlyButton(
            text = "+ Tambah Pakaian",
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        )
    }

    if (showFilter) {
        WardrobeFilterSheet(
            initial = state.filter,
            colors = state.items
                .map { it.color }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted(),
            onDismiss = { showFilter = false },
            onApply = { filter ->
                onFilter(filter)
                showFilter = false
            },
        )
    }
}

@Composable
private fun CollectionStat(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    val border = BorderStroke(1.dp, colors.outlineVariant)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, null, Modifier.size(25.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }

    if (onClick == null) {
        Surface(modifier = modifier, shape = shape, border = border, content = content)
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            border = border,
            content = content,
        )
    }
}

@Composable
internal fun CategoryChip(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.onBackground,
            selectedLabelColor = colors.background,
        ),
    )
}

@Composable
private fun ClothingCard(
    item: ClothingItem,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onFavorite: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WardrobePhoto(
                item.imagePath,
                item.name,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(15.dp)),
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(item.categoryLabel, item.style)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) {
                            Icons.Outlined.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (item.isFavorite) {
                            "Hapus ${item.name} dari favorit"
                        } else {
                            "Favoritkan ${item.name}"
                        },
                        tint = if (item.isFavorite) colors.error else colors.onSurface,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (!item.isArchived) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, "Edit ${item.name}")
                    }
                }
            }
        }
    }
}