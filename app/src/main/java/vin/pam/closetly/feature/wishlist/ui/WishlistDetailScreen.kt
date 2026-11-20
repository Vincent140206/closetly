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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.ui.unit.sp

@Composable
fun WishlistDetailScreen(
    item: WishlistCardData,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePurchased: () -> Unit,
) {
    val context = LocalContext.current
    val productUrl = item.productUrl?.trim()?.takeIf {
        it.isNotEmpty()
    }

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

        // Foto utama barang.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (item.imageModel.isNullOrBlank()) {
                Text(
                    text = "Belum ada foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AsyncImage(
                    model = item.imageModel,
                    contentDescription = "Foto ${item.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

// Kategori dan status pembelian.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.category?.label ?: "Tanpa kategori",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = ClosetlySpacing.Sm),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (item.isPurchased) {
                    Color(0xFFF1F6F0)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Text(
                    text = if (item.isPurchased) {
                        "✓ Sudah Dibeli"
                    } else {
                        "Belum Dibeli"
                    },
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp,
                    ),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

// Nama dan perkiraan harga.
        Column(
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            Text(
                text = item.priceLabel ?: "Harga belum diisi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

// Informasi toko dan prioritas.
        ClosetlyCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    ClosetlySpacing.Md,
                ),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Xs,
                    ),
                ) {
                    Text(
                        text = "Nama toko",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = item.store
                            ?.takeIf { it.isNotBlank() }
                            ?: "Belum diisi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        ClosetlySpacing.Xs,
                    ),
                ) {
                    Text(
                        text = "Prioritas pembelian",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = item.priorityLabel ?: "Belum diisi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        )
        {
            if (productUrl != null) {
                OutlinedButton(
                    onClick = {
                        val normalizedUrl = when {
                            productUrl.startsWith(
                                "https://",
                                ignoreCase = true,
                            ) -> productUrl

                            productUrl.startsWith(
                                "http://",
                                ignoreCase = true,
                            ) -> productUrl

                            else -> "https://$productUrl"
                        }

                        val uri = Uri.parse(normalizedUrl)

                        val isValidUrl =
                            (
                                    uri.scheme.equals("https", ignoreCase = true) ||
                                            uri.scheme.equals("http", ignoreCase = true)
                                    ) &&
                                    !uri.host.isNullOrBlank() &&
                                    normalizedUrl.none { it.isWhitespace() }

                        if (isValidUrl) {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, uri),
                                )
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "Tidak ada aplikasi untuk membuka tautan.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Tautan belum valid. Periksa melalui Edit barang.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(45.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        text = "Kunjungi Produk",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            ClosetlyButton(
                text = if (item.isPurchased) {
                    "Tandai belum dibeli"
                } else {
                    "Tandai sudah dibeli"
                },
                onClick = onTogglePurchased,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(45.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    text = "Edit barang",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = "Hapus barang",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }


    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Hapus barang ini?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            },
            text = {
                Text(
                    text = "\"${item.name}\" akan dihapus dari Wishlist. " +
                            "Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Hapus barang")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    shape = RoundedCornerShape(45.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Batal")
                }
            },
        )
    }
}