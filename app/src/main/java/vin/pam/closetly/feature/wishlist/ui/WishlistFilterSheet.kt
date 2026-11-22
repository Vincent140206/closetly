package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import vin.pam.closetly.ui.theme.ClosetlyTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.ui.theme.ClosetlySpacing

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
    onApply: (
        WishlistCategory?,
        WishlistPriority?,
        PurchaseFilter,
    ) -> Unit,
) {
    var category by rememberSaveable {
        mutableStateOf(activeCategory)
    }

    var priority by rememberSaveable {
        mutableStateOf(activePriority)
    }

    var status by rememberSaveable {
        mutableStateOf(activeStatus)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ClosetlyTheme(darkTheme = false) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(ClosetlySpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(
                    ClosetlySpacing.Md,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Filter Wishlist",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Tutup filter",
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Sm,
                    ),
                ) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleSmall,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            ClosetlySpacing.Sm,
                        ),
                    ) {
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

                    Text(
                        text = "Prioritas pembelian",
                        style = MaterialTheme.typography.titleSmall,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            ClosetlySpacing.Sm,
                        ),
                    ) {
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

                    Text(
                        text = "Status pembelian",
                        style = MaterialTheme.typography.titleSmall,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            ClosetlySpacing.Sm,
                        ),
                    ) {
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
                    horizontalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Sm,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ClosetlyButton(
                        text = "Reset",
                        onClick = {
                            category = null
                            priority = null
                            status = PurchaseFilter.All
                        },
                        style = ClosetlyButtonStyle.Secondary,
                    )

                    ClosetlyButton(
                        text = "Terapkan filter",
                        onClick = {
                            onApply(category, priority, status)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
