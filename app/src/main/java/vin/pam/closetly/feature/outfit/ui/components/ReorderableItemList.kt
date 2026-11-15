package vin.pam.closetly.feature.outfit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.ui.theme.ClosetlySpacing
import kotlin.math.roundToInt

private val RowHeight = 60.dp
private val RowGap = ClosetlySpacing.Sm

/**
 * Daftar item terpilih yang urutannya bisa diatur dengan menggeser ikon ≡.
 * Tinggi baris tetap, jadi perpindahan dihitung dari jarak geser / (tinggi + gap).
 * Pengguna TalkBack mendapat aksi "Pindah ke atas/bawah".
 */
@Composable
fun ReorderableItemList(
    items: List<ClothingOption>,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (itemId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestItems by rememberUpdatedState(items)
    val latestOnMove by rememberUpdatedState(onMove)
    val stepPx = with(LocalDensity.current) { (RowHeight + RowGap).toPx() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(RowGap)) {
        items.forEachIndexed { index, item ->
            key(item.id) {
                val dragging = draggingId == item.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RowHeight)
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging) dragOffset else 0f }
                        .semantics {
                            customActions = buildList {
                                if (index > 0) add(
                                    CustomAccessibilityAction("Pindah ke atas") {
                                        latestOnMove(index, index - 1); true
                                    },
                                )
                                if (index < items.lastIndex) add(
                                    CustomAccessibilityAction("Pindah ke bawah") {
                                        latestOnMove(index, index + 1); true
                                    },
                                )
                            }
                        },
                    shape = MaterialTheme.shapes.small,
                    color = colors.surface,
                    shadowElevation = if (dragging) 8.dp else 0.dp,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier.padding(start = ClosetlySpacing.Sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ClothingThumb(item, Modifier.size(40.dp), toneIndex = index)
                        Column(Modifier.weight(1f).padding(horizontal = ClosetlySpacing.Md)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onRemove(item.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Hapus ${item.name} dari outfit",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Geser untuk mengubah urutan ${item.name}" }
                                .pointerInput(item.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingId = item.id
                                            dragOffset = 0f
                                        },
                                        onDragEnd = { draggingId = null; dragOffset = 0f },
                                        onDragCancel = { draggingId = null; dragOffset = 0f },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y
                                        val from = latestItems.indexOfFirst { it.id == item.id }
                                        if (from >= 0) {
                                            val target = (from + (dragOffset / stepPx).roundToInt())
                                                .coerceIn(0, latestItems.lastIndex)
                                            if (target != from) {
                                                latestOnMove(from, target)
                                                // Posisi baris ikut bergeser; kurangi offset agar tidak melompat.
                                                dragOffset -= (target - from) * stepPx
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            DragHandleIcon(tint = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DragHandleIcon(tint: androidx.compose.ui.graphics.Color) {
    Canvas(Modifier.size(width = 18.dp, height = 12.dp)) {
        val stroke = 2.dp.toPx()
        val ys = listOf(stroke / 2, size.height / 2, size.height - stroke / 2)
        ys.forEach { y ->
            drawLine(
                color = tint,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}
