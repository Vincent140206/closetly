package vin.pam.closetly

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.feature.wishlist.ui.WishlistDemoRoot
import vin.pam.closetly.ui.theme.ClosetlyTheme

class WishlistIntegrationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun filterUsesOneSheet_andAppliesCategory() {
        composeRule.setContent { ClosetlyTheme { WishlistDemoRoot() } }
        composeRule.onNodeWithText("Filter Wishlist").performClick()
        composeRule.onAllNodes(isDialog()).assertCountEquals(1)
        composeRule.onNodeWithText("Sepatu").performClick()
        composeRule.onNodeWithText("Terapkan filter").performClick()
        composeRule.onNodeWithText("Sneakers putih").assertIsDisplayed()
        composeRule.onNodeWithText("Jaket denim biru").assertDoesNotExist()
    }

    @Test fun createdWishlistEntry_survivesSavedStateRestoration() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent { ClosetlyTheme { WishlistDemoRoot() } }
        composeRule.onNodeWithText("Tambah barang").performClick()
        composeRule.onNodeWithText("Nama barang *").performScrollTo().performTextReplacement("Wishlist snapshot")
        composeRule.onNodeWithText("Pakaian").performScrollTo().performClick()
        composeRule.onNodeWithText("Simpan ke Wishlist").performScrollTo().performClick()
        composeRule.onNodeWithText("Wishlist snapshot").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Detail barang").assertIsDisplayed()
        composeRule.onNodeWithText("Wishlist snapshot").assertIsDisplayed()
    }
}
