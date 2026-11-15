package vin.pam.closetly.feature.inspiration.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.ui.components.CircleIconButton
import vin.pam.closetly.feature.inspiration.ui.components.CollectionThumb
import vin.pam.closetly.feature.inspiration.ui.components.InspirationTopBar
import vin.pam.closetly.feature.inspiration.ui.components.dashedBorder

/** Create, rename and delete collections. Nothing else. */
@Composable
fun ManageCollectionsScreen(
    collections: List<CollectionSummary>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(InspirationPalette.OffWhite)) {
        InspirationTopBar("Kelola Koleksi", onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(collections, key = { it.collection.id }) { summary ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(InspirationPalette.CardWhite)
                        .border(1.dp, InspirationPalette.Border, RoundedCornerShape(16.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CollectionThumb(summary.cover, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
                    Column(Modifier.weight(1f)) {
                        Text(summary.collection.name, color = InspirationPalette.Heading, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${summary.postCount} inspirasi", color = InspirationPalette.Muted, fontSize = 12.sp)
                    }
                    CircleIconButton({ onEditClick(summary.collection.id) }) {
                        Icon(Icons.Default.Edit, "Ubah nama", Modifier.size(16.dp), InspirationPalette.Heading)
                    }
                    CircleIconButton({ onDeleteClick(summary.collection.id) }) {
                        Icon(Icons.Default.Delete, "Hapus koleksi", Modifier.size(16.dp), InspirationPalette.Danger)
                    }
                }
            }
            item(key = "add") {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).dashedBorder(InspirationPalette.Border, 16.dp)
                        .clickable(onClick = onAddClick).padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+ Tambah Koleksi Baru", color = InspirationPalette.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
