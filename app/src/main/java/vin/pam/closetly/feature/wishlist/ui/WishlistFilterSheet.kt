package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.ui.theme.ClosetlySpacing
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment

internal enum class PurchaseFilter(val label: String) {
    All("Semua status"),
    Unpurchased("Belum Dibeli"),
    Purchased("Sudah Dibeli"),
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
internal fun WishlistFilterSheet(
    activeCategory: WishlistCategory?,
    activePriority: WishlistPriority?,
    activeStatus: PurchaseFilter,
    onDismiss: () -> Unit,
    onApply: (WishlistCategory?, WishlistPriority?, PurchaseFilter) -> Unit,
) {
    var category by rememberSaveable { mutableStateOf(activeCategory) }
    var priority by rememberSaveable { mutableStateOf(activePriority) }
    var status by rememberSaveable { mutableStateOf(activeStatus) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ClosetlySpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text("Filter Wishlist", style = MaterialTheme.typography.titleLarge)

            Text("Kategori", style = MaterialTheme.typography.titleSmall)
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(ClosetlySpacing.Lg),
                    verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                ) {
                    Text(
                        text = "Filter Wishlist",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                    ) {
                        Text("Kategori", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                            FilterChip(
                                selected = category == null,
                                onClick = { category = null },
                                label = { Text("Semua kategori") },
                            )
                            WishlistCategory.entries.forEach { option ->
                                FilterChip(
                                    selected = category == option,
                                    onClick = { category = option },
                                    label = { Text(option.label) },
                                )
                            }
                        }

                        Text("Prioritas pembelian", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                            FilterChip(
                                selected = priority == null,
                                onClick = { priority = null },
                                label = { Text("Semua prioritas") },
                            )
                            WishlistPriority.entries.forEach { option ->
                                FilterChip(
                                    selected = priority == option,
                                    onClick = { priority = option },
                                    label = { Text(option.label) },
                                )
                            }
                        }

                        Text("Status pembelian", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                            PurchaseFilter.entries.forEach { option ->
                                FilterChip(
                                    selected = status == option,
                                    onClick = { status = option },
                                    label = { Text(option.label) },
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                category = null
                                priority = null
                                status = PurchaseFilter.All
                            },
                        ) {
                            Text("Reset")
                        }

                        ClosetlyButton(
                            text = "Terapkan filter",
                            onClick = { onApply(category, priority, status) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Text("Prioritas pembelian", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                FilterChip(
                    selected = priority == null,
                    onClick = { priority = null },
                    label = { Text("Semua prioritas") },
                )
                WishlistPriority.entries.forEach { option ->
                    FilterChip(
                        selected = priority == option,
                        onClick = { priority = option },
                        label = { Text(option.label) },
                    )
                }
            }

            Text("Status pembelian", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                PurchaseFilter.entries.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option.label) },
                    )
                }
            }

            TextButton(
                onClick = {
                    category = null
                    priority = null
                    status = PurchaseFilter.All
                },
            ) {
                Text("Reset")
            }

            ClosetlyButton(
                text = "Terapkan filter",
                onClick = { onApply(category, priority, status) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}