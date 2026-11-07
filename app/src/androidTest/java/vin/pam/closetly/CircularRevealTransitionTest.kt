package vin.pam.closetly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.ui.splash.CircularRevealTransition
import vin.pam.closetly.ui.theme.GoldenPrimary
import vin.pam.closetly.ui.theme.OffWhite

class CircularRevealTransitionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun revealsCenterBeforeCorners_andPreservesDestinationUntilComplete() {
        composeRule.mainClock.autoAdvance = false
        val reveal = mutableStateOf(false)
        var mounts = 0
        var disposals = 0
        var taps = 0
        composeRule.setContent {
            CircularRevealTransition(
                reveal = reveal.value,
                modifier = Modifier.testTag("transition"),
                splash = { Box(Modifier.fillMaxSize().background(GoldenPrimary)) },
            ) {
                DisposableEffect(Unit) {
                    mounts++
                    onDispose { disposals++ }
                }
                Box(Modifier.fillMaxSize().background(OffWhite)) {
                    Button(onClick = { taps++ }) { Text("Destination") }
                }
            }
        }
        composeRule.runOnIdle { assertEquals(0, mounts) }

        composeRule.runOnIdle { reveal.value = true }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.onNodeWithText("Destination").assertDoesNotExist()
        val partial = composeRule.onNodeWithTag("transition").captureToImage().toPixelMap()
        assertEquals(OffWhite.toArgb(), partial[partial.width / 2, partial.height / 2].toArgb())
        assertEquals(GoldenPrimary.toArgb(), partial[partial.width - 1, partial.height - 1].toArgb())
        composeRule.onNodeWithTag("transition").performTouchInput { click(Offset(40f, 40f)) }
        composeRule.runOnIdle { assertEquals(0, taps) }

        composeRule.mainClock.advanceTimeBy(1_000)
        val completed = composeRule.onNodeWithTag("transition").captureToImage().toPixelMap()
        assertEquals(OffWhite.toArgb(), completed[completed.width - 1, completed.height - 1].toArgb())
        composeRule.onNodeWithText("Destination").assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            assertEquals(1, taps)
            assertEquals(1, mounts)
            assertEquals(0, disposals)
        }
    }

    @Test
    fun alreadyRevealedPresentation_showsDestinationImmediately() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            CircularRevealTransition(reveal = true, splash = { Text("Splash") }) {
                Text("Destination")
            }
        }
        composeRule.onNodeWithText("Splash").assertDoesNotExist()
        composeRule.onNodeWithText("Destination").assertIsDisplayed()
    }
}
