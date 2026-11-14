
package vin.pam.closetly.feature.planner.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val Yellow = Color(0xFFFFCE19)
private val Ink = Color(0xFF242424)
private val DateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Serializable
data class PlannerRecord(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: String? = null,
    @SerialName("outfit_name") val outfitName: String = "",
    @SerialName("outfit_image_url") val outfitImageUrl: String? = null,
    @SerialName("activity_name") val activityName: String,
    @SerialName("planned_date") val plannedDate: String,
    val category: String = "Lainnya",
    val notes: String = "",
    val status: String = "Direncanakan"
)

@Serializable
private data class NewPlannerRecord(
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: String? = null,
    @SerialName("outfit_name") val outfitName: String,
    @SerialName("outfit_image_url") val outfitImageUrl: String? = null,
    @SerialName("activity_name") val activityName: String,
    @SerialName("planned_date") val plannedDate: String,
    val category: String,
    val notes: String,
    val status: String
)

data class PlannerOutfit(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val itemCount: Int = 0
)

private class PlannerRepository(
    private val client: SupabaseClient
) {
    private fun userId(): String =
        requireNotNull(client.auth.currentUserOrNull()?.id) {
            "Silakan login terlebih dahulu."
        }

    suspend fun list(): List<PlannerRecord> =
        client.from("outfit_plans").select {
            filter {
                eq("user_id", userId())
            }
        }.decodeList<PlannerRecord>()
            .sortedBy { it.plannedDate }

    suspend fun add(
        outfit: PlannerOutfit?,
        name: String,
        date: String,
        category: String,
        notes: String,
        status: String
    ) {
        client.from("outfit_plans").insert(
            NewPlannerRecord(
                userId = userId(),
                outfitId = outfit?.id,
                outfitName = outfit?.name ?: "",
                outfitImageUrl = outfit?.imageUrl,
                activityName = name,
                plannedDate = date,
                category = category,
                notes = notes,
                status = status
            )
        )
    }

    suspend fun delete(id: String) {
        client.from("outfit_plans").delete {
            filter {
                eq("id", id)
                eq("user_id", userId())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    supabase: SupabaseClient?,
    onHistoryClick: () -> Unit,
    savedOutfits: List<PlannerOutfit> = emptyList()
) {
    val scope = rememberCoroutineScope()
    val repository = remember(supabase) {
        supabase?.let(::PlannerRepository)
    }

    val colors = MaterialTheme.colorScheme
    val darkMode = isSystemInDarkTheme()

    val plannerBackground = colors.background
    val plannerText = colors.onBackground
    val plannerMuted = colors.onSurfaceVariant
    val plannerBorder = colors.outline

    val nearestBackground =
        if (darkMode) Color(0xFF343020)
        else Color(0xFFFFF8D9)

    var plans by remember {
        mutableStateOf<List<PlannerRecord>>(emptyList())
    }

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var page by remember { mutableStateOf("home") }
    var selectedPlan by remember {
        mutableStateOf<PlannerRecord?>(null)
    }

    var selectedOutfit by remember {
        mutableStateOf<PlannerOutfit?>(null)
    }

    var activity by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(LocalDate.now().toString())
    }

    var category by remember { mutableStateOf("Kuliah") }
    var customCategory by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Direncanakan") }
    var filter by remember { mutableStateOf("Semua") }

    var confirmDelete by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = remember(customCategory) {
        listOf(
            "Semua",
            "Kuliah",
            "Meeting",
            "Hangout",
            "Formal"
        ) + listOfNotNull(
            customCategory.takeIf { it.isNotBlank() }
        )
    }

    fun refresh() {
        if (repository == null) {
            error = "Konfigurasi Supabase belum tersedia."
            return
        }

        scope.launch {
            busy = true
            try {
                plans = repository.list()
                error = null
            } catch (e: Exception) {
                error = e.message ?: "Gagal memuat rencana"
            }
            busy = false
        }
    }

    // Kalender muncul ketika kolom tanggal ditekan
    if (showDatePicker) {
        val initialMillis = runCatching {
            LocalDate.parse(date)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())

        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    LaunchedEffect(repository) {
        refresh()
    }

    // Dialog konfirmasi hapus
    if (confirmDelete && selectedPlan != null) {
        AlertDialog(
            onDismissRequest = {
                confirmDelete = false
            },
            title = {
                Text("Hapus Rencana?")
            },
            text = {
                Text("Rencana ini akan dihapus permanen.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = selectedPlan?.id
                            ?: return@TextButton

                        confirmDelete = false

                        scope.launch {
                            busy = true
                            try {
                                repository?.delete(id)
                                plans = repository?.list().orEmpty()
                                selectedPlan = null
                                page = "home"
                                error = null
                            } catch (e: Exception) {
                                error = e.message
                            }
                            busy = false
                        }
                    }
                ) {
                    Text(
                        "Ya, Hapus Sekarang",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                    }
                ) {
                    Text("Batalkan")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(plannerBackground)
    ) {
        if (page != "home") {
            TextButton(
                onClick = {
                    page = if (page == "choose") "form" else "home"
                }
            ) {
                Text(
                    "‹ Kembali",
                    color = plannerText
                )
            }
        }

        when (page) {

            // HALAMAN UTAMA PLANNER
            "home" -> {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Rencana Outfit",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = plannerText
                                )

                                Text(
                                    text = "Atur outfit untuk aktivitas mendatang.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = plannerMuted
                                )
                            }

                            // Tombol Riwayat ringkas dan satu baris
                            OutlinedButton(
                                onClick = onHistoryClick,
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 10.dp,
                                    vertical = 0.dp
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    plannerBorder
                                )
                            ) {
                                Text(
                                    text = "↶ Riwayat",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = plannerText
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Jadwal Terdekat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    item {
                        val nearest = plans
                            .filter {
                                it.plannedDate >= LocalDate.now().toString()
                            }
                            .minByOrNull { it.plannedDate }

                        if (nearest == null) {
                            Text(
                                text = "Belum ada jadwal mendatang nih",
                                color = if (darkMode) {
                                    Color(0xFFF5EAC4)
                                } else {
                                    Ink
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        nearestBackground,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(24.dp)
                            )
                        } else {
                            PlanCard(nearest) {
                                selectedPlan = nearest
                                page = "detail"
                            }
                        }
                    }

                    item {
                        YellowButton("+ Buat Rencana") {
                            activity = ""
                            date = LocalDate.now().toString()
                            notes = ""
                            status = "Direncanakan"
                            selectedOutfit = null
                            error = null
                            page = "form"
                        }
                    }

                    item {
                        Text(
                            "Kategori",
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(
                                rememberScrollState()
                            ),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { item ->
                                FilterChip(
                                    selected = filter == item,
                                    onClick = {
                                        filter = item
                                    },
                                    label = {
                                        Text(item)
                                    },
                                    colors =
                                        FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Yellow,
                                            selectedLabelColor = Ink
                                        )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Rencana Mendatang",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    val visible = plans.filter {
                        it.plannedDate >= LocalDate.now().toString() &&
                                (filter == "Semua" || it.category == filter)
                    }

                    if (visible.isEmpty()) {
                        item {
                            Text(
                                "Belum ada rencana pada kategori ini.",
                                color = plannerMuted
                            )
                        }
                    }

                    items(visible, key = { it.id }) { plan ->
                        PlanCard(plan) {
                            selectedPlan = plan
                            page = "detail"
                        }
                    }

                    if (busy) {
                        item {
                            CircularProgressIndicator()
                        }
                    }

                    if (error != null) {
                        item {
                            Text(
                                error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    item {
                        TextButton(
                            onClick = { refresh() }
                        ) {
                            Text("Muat ulang")
                        }
                    }
                }
            }

            // HALAMAN MEMILIH OUTFIT
            "choose" -> {
                var pending by remember {
                    mutableStateOf<PlannerOutfit?>(null)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        "Outfit-ku",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    if (savedOutfits.isEmpty()) {
                        Text(
                            "Belum ada outfit yang tersedia. " +
                                    "Daftar ini akan terisi setelah data " +
                                    "Mix & Match dihubungkan.",
                            color = plannerMuted,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedOutfits) { outfit ->
                            Card(
                                onClick = {
                                    pending = outfit
                                },
                                border = if (pending?.id == outfit.id) {
                                    BorderStroke(2.dp, Yellow)
                                } else {
                                    null
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = outfit.imageUrl,
                                        contentDescription = outfit.name,
                                        modifier = Modifier.size(72.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            outfit.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("${outfit.itemCount} item")
                                    }
                                }
                            }
                        }
                    }

                    YellowButton(
                        label = "Pilih",
                        enabled = pending != null
                    ) {
                        selectedOutfit = pending
                        page = "form"
                    }
                }
            }

            // FORM BUAT RENCANA
            "form" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Buat Rencana Outfit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Text(
                        "PILIH OUTFIT",
                        style = MaterialTheme.typography.labelMedium,
                        color = plannerText
                    )

                    OutlinedButton(
                        onClick = {
                            page = "choose"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedOutfit?.name ?: "+ Pilih dari Outfit-ku",
                            color = plannerText
                        )
                    }

                    OutlinedTextField(
                        value = activity,
                        onValueChange = {
                            activity = it
                        },
                        label = {
                            Text("Nama kegiatan")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Kolom tanggal membuka kalender
                    Text(
                        "TANGGAL KEGIATAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = plannerText
                    )

                    OutlinedButton(
                        onClick = {
                            showDatePicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = BorderStroke(
                            1.dp,
                            plannerBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp
                        )
                    ) {
                        Text(
                            "▦",
                            color = colors.primary
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = runCatching {
                                LocalDate.parse(date)
                                    .format(DateFormatter)
                            }.getOrDefault(date),
                            modifier = Modifier.weight(1f),
                            color = plannerText
                        )

                        Text(
                            "Pilih tanggal",
                            style = MaterialTheme.typography.labelSmall,
                            color = plannerMuted
                        )
                    }

                    Text(
                        "Kategori",
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(
                            rememberScrollState()
                        ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        categories
                            .filter { it != "Semua" }
                            .forEach { item ->
                                FilterChip(
                                    selected = category == item,
                                    onClick = {
                                        category = item
                                    },
                                    label = {
                                        Text(item)
                                    },
                                    colors =
                                        FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Yellow,
                                            selectedLabelColor = Ink
                                        )
                                )
                            }
                    }

                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = {
                            customCategory = it
                        },
                        label = {
                            Text("Tambah kategori (opsional)")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                        },
                        label = {
                            Text("Catatan")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text(
                        "Status",
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(
                            rememberScrollState()
                        ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Direncanakan",
                            "Sudah Dipakai"
                        ).forEach { item ->
                            FilterChip(
                                selected = status == item,
                                onClick = {
                                    status = item
                                },
                                label = {
                                    Text(item)
                                },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Yellow,
                                        selectedLabelColor = Ink
                                    )
                            )

                            Spacer(Modifier.width(8.dp))
                        }
                    }

                    if (error != null) {
                        Text(
                            error.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    YellowButton(
                        label = "Simpan",
                        enabled = !busy &&
                                activity.isNotBlank() &&
                                selectedOutfit != null
                    ) {
                        val validDate = runCatching {
                            LocalDate.parse(date)
                        }.isSuccess

                        if (!validDate) {
                            error = "Tanggal tidak valid."
                        } else {
                            scope.launch {
                                busy = true

                                try {
                                    val repo = repository
                                        ?: throw IllegalStateException(
                                            "Supabase belum dikonfigurasi"
                                        )

                                    repo.add(
                                        selectedOutfit,
                                        activity.trim(),
                                        date,
                                        category,
                                        notes,
                                        status
                                    )

                                    plans = repo.list()
                                    error = null
                                    page = "home"
                                } catch (e: Exception) {
                                    error = e.message
                                        ?: "Gagal menyimpan"
                                }

                                busy = false
                            }
                        }
                    }
                }
            }

            // DETAIL RENCANA
            "detail" -> {
                val plan = selectedPlan

                if (plan != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Detail Rencana",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )

                        AsyncImage(
                            model = plan.outfitImageUrl,
                            contentDescription = plan.outfitName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Text(
                            plan.activityName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )

                        Text(
                            "${plan.plannedDate} • ${plan.category}",
                            color = plannerMuted
                        )

                        Text(
                            "Outfit: ${plan.outfitName}",
                            color = plannerText
                        )

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("CATATAN")
                                Text(
                                    plan.notes.ifBlank {
                                        "Tidak ada catatan"
                                    }
                                )
                            }
                        }

                        Text(
                            "Status: ${plan.status}",
                            color = plannerText
                        )

                        OutlinedButton(
                            onClick = {
                                confirmDelete = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hapus Rencana",
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YellowButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Yellow,
            contentColor = Ink
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlanCard(
    plan: PlannerRecord,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = plan.outfitImageUrl,
                contentDescription = plan.outfitName,
                modifier = Modifier.size(78.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    plan.plannedDate,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    plan.activityName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "${plan.category} • ${plan.status}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "›",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
