package vin.pam.closetly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import vin.pam.closetly.core.navigation.AppModule

class AppShellNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun waitForSplashToFinish() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Beranda").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun everyModule_opensSharedPlaceholder_andNavigatesBackHome() {
        AppModule.entries.forEach { module ->
            composeRule.onNodeWithText(module.title).performScrollTo().assertIsDisplayed().performClick()
            composeRule.onNodeWithText("Not implemented yet").assertIsDisplayed()
            composeRule.onNodeWithText("${module.title} is assigned to ${module.owner}.").assertIsDisplayed()
            composeRule.onNodeWithText("Back").performClick()
            composeRule.onNodeWithText(module.title).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun primaryBottomNavigation_updatesSelection_withoutDuplicatingDestinations() {
        composeRule.onNodeWithText("Beranda").assertIsSelected()

        composeRule.onNodeWithText("Lemari").performClick()
        composeRule.onNodeWithText("Digital Wardrobe").assertIsDisplayed()
        composeRule.onNodeWithText("Lemari").assertIsSelected()

        composeRule.onNodeWithText("Komunitas").performClick()
        composeRule.onNodeWithText("Community").assertIsDisplayed()
        composeRule.onNodeWithText("Komunitas").assertIsSelected()

        composeRule.onNodeWithText("Beranda").performClick()
        composeRule.onNodeWithText("Your wardrobe,\nyour story.").assertIsDisplayed()
        composeRule.onNodeWithText("Beranda").assertIsSelected()
    }
}
