package vin.pam.closetly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate
import java.util.UUID
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.outfit.OutfitGraph
import vin.pam.closetly.feature.planner.data.repository.LocalPlannerRepository
import vin.pam.closetly.feature.planner.model.PlannerOutfit
import vin.pam.closetly.feature.planner.ui.PlannerScreen
import vin.pam.closetly.ui.theme.ClosetlyTheme

/** Uses local plan storage and the same outfits as Mix & Match; no Supabase requests. */
class PlannerFlowTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun existingMixAndMatchOutfit_canBeSelectedInPlanner() {
        val existing = runBlocking { OutfitGraph.repository.getOutfits().first() }
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme(darkTheme = false) {
                AppNavHost(rememberNavController(), container)
            }
        }
        composeRule.onNodeWithText("Planner").performClick()
        waitForEnabled("+ Plan an Outfit")
        composeRule.onNodeWithText("+ Plan an Outfit").performClick()
        composeRule.onNodeWithText("+ Choose from My Outfits").performClick()
        composeRule.onNodeWithText(existing.name).assertIsDisplayed()
    }

    @Test
    fun plan_canBeCreatedReadRefreshedAndDeleted() {
        val existing = runBlocking { OutfitGraph.repository.getOutfits().first() }
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme(darkTheme = false) { AppNavHost(rememberNavController(), container) }
        }
        val name = "Planner flow ${System.currentTimeMillis()}"
        composeRule.onNodeWithText("Planner").performClick()
        waitForEnabled("+ Plan an Outfit")
        composeRule.onNodeWithText("+ Plan an Outfit").performClick()
        composeRule.onNodeWithText("+ Choose from My Outfits").performClick()
        composeRule.onNodeWithText(existing.name).performClick()
        composeRule.onNodeWithText("Use This Outfit").performClick()
        composeRule.onNode(hasSetTextAction() and hasText("What's the occasion?")).performTextInput(name)
        closeSoftKeyboard()
        composeRule.onNodeWithText("Save My Plan").performScrollTo().assertIsEnabled().performClick()
        waitForText("Outfit Planner")
        scrollHomeTo(name)
        composeRule.onAllNodes(hasText(name))[0].performClick()
        composeRule.onNodeWithText("Selected Outfit: ${existing.name}").assertIsDisplayed()
        composeRule.onNodeWithText("Delete Plan").performScrollTo().performClick()
        composeRule.onNodeWithText("Yes, Delete").performClick()
        waitForText("Outfit Planner")
        scrollHomeTo("Refresh")
        composeRule.onNodeWithText("Refresh").performClick()
        composeRule.onAllNodes(hasText(name)).assertCountEquals(0)
    }

    @Test
    fun failedDelete_keepsDetailVisible_andShowsError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val owner = "planner-test-${UUID.randomUUID()}"
        val folder = MessageDigest.getInstance("SHA-256").digest(owner.toByteArray()).joinToString("") { "%02x".format(it) }
        val directory = File(context.filesDir, "planner/$folder")
        val file = File(directory, "plans.json")
        val outfit = PlannerOutfit("test-outfit", "Audit look")
        try {
            runBlocking {
                LocalPlannerRepository(file, owner).add(UUID.randomUUID().toString(), outfit, "Storage failure test",
                    LocalDate.now().toString(), "Kuliah", "", "Direncanakan")
            }
            composeRule.setContent {
                ClosetlyTheme(darkTheme = false) {
                    PlannerScreen(accountId = owner, onHistoryClick = {}, savedOutfits = listOf(outfit))
                }
            }
            waitForText("Storage failure test")
            scrollHomeTo("Storage failure test")
            composeRule.onAllNodes(hasText("Storage failure test"))[0].performClick()
            file.writeText("broken snapshot")
            composeRule.onNodeWithText("Delete Plan").performScrollTo().performClick()
            composeRule.onNodeWithText("Yes, Delete").performClick()
            waitForText("Saved plans couldn't be read. Existing data has been kept.")
            composeRule.onNodeWithText("Saved plans couldn't be read. Existing data has been kept.").assertIsDisplayed()
            composeRule.onNodeWithText("Your Outfit Plan").performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText("Delete Plan").assertIsEnabled()
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun scrollHomeTo(text: String) {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(10_000) { composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitForEnabled(text: String) {
        composeRule.waitUntil(10_000) { composeRule.onAllNodes(hasText(text) and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
    }
}
