package vin.pam.closetly.feature.wardrobe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.feature.wardrobe.model.WardrobeCategory
import vin.pam.closetly.feature.wardrobe.model.WardrobeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WardrobeFilterSheet(initial: WardrobeFilter, colors: List<String>, onDismiss: () -> Unit, onApply: (WardrobeFilter) -> Unit) {
    var filter by remember(initial) { mutableStateOf(initial) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Filter koleksi", style = MaterialTheme.typography.titleLarge)
            Text("Kategori", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("Semua", filter.category == null) { filter = filter.copy(category = null) }
                WardrobeCategory.entries.forEach { category ->
                    CategoryChip(category.label, filter.category == category) { filter = filter.copy(category = category) }
                }
            }
            if (colors.isNotEmpty()) {
                Text("Warna", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip("Semua warna", filter.color.isBlank()) { filter = filter.copy(color = "") }
                    colors.forEach { color ->
                        CategoryChip(color, filter.color == color) { filter = filter.copy(color = color) }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = filter.favoritesOnly, role = Role.Switch,
                    onValueChange = { filter = filter.copy(favoritesOnly = it) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Favorit saja", Modifier.weight(1f))
                Switch(filter.favoritesOnly, onCheckedChange = null)
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = filter.archivedOnly, role = Role.Switch,
                    onValueChange = { filter = filter.copy(archivedOnly = it) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tampilkan arsip", Modifier.weight(1f))
                Switch(filter.archivedOnly, onCheckedChange = null)
            }
            ClosetlyButton("Terapkan filter", { onApply(filter) }, Modifier.fillMaxWidth())
            ClosetlyTextButton("Reset filter", { filter = WardrobeFilter() }, Modifier.fillMaxWidth())
        }
    }
}
