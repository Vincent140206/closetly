package vin.pam.closetly.core.ui.splash

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import kotlinx.coroutines.delay

@Composable
internal fun ClosetlyLogoAnimation(
    modifier: Modifier = Modifier,
    letterModifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }

    // One sequence per presentation; ordinary recompositions do not restart it.
    LaunchedEffect(Unit) {
        rotation.animateTo(360f, tween(550, easing = FastOutSlowInEasing))
        reveal.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(300)
    }

    val style = MaterialTheme.typography.displaySmall.copy(
        color = MaterialTheme.colorScheme.onPrimary,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )
    val measurer = rememberTextMeasurer()
    val text = measurer.measure(text = "Closetly", style = style, softWrap = false)
    val split = text.getHorizontalPosition(1, usePrimaryDirection = true)
    val density = LocalDensity.current
    val typeface by LocalFontFamilyResolver.current.resolve(
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight ?: FontWeight.Normal,
        fontStyle = style.fontStyle ?: FontStyle.Normal,
        fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
    )
    // Ink bounds exclude font ascent/descent padding, keeping the rotation pivot
    // at the visible C's center. Compose still shapes and draws the actual text.
    val (letterCenter, wordCenter) = remember(text, typeface, density) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface as Typeface
            textSize = with(density) { style.fontSize.toPx() }
        }
        val bounds = Rect()
        paint.getTextBounds("C", 0, 1, bounds)
        val baseline = text.getLineBaseline(0)
        val letterCenter = Offset(bounds.exactCenterX(), baseline + bounds.exactCenterY())
        val wordLeft = bounds.left.toFloat()
        paint.getTextBounds("Closetly", 0, 7, bounds)
        val wordCenterY = baseline + bounds.exactCenterY()
        paint.getTextBounds("y", 0, 1, bounds)
        val wordRight = text.getHorizontalPosition(6, true) + bounds.right
        letterCenter to Offset((wordLeft + wordRight) / 2f, wordCenterY)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Closetly" }
            .graphicsLayer {
                // Fit the entire word at large font scales or on narrow windows.
                // Keep this scale constant throughout the sequence.
                val fit = minOf(1f, size.width / text.size.width)
                scaleX = fit
                scaleY = fit
                translationX = (letterCenter.x - wordCenter.x) * reveal.value * fit
                translationY = (letterCenter.y - wordCenter.y) * reveal.value * fit
            },
    ) {
        Canvas(
            modifier = letterModifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation.value },
        ) {
            val origin = center - letterCenter
            // The C is drawn once, from the same shaped text for every frame.
            clipRect(left = origin.x, right = origin.x + split) {
                drawText(textLayoutResult = text, topLeft = origin)
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val origin = center - letterCenter
            val left = origin.x + split
            clipRect(left = left, right = left + (text.size.width - split) * reveal.value) {
                drawText(textLayoutResult = text, topLeft = origin)
            }
        }
    }
}

