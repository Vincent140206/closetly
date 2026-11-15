package vin.pam.closetly.feature.outfit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.feature.outfit.model.ClothingOption
import vin.pam.closetly.ui.theme.ClosetlySpacing

/**
 * Placeholder foto pakaian.
 * TODO: tampilkan foto dari Storage (imagePath -> signed URL) dengan Coil
 * setelah repository Supabase siap. Jangan simpan signed URL di database.
 */
@Composable
fun ClothingThumb(
    item: ClothingOption,
    modifier: Modifier = Modifier,
    toneIndex: Int = 0,
) {
    val colors = MaterialTheme.colorScheme
    val tones = listOf(
        colors.surfaceVariant,
        colors.primaryContainer.copy(alpha = 0.5f),
        colors.secondaryContainer,
    )
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(tones[toneIndex % tones.size]),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_closetly_wardrobe),
            contentDescription = item.name,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

/** Kolase pratinjau outfit (maksimal 4 item). */
@Composable
fun OutfitCollage(
    items: List<ClothingOption>,
    modifier: Modifier = Modifier,
) {
    val shown = items.take(4)
    val gap = ClosetlySpacing.Xs
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.background)
            .padding(gap),
    ) {
        when (shown.size) {
            0 -> Icon(
                painter = painterResource(R.drawable.ic_closetly_outfit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
            1 -> ClothingThumb(shown[0], Modifier.fillMaxSize())
            2 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                shown.forEachIndexed { index, item ->
                    ClothingThumb(item, Modifier.weight(1f).fillMaxHeight(), index)
                }
            }
            3 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                ClothingThumb(shown[0], Modifier.weight(1f).fillMaxHeight(), 0)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    ClothingThumb(shown[1], Modifier.weight(1f).fillMaxWidth(), 1)
                    ClothingThumb(shown[2], Modifier.weight(1f).fillMaxWidth(), 2)
                }
            }
            else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
                shown.chunked(2).forEachIndexed { rowIndex, pair ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        pair.forEachIndexed { columnIndex, item ->
                            ClothingThumb(
                                item = item,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                toneIndex = rowIndex * 2 + columnIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 2.dp): Modifier =
    drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)),
            ),
        )
    }
