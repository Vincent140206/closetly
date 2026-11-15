package vin.pam.closetly.feature.wearhistory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.SupabaseClient
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.BlendModeColorFilter
import vin.pam.closetly.feature.wearhistory.data.repository.SupabaseWearHistoryRepository
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import vin.pam.closetly.feature.wearhistory.ui.components.OutfitCalendar
import vin.pam.closetly.feature.wearhistory.ui.components.OutfitThumbnail
import vin.pam.closetly.ui.theme.ClosetlyTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import vin.pam.closetly.feature.wearhistory.ui.components.WearHistoryDetailScreen

private val Yellow = Color(0xFFFACC15)
private val Ink = Color(0xFF171717)
private val Gray = Color(0xFF62625F)
private val Cream = Color(0xFFFAFAF7)
private val Border = Color(0xFFE8E5DC)
private val LocaleId = Locale.forLanguageTag("id-ID")

/**
 * Destination yang dipanggil AppNavHost.
 * Menggunakan SupabaseClient dari AppContainer; bukan membuat client kedua.
 */
private enum class WearHistoryPage {
    MAIN,
    CHOOSE_OUTFIT,
    ADD_FORM,
    DETAIL,
    EDIT_FORM
}


@Composable
fun WearHistoryDestination(
    supabaseClient: SupabaseClient?,
    userId: String?,
    onBack: () -> Unit
) {

    // KONDISI BELUM LOGIN
    if (supabaseClient == null || userId.isNullOrBlank()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Riwayat Pemakaian")
            Text("Silakan login terlebih dahulu.")

            TextButton(onClick = onBack) {
                Text("Kembali")
            }
        }
        return
    }

    // REPOSITORY & VIEWMODEL
    val repository = remember(supabaseClient) {
        SupabaseWearHistoryRepository(supabaseClient)
    }

    val viewModel: WearHistoryViewModel = viewModel(
        key = "wearhistory-$userId",
        factory = WearHistoryViewModel.factory(
            repository,
            userId
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // STATE NAVIGASI
    var currentPage by rememberSaveable {
        mutableStateOf(WearHistoryPage.MAIN)
    }

    var selectedHistoryId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedOutfitId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isChoosingForEdit by rememberSaveable {
        mutableStateOf(false)
    }

    // Pesan sementara hingga fungsi CRUD dipasang
    var temporaryMessage by remember {
        mutableStateOf<String?>(null)
    }

    val selectedHistory = uiState.histories.firstOrNull {
        it.id == selectedHistoryId
    }

    val selectedOutfit = uiState.savedOutfits.firstOrNull {
        it.id == selectedOutfitId
    }

    // NAVIGASI KEMBALI
    fun goBack() {
        currentPage = when (currentPage) {
            WearHistoryPage.MAIN -> WearHistoryPage.MAIN

            WearHistoryPage.CHOOSE_OUTFIT ->
                if (isChoosingForEdit) {
                    WearHistoryPage.EDIT_FORM
                } else {
                    WearHistoryPage.MAIN
                }

            WearHistoryPage.ADD_FORM ->
                WearHistoryPage.CHOOSE_OUTFIT

            WearHistoryPage.DETAIL ->
                WearHistoryPage.MAIN

            WearHistoryPage.EDIT_FORM ->
                WearHistoryPage.DETAIL
        }
    }

    // Tombol Back fisik / gesture Android
    BackHandler(
        enabled = currentPage != WearHistoryPage.MAIN
    ) {
        goBack()
    }

    // PILIH SCREEN SESUAI STATE
    when (currentPage) {

        // 1. KALENDER UTAMA
        WearHistoryPage.MAIN -> {
            WearHistoryScreen(
                uiState = uiState,
                onBackClick = onBack,

                onAddClick = {
                    isChoosingForEdit = false
                    selectedOutfitId = null
                    viewModel.loadSavedOutfits()
                    currentPage = WearHistoryPage.CHOOSE_OUTFIT
                },

                onHistoryClick = { historyId ->
                    selectedHistoryId = historyId
                    currentPage = WearHistoryPage.DETAIL
                },

                onPreviousMonth = {
                    viewModel.changeMonth(-1)
                },

                onNextMonth = {
                    viewModel.changeMonth(1)
                },

                onDaySelected = viewModel::selectDate,
                onRetry = viewModel::refresh
            )
        }

        // 2. PILIH OUTFIT TERSIMPAN
        WearHistoryPage.CHOOSE_OUTFIT -> {

            if (uiState.outfitError != null &&
                !uiState.isLoadingOutfits
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Gagal memuat outfit")
                    Text(uiState.outfitError.orEmpty())

                    TextButton(
                        onClick = viewModel::loadSavedOutfits
                    ) {
                        Text("Coba Lagi")
                    }

                    TextButton(onClick = ::goBack) {
                        Text("Kembali")
                    }
                }
            } else {
                AddWearHistoryScreen(
                    outfits = uiState.savedOutfits.map { outfit ->
                        SavedOutfitOption(
                            id = outfit.id,
                            name = outfit.name,
                            description = outfit.description,
                            coverImageUrl = outfit.imageUrl
                        )
                    },

                    isLoading = uiState.isLoadingOutfits,

                    onBackClick = ::goBack,

                    onOutfitSelected = { outfitId ->
                        selectedOutfitId = outfitId

                        currentPage =
                            if (isChoosingForEdit) {
                                WearHistoryPage.EDIT_FORM
                            } else {
                                WearHistoryPage.ADD_FORM
                            }
                    }
                )
            }
        }

        // 3. FORM TAMBAH RIWAYAT
        WearHistoryPage.ADD_FORM -> {

            if (selectedOutfit != null) {
                WearHistoryFormScreen(
                    outfitName = selectedOutfit.name,
                    outfitImageUrl = selectedOutfit.imageUrl,
                    initialDate =
                        uiState.selectedDate ?: LocalDate.now(),

                    onBack = ::goBack,

                    onSave = { _, _ ->
                        temporaryMessage =
                            "Navigasi Add sudah berhasil. " +
                                    "Operasi Insert Supabase " +
                                    "belum dihubungkan."
                    }
                )
            } else {
                TextButton(onClick = ::goBack) {
                    Text("Kembali memilih outfit")
                }
            }
        }

        // 4. DETAIL RIWAYAT
        WearHistoryPage.DETAIL -> {

            if (selectedHistory != null) {
                WearHistoryDetailScreen(
                    outfitName = selectedHistory.outfitName,
                    wornDate = selectedHistory.wornDate,
                    notes = selectedHistory.notes,
                    imageUrl = selectedHistory.imageUrl,

                    activity = selectedHistory.activity
                        .takeUnless { it == "Manual" },

                    onBackClick = ::goBack,

                    onEditClick = {
                        selectedOutfitId = selectedHistory.outfitId
                        currentPage = WearHistoryPage.EDIT_FORM
                    },

                    onDeleteConfirmed = {
                        temporaryMessage =
                            "Konfirmasi Delete berhasil dibuka. " +
                                    "Operasi Delete Supabase " +
                                    "belum dihubungkan."
                    }
                )
            } else {
                TextButton(onClick = ::goBack) {
                    Text("Kembali ke kalender")
                }
            }
        }

        // 5. FORM EDIT RIWAYAT
        WearHistoryPage.EDIT_FORM -> {

            if (selectedHistory != null) {
                WearHistoryFormScreen(
                    outfitName = selectedOutfit?.name
                        ?: selectedHistory.outfitName,

                    outfitImageUrl = selectedOutfit?.imageUrl
                        ?: selectedHistory.imageUrl,

                    initialDate = selectedHistory.wornDate,
                    initialNotes = selectedHistory.notes,

                    isEditing = true,

                    onChangeOutfitClick = {
                        isChoosingForEdit = true
                        viewModel.loadSavedOutfits()
                        currentPage = WearHistoryPage.CHOOSE_OUTFIT
                    },

                    onBack = ::goBack,

                    onSave = { _, _ ->
                        temporaryMessage =
                            "Navigasi Edit sudah berhasil. " +
                                    "Operasi Update Supabase " +
                                    "belum dihubungkan."
                    }
                )
            } else {
                TextButton(onClick = ::goBack) {
                    Text("Kembali ke detail")
                }
            }
        }
    }

    // PESAN SEMENTARA SELAMA PENGUJIAN NAVIGASI
    temporaryMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                temporaryMessage = null
            },

            title = {
                Text("Tahap Navigasi")
            },

            text = {
                Text(message)
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        temporaryMessage = null
                    }
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}


/** View: hanya menerima UI state dan event, tidak mengakses Supabase. */
@Composable
fun WearHistoryScreen(
    uiState: WearHistoryUiState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Area klik 48dp, lingkaran visual 30dp
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "Riwayat Pemakaian",
                color = Ink,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                ),
                maxLines = 1
            )
        }

        Spacer(Modifier.height(4.dp))

        // SUBTITLE — DI LUAR ROW HEADER
        Text(
            text = "Lihat kembali outfit yang sudah kamu pakai.",
            modifier = Modifier.padding(start = 8.dp),
            color = Gray,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        )

        Spacer(Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow)
            }
        } else if (uiState.errorMessage != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gagal memuat dari Supabase:", color = MaterialTheme.colorScheme.error)
                Text(uiState.errorMessage, color = Gray)
                TextButton(onClick = onRetry) { Text("Coba lagi") }
            }
        } else {
            OutfitCalendar(
                month = uiState.visibleMonth,
                selectedDate = uiState.selectedDate,
                histories = uiState.historiesThisMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDayClick = { date, recordId ->
                    onDaySelected(date)
                    if (recordId != null) onHistoryClick(recordId)
                },
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink),
        ) {
            Text("Tambah Riwayat", fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(8.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Riwayat Terbaru", color = Ink,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(10.dp))

        if (uiState.histories.isEmpty() && !uiState.isLoading && uiState.errorMessage == null) {
            Text(
                "Belum ada riwayat. Tambahkan outfit yang sudah kamu pakai.",
                modifier = Modifier.padding(vertical = 20.dp),
                color = Gray,
            )
        } else if (uiState.errorMessage == null) {
            uiState.histories.take(5).forEach { item ->
                HistoryCard(history = item, onClick = { onHistoryClick(item.id) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Kartu yang ditampilkan ulang untuk setiap objek WearHistory. */

@Composable
private fun HistoryCard(
    history: WearHistory,
    onClick: () -> Unit
) {
    // Mengubah tanggal menjadi format Indonesia
    val dateLabel = history.wornDate
        .format(
            DateTimeFormatter.ofPattern(
                "EEEE, dd MMM yyyy",
                LocaleId
            )
        )
        .replaceFirstChar { it.titlecase(LocaleId) }

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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // 1. THUMBNAIL OUTFIT
            OutfitThumbnail(
                history = history,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            // 2. INFORMASI RIWAYAT
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                // TANGGAL + KATEGORI DALAM SATU BARIS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = dateLabel,
                        modifier = Modifier.weight(1f),
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Badge aktivitas: Kuliah / Meeting
                    Text(
                        text = history.activity,
                        color = Ink,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                Color(0xFFFFFBEB),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                0.5.dp,
                                Yellow.copy(alpha = 0.6f),
                                RoundedCornerShape(5.dp)
                            )
                            .padding(
                                horizontal = 6.dp,
                                vertical = 3.dp
                            )
                    )
                }

                // DESKRIPSI OUTFIT
                Text(
                    text = history.outfitName,
                    color = Gray,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. IKON MENUJU DETAIL RIWAYAT
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Lihat detail riwayat",
                tint = Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


/** Preview hanya simulasi UI; callback kosong, tidak mengubah database. */

@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun WearHistoryPreview() {
    ClosetlyTheme {
        WearHistoryScreen(
            uiState = WearHistoryUiState(
                visibleMonth = YearMonth.of(2026, 11),
                selectedDate = LocalDate.of(2026, 11, 10),
                histories = listOf(
                    WearHistory(
                        id = "preview-1",
                        outfitId = "preview-outfit",
                        outfitName = "Campus Casual",
                        wornDate = LocalDate.of(2026, 11, 10),
                        activity = "Kuliah",
                        notes = "Outfit nyaman untuk kuliah"
                    )
                ),
                isLoading = false
            ),
            onBackClick = {},
            onAddClick = {},
            onHistoryClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDaySelected = {},
            onRetry = {}
        )
    }
}
