package vin.pam.closetly.feature.wearhistory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vin.pam.closetly.ui.theme.ClosetlyTheme

private val Ink = Color(0xFF171717)
private val Gray = Color(0xFF62625F)
private val Border = Color(0xFFE8E5DC)
private val Cream = Color(0xFFFAFAF7)

// Model sementara untuk menampilkan pilihan outfit.
// Nantinya daftar diisi dari tabel "outfits".
data class SavedOutfitOption(
    val id: String,
    val name: String,
    val description: String,
    val coverImageUrl: String? = null
)

@Composable
fun AddWearHistoryScreen(
    outfits: List<SavedOutfitOption>,
    onBackClick: () -> Unit,
    onOutfitSelected: (String) -> Unit,
    isLoading: Boolean = false
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(
            horizontal = 32.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 1. HEADER
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Border,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Ink,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Text(
                    text = "Tambah Riwayat",
                    color = Ink,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // 2. SUBTITLE
        item {
            Text(
                text = "Pilih outfit yang pernah kamu pakai.",
                color = Gray,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                )
            )
        }

        // 3. JUDUL DAFTAR
        item {
            Spacer(Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Outfit Tersimpan",
                    color = Ink,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (!isLoading) {
                    Text(
                        text = "${outfits.size} outfit",
                        color = Gray,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        )
                    )
                }
            }

        }

        // 4. LOADING, EMPTY STATE, ATAU DAFTAR OUTFIT
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFACC15)
                    )
                }
            }
        } else if (outfits.isEmpty()) {
            item {
                Text(
                    text = "Belum ada outfit tersimpan. " +
                            "Buat outfit terlebih dahulu di Mix & Match.",
                    color = Gray,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(
                items = outfits,
                key = { it.id }
            ) { outfit ->
                SavedOutfitCard(
                    outfit = outfit,
                    onClick = {
                        onOutfitSelected(outfit.id)
                    }
                )
            }
        }
    }
}

// Komponen satu kartu outfit.
@Composable
private fun SavedOutfitCard(
    outfit: SavedOutfitOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FOTO OUTFIT
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F1EB)),
                contentAlignment = Alignment.Center
            ) {
                if (!outfit.coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = outfit.coverImageUrl,
                        contentDescription = outfit.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Checkroom,
                        contentDescription = "Foto belum tersedia",
                        tint = Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // NAMA DAN DESKRIPSI
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = outfit.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = outfit.description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Pilih ${outfit.name}",
                tint = Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// PREVIEW TANPA SUPABASE
@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun AddWearHistoryPreview() {
    ClosetlyTheme {
        AddWearHistoryScreen(
            outfits = listOf(
                SavedOutfitOption(
                    id = "outfit-1",
                    name = "Beige Casual",
                    description = "Beige blazer, cream trousers, loafers"
                ),
                SavedOutfitOption(
                    id = "outfit-2",
                    name = "Campus Daily",
                    description = "Striped sweater, blue jeans, sneakers"
                ),
                SavedOutfitOption(
                    id = "outfit-3",
                    name = "Weekend Look",
                    description = "Striped shirt, white shorts, slippers"
                ),
                SavedOutfitOption(
                    id = "outfit-4",
                    name = "Minimal White",
                    description = "White t-shirt, gray skirt, sneakers"
                )
            ),
            onBackClick = {},
            onOutfitSelected = {}
        )
    }
}
