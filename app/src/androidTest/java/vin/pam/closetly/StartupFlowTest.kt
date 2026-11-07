package vin.pam.closetly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.ui.theme.ClosetlyTheme

class StartupFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splashPrecedesHome_andDoesNotReturnAfterStateRestoration() {
        composeRule.mainClock.autoAdvance = false
        val restorationTester = StateRestorationTester(composeRule)
        val container = AppContainer(AppConfig(null, null))
        restorationTester.setContent {
            ClosetlyTheme {
                ClosetlyApp(container)
            }
        }

        composeRule.onNodeWithContentDescription("Closetly").assertIsDisplayed()
        composeRule.onNodeWithText("Beranda").assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Beranda").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Closetly").assertDoesNotExist()
        composeRule.onNodeWithText("Beranda").assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithContentDescription("Closetly").assertDoesNotExist()
        composeRule.onNodeWithText("Beranda").assertIsDisplayed()
    }
}
