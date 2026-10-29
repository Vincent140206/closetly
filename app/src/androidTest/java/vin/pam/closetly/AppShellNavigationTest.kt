package vin.pam.closetly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.navigation.AppModule

class AppShellNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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
}
