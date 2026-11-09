package vin.pam.closetly

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
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
    fun unfinishedModules_openSharedPlaceholder_andNavigateBackHome() {
        AppModule.entries.filterNot { it == AppModule.Outfit || it == AppModule.Inspiration }.forEach { module ->
            scrollTo(module.title)
            composeRule.onNodeWithText(module.title).assertIsDisplayed().performClick()
            composeRule.onNodeWithText("Not implemented yet").assertIsDisplayed()
            composeRule.onNodeWithText("${module.title} is assigned to ${module.owner}.").assertIsDisplayed()
            composeRule.onNodeWithText("Back").performClick()
            scrollTo(module.title)
            composeRule.onNodeWithText(module.title).assertIsDisplayed()
        }
    }

    @Test
    fun primaryBottomNavigation_updatesSelection_withoutDuplicatingDestinations() {
        composeRule.onNodeWithText("Beranda").assertIsSelected()

        composeRule.onNodeWithText("Lemari").performClick()
        composeRule.onNodeWithText("Digital Wardrobe").assertIsDisplayed()
        composeRule.onNodeWithText("Lemari").assertIsSelected()

        composeRule.onNodeWithText("Outfit").performClick()
        composeRule.onNodeWithText("Mix & Match Outfit").assertIsDisplayed()
        composeRule.onNodeWithText("Outfit").assertIsSelected()

        composeRule.onNodeWithText("Inspirasi").performClick()
        composeRule.onNodeWithText("Papan Inspirasi").assertIsDisplayed()
        composeRule.onNodeWithText("Inspirasi").assertIsSelected()

        composeRule.onNodeWithText("Beranda").performClick()
        composeRule.onNodeWithText("Your wardrobe,\nyour story.").assertIsDisplayed()
        composeRule.onNodeWithText("Beranda").assertIsSelected()
    }

    @Test
    fun outfit_canBeCreatedEditedAndDeletedFromAppShell() {
        val name = "Outfit uji integrasi"
        val editedName = "Outfit uji setelah edit"
        composeRule.onNodeWithText("Outfit").performClick()
        composeRule.onNodeWithText("Buat outfit").performClick()
        composeRule.onNodeWithText("Nama outfit").performTextReplacement(name)
        scrollTo("White Oversized Shirt")
        composeRule.onNodeWithText("White Oversized Shirt").performClick()
        scrollTo("Simpan outfit")
        composeRule.onNodeWithText("Simpan outfit").performClick()

        composeRule.onNodeWithText(name).performClick()
        composeRule.onNodeWithText("White Oversized Shirt").assertIsDisplayed()
        composeRule.onNodeWithText("Edit outfit").performClick()
        composeRule.onNodeWithText("Nama outfit").performTextReplacement(editedName)
        composeRule.onNodeWithText("Simpan outfit").performClick()

        composeRule.onNodeWithText(editedName).assertIsDisplayed()
        composeRule.onNodeWithText("Hapus outfit").performClick()
        composeRule.onNodeWithText("Hapus").performClick()
        composeRule.onNodeWithText("Mix & Match Outfit").assertIsDisplayed()
        composeRule.onNodeWithText(editedName).assertDoesNotExist()
    }

    @Test
    fun inspiration_canOpenSavedCollectionFromAppShell() {
        composeRule.onNodeWithText("Inspirasi").performClick()
        composeRule.onNodeWithText("Inspirasiku").performClick()
        composeRule.onNodeWithText("Koleksi").assertIsDisplayed()
        composeRule.onNodeWithText("OOTD Harian").performClick()
        composeRule.onNodeWithText("Inspirasiku").assertDoesNotExist()
        composeRule.onNodeWithText("OOTD Harian").assertIsDisplayed()
    }

    private fun scrollTo(text: String) {
        val verticalScroll = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        composeRule.onNode(hasScrollToIndexAction() and verticalScroll).performScrollToNode(hasText(text))
    }
}
