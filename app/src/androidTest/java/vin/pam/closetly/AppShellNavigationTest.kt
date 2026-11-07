package vin.pam.closetly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import vin.pam.closetly.core.navigation.AppModule
import androidx.navigation.compose.rememberNavController
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.ui.theme.ClosetlyTheme

class AppShellNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun showDevelopmentHome() {
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme {
                AppNavHost(navController = rememberNavController(), container = container)
            }
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
