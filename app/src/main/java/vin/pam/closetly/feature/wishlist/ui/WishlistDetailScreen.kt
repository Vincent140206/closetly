package vin.pam.closetly.feature.wishlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.ui.theme.ClosetlySpacing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WishlistDetailScreen(
    item: WishlistCardData,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
)

{
    var showDeleteDialog by rememberSaveable(item.id) {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ClosetlySpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics {
                    contentDescription = "Kembali ke Wishlist"
                },
            ) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Text(
                text = "Detail barang",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text("Perkiraan harga: ${item.priceLabel ?: "Belum diisi"}")
            Text("Toko: ${item.store ?: "Belum diisi"}")
            Text("Prioritas: ${item.priorityLabel ?: "Belum diisi"}")
            Text(if (item.isPurchased) "Sudah Dibeli" else "Belum Dibeli")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClosetlyButton(
                text = "Edit barang",
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(45.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Hapus barang")
            }
        }



    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus barang ini?") },
            text = {
                Text("\"${item.name}\" akan dihapus dari Wishlist.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = "Hapus barang",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}