package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.CollectionSummary
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette

/** Cover image of a collection: its latest saved post, or a soft yellow block when empty. */
@Composable
fun CollectionThumb(cover: InspirationPost?, modifier: Modifier = Modifier) {
    Box(modifier.background(InspirationPalette.SoftYellow)) {
        if (cover != null) PostImage(cover, Modifier.fillMaxSize())
    }
}

@Composable
fun CollectionCard(summary: CollectionSummary, onClick: () -> Unit) {
    Column(Modifier.width(108.dp).clickable(onClick = onClick)) {
        CollectionThumb(
            summary.cover,
            Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(14.dp))
                .border(1.dp, InspirationPalette.Border, RoundedCornerShape(14.dp)),
        )
        Text(
            summary.collection.name, Modifier.padding(top = 6.dp),
            color = InspirationPalette.Heading, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Text("${summary.postCount} item", color = InspirationPalette.Muted, fontSize = 11.sp)
    }
}

/** The "+" card at the start of the collections row. */
@Composable
fun AddCollectionCard(onClick: () -> Unit) {
    Column(Modifier.width(108.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(14.dp))
                .background(InspirationPalette.Vanilla)
                .dashedBorder(InspirationPalette.Pressed, 14.dp),
            contentAlignment = Alignment.Center,
        ) { Text("+", fontSize = 30.sp, color = InspirationPalette.Pressed) }
        Text("Baru", Modifier.padding(top = 6.dp), color = InspirationPalette.Heading, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text("Buat koleksi", color = InspirationPalette.Muted, fontSize = 11.sp)
    }
}

fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
    )
}
