package vin.pam.closetly

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.navigation.AppModule
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.core.ui.shell.HomeScreen
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.ui.theme.ClosetlyTheme

class HomeDashboardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboard_showsCurrentDate_andKeepsLowerShortcutsAccessible() {
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme(darkTheme = false) {
                AppNavHost(navController = rememberNavController(), container = container)
            }
        }

        composeRule.onNodeWithText("Halo, Teman Closetly").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Buka profil").assertIsDisplayed()
        val today = LocalDate.now()
        val locale = Locale.forLanguageTag("id-ID")
        composeRule.onNodeWithText(today.format(DateTimeFormatter.ofPattern("dd", locale)))
            .assertIsDisplayed()
        composeRule.onNodeWithText(today.format(DateTimeFormatter.ofPattern("EEEE", locale)))
            .assertIsDisplayed()
        capture("home-dashboard.png")

        scrollTo("Buka lemari")
        composeRule.onNodeWithText("Buka lemari").assertIsDisplayed()
        capture("home-dashboard-lower.png")
        scrollTo("Wear History")
        composeRule.onNodeWithText("Wear History").assertIsDisplayed()
        composeRule.onNodeWithText("Fashion Wishlist").assertIsDisplayed()
    }

    @Test
    fun compactDashboard_withLargeText_keepsActionsUsable() {
        var selectedModule: AppModule? = null
        composeRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1.5f)) {
                ClosetlyTheme(darkTheme = false) {
                    Box(Modifier.width(320.dp).fillMaxHeight()) {
                        HomeScreen(
                            displayName = "Vincent",
                            onModuleClick = { selectedModule = it },
                            onProfileClick = {},
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Halo, Vincent").assertIsDisplayed()
        assertSingleLine("Coba sekarang")
        composeRule.onNodeWithText("Coba sekarang").performClick()
        assertEquals(AppModule.Outfit, selectedModule)
        capture("home-dashboard-large-text.png")
        scrollTo("Buat Rencana")
        assertSingleLine("Kalender outfit")
        composeRule.onNodeWithText("Buat Rencana").performClick()
        assertEquals(AppModule.Planner, selectedModule)
        scrollTo("Wear History")
        composeRule.onNodeWithText("Wear History").performClick()
        assertEquals(AppModule.WearHistory, selectedModule)
    }

    private fun scrollTo(text: String) {
        val verticalScroll = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        composeRule.onNode(hasScrollToIndexAction() and verticalScroll)
            .performScrollToNode(hasText(text))
    }

    private fun capture(name: String) {
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir, name).outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    private fun assertSingleLine(text: String) {
        val layouts = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(text, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(layouts) }
        assertEquals("$text should fit on one line", 1, layouts.single().lineCount)
    }
}
