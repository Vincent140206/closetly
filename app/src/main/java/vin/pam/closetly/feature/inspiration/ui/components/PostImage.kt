package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import vin.pam.closetly.feature.inspiration.model.InspirationPost
import vin.pam.closetly.feature.inspiration.model.OutfitStyle

/**
 * Placeholder outfit artwork. Swap the body for Coil's AsyncImage once posts carry image URLs.
 * Callers control the size (e.g. `Modifier.fillMaxWidth().aspectRatio(post.aspectRatio)`).
 */
@Composable
fun PostImage(post: InspirationPost, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawOutfit(post) }
}

private fun DrawScope.drawOutfit(p: InspirationPost) {
    val art = p.art ?: return
    val unit = size.width / 100f
    val h = size.height / unit
    val top = Color(art.topColor)
    val bottom = Color(art.bottomColor)
    val skin = Color(art.skinColor)

    withTransform({ scale(unit, unit, Offset.Zero) }) {
        drawRect(Color(art.backgroundColor), size = Size(100f, h))
        drawOval(Color(0x1A000000), Offset(16f, h - 9f), Size(68f, 10f))

        val y0 = h * 0.3f
        val legTop = y0 + h * 0.34f
        val legH = h * 0.34f
        val torsoH = h * 0.36f

        if (art.outfit == OutfitStyle.Dress) {
            val skirt = Path().apply {
                moveTo(40f, y0 + 8f); lineTo(60f, y0 + 8f)
                lineTo(72f, y0 + 8f + h * 0.5f); lineTo(28f, y0 + 8f + h * 0.5f); close()
            }
            drawPath(skirt, top)
            drawRect(skin, Offset(45f, h - 30f), Size(4f, 26f))
            drawRect(skin, Offset(52f, h - 30f), Size(4f, 26f))
        } else {
            val r = CornerRadius(2f)
            drawRoundRect(bottom, Offset(39f, legTop), Size(10f, legH), r)
            drawRoundRect(bottom, Offset(51f, legTop), Size(10f, legH), r)
            drawRoundRect(top, Offset(37f, y0 + 8f), Size(26f, torsoH), CornerRadius(5f))
            drawRoundRect(top, Offset(29f, y0 + 10f), Size(9f, h * 0.3f), CornerRadius(4f))
            drawRoundRect(top, Offset(62f, y0 + 10f), Size(9f, h * 0.3f), CornerRadius(4f))
            when (art.outfit) {
                OutfitStyle.Suit -> drawPath(
                    Path().apply { moveTo(45f, y0 + 8f); lineTo(50f, y0 + 24f); lineTo(55f, y0 + 8f); close() },
                    Color.White,
                )
                OutfitStyle.Coat -> drawPath(
                    Path().apply {
                        moveTo(36f, y0 + 8f); lineTo(64f, y0 + 8f)
                        lineTo(68f, y0 + 8f + h * 0.42f); lineTo(32f, y0 + 8f + h * 0.42f); close()
                    },
                    top,
                )
                else -> drawRect(Color.White.copy(alpha = 0.85f), Offset(44f, y0 + 8f), Size(12f, h * 0.1f))
            }
        }

        drawRect(skin, Offset(46f, y0 - 4f), Size(8f, 10f))
        drawCircle(skin, 9f, Offset(50f, y0 - 10f))
        drawArc(Color(0xFF3A2A22), 180f, 180f, true, Offset(41f, y0 - 19f), Size(18f, 18f))
        art.hatColor?.let { hat ->
            val c = Color(hat)
            drawOval(c, Offset(31f, y0 - 18.5f), Size(38f, 7f))
            drawArc(c, 180f, 180f, true, Offset(41f, y0 - 25f), Size(18f, 14f))
        }
    }
}
