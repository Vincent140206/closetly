package vin.pam.closetly.feature.wearhistory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vin.pam.closetly.feature.wearhistory.model.WearHistory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Yellow = Color(0xFFFACC15)
private val Ink = Color(0xFF171717)
private val Border = Color(0xFFE8E5DC)
private val Gray = Color(0xFF62625F)
private val LocaleId = Locale.forLanguageTag("id-ID")

/**
 * UI kalender foto outfit. Tanggal dihitung dengan java.time, bukan hardcoded.
 * Klik tanggal bergambar membuka detail; tanggal kosong hanya menjadi selected.
 */
@Composable
fun OutfitCalendar(
    month: YearMonth,
    selectedDate: LocalDate?,
    histories: List<WearHistory>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val offset = month.atDay(1).dayOfWeek.value - 1 // Senin = 0, Minggu = 6
    val totalCells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    val byDate = histories.groupBy { it.wornDate }
    val monthName = month.atDay(1)
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", LocaleId))
        .replaceFirstChar { it.titlecase(LocaleId) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Border),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Bulan sebelumnya")
                }
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Bulan berikutnya")
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN").forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        ),
                        color = Gray,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            (0 until totalCells).chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { cellIndex ->
                        val dateNumber = cellIndex - offset + 1
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(1.dp)) {
                            if (dateNumber in 1..month.lengthOfMonth()) {
                                val date = month.atDay(dateNumber)
                                val records = byDate[date].orEmpty()
                                CalendarDate(
                                    date = date,
                                    histories = records,
                                    isSelected = selectedDate == date,
                                    onClick = { onDayClick(date, records.firstOrNull()?.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDate(
    date: LocalDate,
    histories: List<WearHistory>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier.fillMaxSize().clip(shape)
            .then(if (isSelected) Modifier.border(2.dp, Yellow, shape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (histories.isEmpty()) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Ink,
            )
        } else {
            OutfitThumbnail(
                history = histories.first(),
                modifier = Modifier.fillMaxSize().padding(3.dp).clip(RoundedCornerShape(7.dp)),
            )
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier.align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(bottomEnd = 6.dp))
                    .padding(horizontal = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = Ink,
            )
            if (histories.size > 1) {
                Text(
                    text = "+${histories.size - 1}",
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .background(Yellow, RoundedCornerShape(topStart = 6.dp))
                        .padding(horizontal = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Ink,
                )
            }
        }
    }
}

/** Gambar foto outfit. Jika hanya ada path Storage, tampilkan fallback dahulu. */
@Composable
fun OutfitThumbnail(history: WearHistory, modifier: Modifier = Modifier) {
    val url = history.imageUrl
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = "Foto outfit ${history.outfitName}",
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFF2F0E9)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Checkroom,
                contentDescription = "Gambar outfit belum tersedia",
                tint = Gray,
            )
        }
    }
}
