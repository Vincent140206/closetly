package vin.pam.closetly.core.ui.splash

import android.graphics.Paint
import android.graphics.Rect as AndroidRect
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import kotlinx.coroutines.delay

@Composable
internal fun ClosetlyLogoAnimation(
    modifier: Modifier = Modifier,
    letterModifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
) {
    val roll = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }
    val iconReveal = remember { Animatable(0f) }
    val iconPainter = painterResource(R.drawable.ic_closetly_splash)
    val currentOnAnimationFinished by rememberUpdatedState(onAnimationFinished)

    LaunchedEffect(Unit) {
        roll.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        )
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        )
        iconReveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        )
        delay(300)
        currentOnAnimationFinished()
    }

    val displayStyle = MaterialTheme.typography.displaySmall
    val style = displayStyle.copy(
        fontSize = displayStyle.fontSize * 1.5f,
        lineHeight = displayStyle.lineHeight * 1.5f,
        letterSpacing = displayStyle.letterSpacing * 1.5f,
        color = MaterialTheme.colorScheme.onPrimary,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )
    val wordmark = "Closetly"
    val measurer = rememberTextMeasurer()
    val text = measurer.measure(
        text = wordmark,
        style = style,
        softWrap = false,
        maxLines = 1,
        overflow = TextOverflow.Visible,
    )
    val split = text.getHorizontalPosition(1, usePrimaryDirection = true)
    val density = LocalDensity.current
    val resolvedTypeface by LocalFontFamilyResolver.current.resolve(
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight ?: FontWeight.Normal,
        fontStyle = style.fontStyle ?: FontStyle.Normal,
        fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
    )

    val (letterBounds, wordCenterX) = remember(text, resolvedTypeface, density) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = resolvedTypeface as Typeface
            textSize = with(density) { style.fontSize.toPx() }
        }
        val bounds = AndroidRect()
        paint.getTextBounds("C", 0, 1, bounds)
        val baseline = text.getLineBaseline(0)
        val letterBounds = Rect(
            left = bounds.left.toFloat(),
            top = baseline + bounds.top,
            right = bounds.right.toFloat(),
            bottom = baseline + bounds.bottom,
        )

        paint.getTextBounds("y", 0, 1, bounds)
        val wordRight = text.getHorizontalPosition(wordmark.lastIndex, true) + bounds.right
        val wordCenterX = (letterBounds.left + wordRight) / 2f

        letterBounds to wordCenterX
    }
    val letterCenter = letterBounds.center
    val iconSize = letterBounds.height * 1.15f
    val iconSpace = iconSize + with(density) { 8.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = wordmark }
            .graphicsLayer {
                val fit = minOf(1f, size.width / (text.size.width + iconSpace))
                scaleX = fit
                scaleY = fit
                val rollShift = (letterCenter.x - wordCenterX) * roll.value
                val iconShift = iconSpace / 2f * iconReveal.value
                translationX = (rollShift + iconShift) * fit
            },
    ) {
        Canvas(
            modifier = letterModifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = -360f * roll.value },
        ) {
            val origin = center - letterCenter
            clipRect(left = origin.x, right = origin.x + split) {
                drawText(textLayoutResult = text, topLeft = origin)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val origin = center - letterCenter
            val progress = reveal.value
            if (progress > 0f) {
                val left = origin.x + split
                val right = left + (text.size.width - split) * progress
                clipRect(left = left, right = right) {
                    drawText(textLayoutResult = text, topLeft = origin)
                }
            }

            val iconAlpha = iconReveal.value
            if (iconAlpha > 0f) {
                translate(
                    left = origin.x + letterBounds.left - iconSpace,
                    top = center.y - iconSize / 2f,
                ) {
                    with(iconPainter) {
                        draw(size = Size(iconSize, iconSize), alpha = iconAlpha)
                    }
                }
            }
        }
    }
}
