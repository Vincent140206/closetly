package vin.pam.closetly

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.ui.theme.ClosetlyTheme

class WardrobeFlowTest {
    @get:Rule val composeRule = createComposeRule()

    @Before
    fun showCollection() {
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme(darkTheme = false) {
                AppNavHost(navController = rememberNavController(), container = container)
            }
        }
        composeRule.onNodeWithText("Lemari").performClick()
        waitForText("Koleksi Lemari Saya")
    }

    @Test
    fun collection_createReadUpdateFavoriteArchiveAndRestore() {
        val name = "Kemeja uji ${System.currentTimeMillis()}"
        val edited = "$name edit"
        composeRule.onNodeWithText("+ Tambah Pakaian").performClick()
        field("Nama pakaian *").performTextInput(name)
        composeRule.onNodeWithText("Atasan").performScrollTo().performClick()
        field("Warna").performScrollTo().performTextInput("Biru")
        closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan ke Koleksi").performScrollTo().performClick()
        waitForText("Koleksi Lemari Saya")
        findClothing(name)
        composeRule.onNodeWithText(name).performClick()
        waitForText("Detail pakaian")
        composeRule.onNodeWithText("Jadikan favorit").performScrollTo().performClick()
        waitForText("Hapus dari favorit")
        composeRule.onNodeWithText("Edit pakaian").performScrollTo().performClick()
        field("Nama pakaian *").performTextReplacement(edited)
        closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan perubahan").performScrollTo().performClick()
        waitForText(edited)
        composeRule.onNodeWithText("Hapus pakaian").performScrollTo().performClick()
        composeRule.onNodeWithText("Hapus").performClick()
        waitForText("Koleksi Lemari Saya")

        // Opening the archive and restoring proves that deletion keeps outfit references intact.
        collectionScroll("Cari nama pakaian")
        composeRule.onNode(hasSetTextAction()).performTextReplacement(edited)
        closeSoftKeyboard()
        composeRule.onNodeWithContentDescription("Filter koleksi").performClick()
        composeRule.onNodeWithText("Tampilkan arsip").performClick()
        composeRule.onNodeWithText("Terapkan filter").performClick()
        findClothing(edited)
        composeRule.onNode(hasText(edited) and !hasSetTextAction()).performClick()
        composeRule.onNodeWithText("Pulihkan ke koleksi").performScrollTo().performClick()
        waitForText("Edit pakaian")
        composeRule.onNodeWithText("Hapus pakaian").performScrollTo().performClick()
        composeRule.onNodeWithText("Hapus").performClick()
        waitForText("Koleksi Lemari Saya")
    }

    @Test
    fun collectionScreenshot_andSearchEmptyState() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("4 pakaian").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithContentDescription("Jaket denim biru").fetchSemanticsNodes().isNotEmpty()
        }
        capture("wardrobe-collection.png")
        composeRule.onNodeWithText("+ Tambah Pakaian").performClick()
        capture("wardrobe-form.png")
        composeRule.onNodeWithContentDescription("Kembali ke koleksi").performClick()
        collectionScroll("Cari nama pakaian")
        composeRule.onNode(hasSetTextAction()).performTextReplacement("pakaian-yang-tidak-ada")
        closeSoftKeyboard()
        collectionScroll("Tidak ada hasil")
        composeRule.onNodeWithText("Tidak ada hasil").assertIsDisplayed()
    }

    private fun field(label: String) = composeRule.onNode(hasSetTextAction() and hasText(label))

    private fun findClothing(name: String) {
        val clothing = hasText(name) and !hasSetTextAction()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(clothing).fetchSemanticsNodes().isNotEmpty()
        }
        val vertical = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        composeRule.onNode(hasScrollToIndexAction() and vertical).performScrollToNode(clothing)
    }

    private fun collectionScroll(text: String) {
        val vertical = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        composeRule.onNode(hasScrollToIndexAction() and vertical).performScrollToNode(hasText(text))
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(context.cacheDir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
