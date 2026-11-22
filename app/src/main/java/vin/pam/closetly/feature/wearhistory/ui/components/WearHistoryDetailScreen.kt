package vin.pam.closetly.feature.wearhistory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import vin.pam.closetly.ui.theme.ClosetlyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Warna lokal untuk Detail Wear History
private val DetailYellow = Color(0xFFFACC15)
private val DetailInk = Color(0xFF171717)
private val DetailGray = Color(0xFF62625F)
private val DetailCream = Color(0xFFFAFAF7)
private val DetailBorder = Color(0xFFE8E5DC)
private val DetailRed = Color(0xFFB42318)

@Composable
fun WearHistoryDetailScreen(
    outfitName: String,
    wornDate: LocalDate,
    notes: String,
    imageUrl: String? = null,
    activity: String? = null,
    outfitItems: List<String> = emptyList(),
    outfitPreviewRes: Int? = null,
    isDeleting: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern(
            "EEEE, dd MMMM yyyy",
            Locale.forLanguageTag("id-ID")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailCream)
    ) {

        // 1. HEADER - TETAP DI ATAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 20.dp,
                    bottom = 12.dp
                ),
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
                            color = DetailBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        modifier = Modifier.size(16.dp),
                        tint = DetailInk
                    )
                }
            }

            Text(
                text = "Detail Riwayat",
                color = DetailInk,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )
        }

        // 2. KONTEN - SCROLL VERTIKAL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // FOTO OUTFIT
            DetailOutfitImage(
                outfitName = outfitName,
                imageUrl = imageUrl,
                previewRes = outfitPreviewRes
            )

            // NAMA OUTFIT
            DetailInformationSection(
                label = "Outfit Dipakai",
                value = outfitName
            )

            // TANGGAL PEMAKAIAN
            DetailInformationSection(
                label = "Tanggal Pemakaian",
                value = wornDate.format(dateFormatter)
            )

            // AKTIVITAS - HANYA JIKA TERSEDIA
            if (!activity.isNullOrBlank()) {
                DetailInformationSection(
                    label = "Aktivitas",
                    value = activity
                )
            }

            // CATATAN
            DetailInformationSection(
                label = "Catatan",
                value = notes.ifBlank {
                    "Tidak ada catatan."
                }
            )

            // ITEM PAKAIAN - JIKA SUDAH ADA DATA
            if (outfitItems.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Item Pakaian",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailInk
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(
                            1.dp,
                            DetailBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            outfitItems.forEach { item ->
                                Text(
                                    text = "• $item",
                                    color = DetailInk,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // TOMBOL EDIT
            Button(
                onClick = onEditClick,
                enabled = !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailYellow,
                    contentColor = DetailInk
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Edit Riwayat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // TOMBOL HAPUS
            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                enabled = !isDeleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = DetailRed
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = DetailRed,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (isDeleting) "Menghapus..."
                    else "Hapus Riwayat",
                    color = DetailRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    // 3. DIALOG KONFIRMASI DELETE
    if (showDeleteDialog) {
        Dialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 30.dp,
                        vertical = 24.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ICON TEMPAT SAMPAH
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = Color(0xFFFFEFEF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF5D63),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // JUDUL
                    Text(
                        text = "Hapus Riwayat?",
                        color = DetailInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // TOMBOL KONFIRMASI
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteConfirmed()
                        },
                        enabled = !isDeleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5D63),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Ya, Hapus Sekarang",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // TOMBOL BATAL
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                        },
                        enabled = !isDeleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = DetailBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = DetailGray
                        )
                    ) {
                        Text(
                            text = "Batalkan",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

}


// KOMPONEN GAMBAR OUTFIT
@Composable
private fun DetailOutfitImage(
    outfitName: String,
    imageUrl: String?,
    previewRes: Int?,
    modifier: Modifier = Modifier
) {
    val imageShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(imageShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = DetailBorder,
                shape = imageShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Gambar lokal untuk Preview
            previewRes != null -> {
                Image(
                    painter = painterResource(previewRes),
                    contentDescription = "Foto outfit $outfitName",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gambar dari outfit tersimpan
            !imageUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto outfit $outfitName",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Placeholder ketika foto belum tersedia
            else -> {
                Icon(
                    imageVector = Icons.Outlined.Checkroom,
                    contentDescription = "Foto outfit belum tersedia",
                    tint = DetailGray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}


// KOMPONEN INFORMASI DETAIL
@Composable
private fun DetailInformationSection(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DetailInk
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(
                width = 1.dp,
                color = DetailBorder
            )
        ) {
            Text(
                text = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = DetailInk
            )
        }
    }
}


// COMPOSE PREVIEW
@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun WearHistoryDetailPreview() {
    ClosetlyTheme {
        WearHistoryDetailScreen(
            outfitName = "Beige Casual",
            wornDate = LocalDate.of(2026, 11, 10),
            activity = "Kuliah",
            notes = "Outfit nyaman untuk kegiatan kampus.",
            outfitItems = listOf(
                "Blazer Beige",
                "Celana Cream",
                "Sepatu Putih"
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteConfirmed = {}
        )
    }
}