
package vin.pam.closetly.feature.planner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.io.File
import java.security.MessageDigest
import kotlinx.serialization.SerializationException
import vin.pam.closetly.feature.planner.model.PlannerRecord
import vin.pam.closetly.feature.planner.model.PlannerOutfit
import vin.pam.closetly.feature.planner.data.repository.LocalPlannerRepository
import java.io.IOException

private val Yellow = Color(0xFFFFCE19)
private val Ink = Color(0xFF242424)

private val DateFormatter =
    DateTimeFormatter.ofPattern(
        "dd MMM yyyy",
        Locale.ENGLISH
    )

// Database values stay unchanged.
// Only the visible labels are translated.
private fun categoryLabel(value: String): String =
    when (value) {
        "Semua" -> "All"
        "Kuliah" -> "Campus"
        "Lainnya" -> "Other"
        else -> value
    }

private fun statusLabel(value: String): String =
    when (value) {
        "Direncanakan" -> "Planned"
        "Sudah Dipakai" -> "Worn"
        else -> value
    }

private fun displayDate(value: String): String =
    runCatching {
        LocalDate.parse(value).format(DateFormatter)
    }.getOrDefault(value)

private fun plannerError(error: Exception, fallback: String): String = when (error) {
    is SerializationException -> "Saved plans couldn't be read. Existing data has been kept."
    is IOException -> "Couldn't access device storage. Check available space and try again."
    is IllegalArgumentException, is IllegalStateException -> error.message ?: fallback
    else -> fallback
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    accountId: String?,
    onHistoryClick: () -> Unit,
    savedOutfits: List<PlannerOutfit> = emptyList()
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current.applicationContext
    val repository = remember(context, accountId) {
        val owner = accountId ?: "development"
        val folder = MessageDigest.getInstance("SHA-256").digest(owner.toByteArray())
            .joinToString("") { "%02x".format(it) }
        LocalPlannerRepository(File(context.filesDir, "planner/$folder/plans.json"), owner)
    }
    val availableOutfits = savedOutfits
    var requestId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }

    val colors = MaterialTheme.colorScheme

    val plannerBackground = colors.background
    val plannerText = colors.onBackground
    val plannerMuted = colors.onSurfaceVariant
    val plannerBorder = colors.outline

    val nearestBackground =
        if (colors.background.luminance() < 0.5f) {
            Color(0xFF343020)
        } else {
            Color(0xFFFFF8D9)
        }

    val nearestText =
        if (colors.background.luminance() < 0.5f) {
            Color(0xFFF5EAC4)
        } else {
            Ink
        }

    var plans by remember {
        mutableStateOf<List<PlannerRecord>>(emptyList())
    }

    var busy by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var page by remember {
        mutableStateOf("home")
    }

    var selectedPlan by remember {
        mutableStateOf<PlannerRecord?>(null)
    }

    var selectedOutfit by remember {
        mutableStateOf<PlannerOutfit?>(null)
    }

    var activity by remember {
        mutableStateOf("")
    }

    var date by remember {
        mutableStateOf(LocalDate.now().toString())
    }

    var category by remember {
        mutableStateOf("Kuliah")
    }

    var customCategory by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    var status by remember {
        mutableStateOf("Direncanakan")
    }

    var filter by remember {
        mutableStateOf("Semua")
    }

    var confirmDelete by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    BackHandler(enabled = page != "home") {
        if (!busy) page = if (page == "choose") "form" else "home"
    }

    val categories = remember(customCategory, plans) {
        listOf(
            "Semua",
            "Kuliah",
            "Meeting",
            "Hangout",
            "Formal"
        ) + plans.map { it.category } + listOfNotNull(
            customCategory
                .takeIf { it.isNotBlank() }
        )
    }.distinct()

    fun refresh() {
        if (busy) return
        busy = true
        scope.launch {

            try {
                plans = repository.list()
                error = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                error = plannerError(e, "Couldn't load your outfit plans. Try again.")
            } finally {
                busy = false
            }
        }
    }

    // Interactive calendar
    if (showDatePicker) {
        val initialMillis = runCatching {
            LocalDate.parse(date)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrDefault(
            System.currentTimeMillis()
        )

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
                        pickerState.selectedDateMillis
                            ?.let { millis ->
                                date = Instant
                                    .ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                    .toString()
                            }

                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = pickerState
            )
        }
    }

    LaunchedEffect(repository) {
        refresh()
    }

    // Delete confirmation
    if (confirmDelete && selectedPlan != null) {
        AlertDialog(
            onDismissRequest = {
                confirmDelete = false
            },
            title = {
                Text("Delete This Plan?")
            },
            text = {
                Text(
                    "This outfit plan will be permanently " +
                            "deleted. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = selectedPlan?.id
                            ?: return@TextButton

                        confirmDelete = false
                        if (busy) return@TextButton
                        busy = true
                        scope.launch {

                            try {
                                val repo = repository

                                repo.delete(id)
                                plans = plans.filterNot { it.id == id }

                                selectedPlan = null
                                page = "home"
                                error = null
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (e: Exception) {
                                error = plannerError(e, "Couldn't delete this plan. Try again.")
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text(
                        "Yes, Delete",
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                    }
                ) {
                    Text("Keep My Plan")
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
                enabled = !busy,
                onClick = {
                    page =
                        if (page == "choose") "form"
                        else "home"
                }
            ) {
                Text(
                    "‹ Back",
                    color = plannerText
                )
            }
        }

        if (error != null) {
            Text(error.orEmpty(), Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = colors.error)
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

        when (page) {

            // MAIN PLANNER SCREEN
            "home" -> {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Outfit Planner",
                                    style = MaterialTheme
                                        .typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = plannerText
                                )

                                Text(
                                    text = "Plan your looks, own your day.",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = plannerMuted
                                )
                                Text(
                                    "Plans are saved on this device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = plannerMuted,
                                )
                            }

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
                                    "↶ History",
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
                            "Coming Up Next",
                            style = MaterialTheme
                                .typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    item {
                        val nearest = plans
                            .filter {
                                it.plannedDate >=
                                        LocalDate.now().toString()
                            }
                            .minByOrNull {
                                it.plannedDate
                            }

                        if (nearest == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        nearestBackground,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(24.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Your next look is waiting!",
                                    fontWeight = FontWeight.Bold,
                                    color = nearestText
                                )

                                Text(
                                    "Make every occasion a stylish one.",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = nearestText
                                )
                            }
                        } else {
                            PlanCard(nearest) {
                                selectedPlan = nearest
                                page = "detail"
                            }
                        }
                    }

                    item {
                        YellowButton(
                            "+ Plan an Outfit", enabled = !busy,
                        ) {
                            if (busy) return@YellowButton
                            requestId = UUID.randomUUID().toString()
                            activity = ""
                            date = LocalDate.now().toString()
                            category = "Kuliah"
                            customCategory = ""
                            notes = ""
                            status = "Direncanakan"
                            selectedOutfit = null
                            error = null
                            page = "form"
                        }
                    }

                    item {
                        Text(
                            "Categories",
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(
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
                                        Text(
                                            categoryLabel(item)
                                        )
                                    },
                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
                                                selectedContainerColor = Yellow,
                                                selectedLabelColor = Ink
                                            )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Upcoming Outfits",
                            style = MaterialTheme
                                .typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )
                    }

                    val visible = plans.filter {
                        it.plannedDate >=
                                LocalDate.now().toString() &&
                                (
                                        filter == "Semua" ||
                                                it.category == filter
                                        )
                    }

                    if (visible.isEmpty()) {
                        item {
                            Text(
                                "No plans yet. Let's create your next look!",
                                color = plannerMuted
                            )
                        }
                    }

                    items(
                        visible,
                        key = { it.id }
                    ) { plan ->
                        PlanCard(plan) {
                            selectedPlan = plan
                            page = "detail"
                        }
                    }

                    item {
                        TextButton(
                            onClick = {
                                refresh()
                            }
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }

            // SELECT OUTFIT SCREEN
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
                        "My Outfits",
                        style = MaterialTheme
                            .typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Text(
                        "Pick the perfect look for your plans.",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = plannerMuted
                    )

                    Spacer(
                        Modifier.height(16.dp)
                    )

                    if (availableOutfits.isEmpty()) {
                        Text(
                            "Your outfit collection is looking " +
                                    "a little empty. Create a look " +
                                    "in Mix & Match to get started!",
                            color = plannerMuted,
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        items(availableOutfits) { outfit ->
                            Card(
                                onClick = {
                                    pending = outfit
                                },
                                border =
                                    if (pending?.id == outfit.id) {
                                        BorderStroke(
                                            2.dp,
                                            Yellow
                                        )
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
                                        contentDescription =
                                            outfit.name,
                                        modifier =
                                            Modifier.size(72.dp)
                                    )

                                    Spacer(
                                        Modifier.width(12.dp)
                                    )

                                    Column {
                                        Text(
                                            outfit.name,
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            "${outfit.itemCount} pieces",
                                            color = plannerMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    YellowButton(
                        label = "Use This Outfit",
                        enabled = pending != null
                    ) {
                        if (busy) return@YellowButton
                        selectedOutfit = pending
                        page = "form"
                    }
                }
            }

            // CREATE PLAN FORM
            "form" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Plan Your Look",
                        style = MaterialTheme
                            .typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Text(
                        "A great outfit starts with a plan.",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = plannerMuted
                    )

                    Text(
                        "CHOOSE YOUR OUTFIT",
                        style = MaterialTheme
                            .typography.labelMedium,
                        color = plannerText
                    )

                    OutlinedButton(
                        onClick = {
                            page = "choose"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedOutfit?.name
                                ?: "+ Choose from My Outfits",
                            color = plannerText
                        )
                    }

                    OutlinedTextField(
                        value = activity,
                        onValueChange = {
                            activity = it
                        },
                        label = {
                            Text("What's the occasion?")
                        },
                        placeholder = {
                            Text(
                                "e.g. Campus day, coffee date..."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Tap to open the calendar
                    Text(
                        "WHEN WILL YOU WEAR IT?",
                        style = MaterialTheme
                            .typography.labelMedium,
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

                        Spacer(
                            Modifier.width(12.dp)
                        )

                        Text(
                            displayDate(date),
                            modifier = Modifier.weight(1f),
                            color = plannerText
                        )

                        Text(
                            "Select Date",
                            style = MaterialTheme
                                .typography.labelSmall,
                            color = plannerMuted
                        )
                    }

                    Text(
                        "Occasion",
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Row(
                        modifier = Modifier
                            .horizontalScroll(
                                rememberScrollState()
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        categories
                            .filter {
                                it != "Semua"
                            }
                            .forEach { item ->
                                FilterChip(
                                    selected = category == item,
                                    onClick = {
                                        category = item
                                    },
                                    label = {
                                        Text(
                                            categoryLabel(item)
                                        )
                                    },
                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
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
                            Text(
                                "Add a custom occasion (optional)"
                            )
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
                            Text("Add a little note...")
                        },
                        placeholder = {
                            Text(
                                "Anything you'd like to remember?"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text(
                        "Outfit Status",
                        fontWeight = FontWeight.Bold,
                        color = plannerText
                    )

                    Row(
                        modifier = Modifier
                            .horizontalScroll(
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
                                    Text(
                                        statusLabel(item)
                                    )
                                },
                                colors =
                                    FilterChipDefaults
                                        .filterChipColors(
                                            selectedContainerColor = Yellow,
                                            selectedLabelColor = Ink
                                        )
                            )
                        }
                    }

                    YellowButton(
                        label = "Save My Plan",
                        enabled = !busy &&
                                activity.isNotBlank() &&
                                selectedOutfit != null
                    ) {
                        if (busy) return@YellowButton
                        val validDate = runCatching {
                            LocalDate.parse(date)
                        }.isSuccess

                        if (!validDate) {
                            error = "Please select a valid date."
                        } else {
                            busy = true
                            scope.launch {

                                try {
                                    val repo = repository

                                    val saved = repo.add(
                                        requestId,
                                        selectedOutfit,
                                        activity.trim(),
                                        date,
                                        category,
                                        notes,
                                        status
                                    )

                                    plans = (plans.filterNot { it.id == saved.id } + saved).sortedBy { it.plannedDate }
                                    error = null
                                    page = "home"
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (e: Exception) {
                                    error = plannerError(e, "Couldn't save your plan. Try again.")
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    }
                }
            }

            // PLAN DETAILS SCREEN
            "detail" -> {
                val plan = selectedPlan

                if (plan != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Your Outfit Plan",
                            style = MaterialTheme
                                .typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )

                        AsyncImage(
                            model = plan.outfitImageUrl,
                            contentDescription =
                                plan.outfitName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Text(
                            plan.activityName,
                            style = MaterialTheme
                                .typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = plannerText
                        )

                        Text(
                            "${displayDate(plan.plannedDate)} • " +
                                    categoryLabel(plan.category),
                            color = plannerMuted
                        )

                        Text(
                            "Selected Outfit: ${plan.outfitName}",
                            color = plannerText
                        )

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "NOTES",
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                Text(
                                    plan.notes.ifBlank {
                                        "No notes added yet."
                                    }
                                )
                            }
                        }

                        Text(
                            "Status: ${statusLabel(plan.status)}",
                            color = plannerText
                        )

                        OutlinedButton(
                            enabled = !busy,
                            onClick = {
                                confirmDelete = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Delete Plan",
                                color = colors.error
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
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AsyncImage(
                model = plan.outfitImageUrl,
                contentDescription = plan.outfitName,
                modifier = Modifier.size(78.dp)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    displayDate(plan.plannedDate),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    plan.activityName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "${categoryLabel(plan.category)} • " +
                            statusLabel(plan.status),
                    color = MaterialTheme
                        .colorScheme.onSurfaceVariant
                )
            }

            Text(
                "›",
                style = MaterialTheme
                    .typography.headlineMedium,
                color = MaterialTheme
                    .colorScheme.onSurface
            )
        }
    }
}
