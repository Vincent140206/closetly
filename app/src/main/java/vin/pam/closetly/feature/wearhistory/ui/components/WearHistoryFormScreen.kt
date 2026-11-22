package vin.pam.closetly.feature.wearhistory.ui

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vin.pam.closetly.ui.theme.ClosetlyTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.painterResource


private val Yellow = Color(0xFFFACC15)
private val Ink = Color(0xFF171717)
private val Gray = Color(0xFF62625F)
private val Cream = Color(0xFFFAFAF7)
private val FormBorder = Color(0xFFE8E5DC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearHistoryFormScreen(
    outfitName: String,
    outfitImageUrl: String? = null,
    outfitPreviewRes: Int? = null,
    initialDate: LocalDate = LocalDate.now(),
    initialNotes: String = "",
    isEditing: Boolean = false,
    isSaving: Boolean = false,
    onChangeOutfitClick: (() -> Unit)? = null,
    onBack: () -> Unit,
    onSave: (LocalDate, String) -> Unit
) {
    var selectedDate by remember(initialDate) {
        mutableStateOf(initialDate)
    }

    var notes by remember(initialNotes) {
        mutableStateOf(initialNotes)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val localeId = Locale.forLanguageTag("id-ID")
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", localeId)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {

        // 1. HEADER - TETAP DI ATAS, TIDAK IKUT SCROLL
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
            // Area klik 40dp, lingkaran visual 30dp
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = FormBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = if (isEditing) "Edit Riwayat"
                else "Lengkapi Riwayat",
                color = Ink,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                ),
                maxLines = 1
            )
        }

        // 2. AREA KONTEN - BISA SCROLL VERTIKAL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // FOTO OUTFIT TERPILIH
            SelectedOutfitImage(
                imageUrl = outfitImageUrl,
                previewRes = outfitPreviewRes,
                outfitName = outfitName,
                modifier = Modifier.fillMaxWidth()
            )

            // INFORMASI OUTFIT
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Outfit Dipilih",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, FormBorder)
                ) {
                    Text(
                        text = outfitName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Ink,
                        fontSize = 14.sp
                    )
                }
                // 3. TOMBOL GANTI OUTFIT (TAMBAHAN BARU)
                if (onChangeOutfitClick != null) {
                    TextButton(
                        onClick = onChangeOutfitClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Ganti Outfit",
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // TANGGAL PEMAKAIAN
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tanggal Pemakaian",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Ink
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, FormBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDate.format(dateFormatter),
                            fontSize = 14.sp,
                            color = Ink
                        )

                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pilih tanggal",
                            tint = Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // CATATAN
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Catatan (Opsional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            text = "Ceritakan outfit yang kamu pakai...",
                            fontSize = 14.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // TOMBOL SIMPAN
            Button(
                onClick = {
                    onSave(selectedDate, notes.trim())
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow,
                    contentColor = Ink
                )
            ) {
                Text(
                    text = when {
                        isSaving -> "Menyimpan..."
                        isEditing -> "Simpan Perubahan"
                        else -> "Simpan Riwayat"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }


    // DIALOG PEMILIHAN TANGGAL
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SelectedOutfitImage(
    imageUrl: String?,
    previewRes: Int?,
    outfitName: String,
    modifier: Modifier = Modifier
) {
    val imageShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(imageShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = FormBorder,
                shape = imageShape
            )
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Gambar lokal untuk Compose Preview
            previewRes != null -> {
                Image(
                    painter = painterResource(id = previewRes),
                    contentDescription = "Foto outfit $outfitName",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gambar outfit tersimpan dari Supabase
            !imageUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto outfit $outfitName",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Jika foto belum tersedia
            else -> {
                Icon(
                    imageVector = Icons.Outlined.Checkroom,
                    contentDescription = "Foto outfit belum tersedia",
                    tint = Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)

@Composable
private fun WearHistoryFormPreview() {
    ClosetlyTheme {
        WearHistoryFormScreen(
            outfitName = "Campus Casual",
            initialDate = LocalDate.of(2026, 11, 10),
            initialNotes = "",
            onBack = {},
            onSave = { _, _ -> }
        )
    }
}


@Preview(
    name = "Edit Wear History",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun EditWearHistoryPreview() {
    ClosetlyTheme {
        WearHistoryFormScreen(
            outfitName = "Campus Casual",
            initialDate = LocalDate.of(2026, 11, 9),
            initialNotes = "Outfit nyaman untuk kelas seharian.",
            isEditing = true,
            onChangeOutfitClick = {},
            onBack = {},
            onSave = { _, _ -> }
        )
    }
}