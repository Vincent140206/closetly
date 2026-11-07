package vin.pam.closetly.core.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import vin.pam.closetly.ui.theme.ClosetlyTheme
import kotlin.math.hypot

@Composable
fun CircularRevealTransition(
    reveal: Boolean,
    modifier: Modifier = Modifier,
    splash: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (reveal) 1f else 0f) }

    LaunchedEffect(reveal) {
        if (reveal && progress.value < 1f) {
            progress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }
    }

    val transitioning = progress.value < 1f
    Box(modifier.fillMaxSize()) {
        if (transitioning) {
            Box(Modifier.fillMaxSize()) { splash() }
        }
        if (reveal) {
            val interactionGuard = if (transitioning) {
                Modifier
                    .clearAndSetSemantics { }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial).changes.forEach {
                                    it.consume()
                                }
                            }
                        }
                    }
            } else {
                Modifier
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(interactionGuard)
                    .graphicsLayer {
                        shape = CircularRevealShape(progress.value)
                        clip = progress.value < 1f
                    },
            ) {
                content()
            }
        }
    }
}

private class CircularRevealShape(private val progress: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val center = size.center
        val radius = hypot(size.width, size.height) / 2f * progress
        return Outline.Generic(
            Path().apply {
                addOval(
                    Rect(
                        left = center.x - radius,
                        top = center.y - radius,
                        right = center.x + radius,
                        bottom = center.y + radius,
                    ),
                )
            },
        )
    }
}

@Preview(name = "Splash to circular reveal", widthDp = 393, heightDp = 852)
@Preview(name = "Circular reveal landscape", widthDp = 852, heightDp = 393)
@Composable
private fun CircularRevealTransitionPreview() {
    ClosetlyTheme(darkTheme = false) {
        var replay by remember { mutableIntStateOf(0) }
        Box(Modifier.fillMaxSize()) {
            key(replay) {
                var reveal by remember { mutableStateOf(false) }
                CircularRevealTransition(
                    reveal = reveal,
                    splash = { ClosetlySplashScreen(onAnimationFinished = { reveal = true }) },
                ) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Beranda", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
            TextButton(
                onClick = { replay++ },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Text("Replay", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

