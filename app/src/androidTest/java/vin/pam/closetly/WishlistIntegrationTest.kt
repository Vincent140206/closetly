package vin.pam.closetly

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import vin.pam.closetly.feature.wishlist.ui.WishlistDemoRoot
import vin.pam.closetly.ui.theme.ClosetlyTheme

class WishlistIntegrationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun filterUsesOneSheet_andAppliesCategory() {
        composeRule.setContent { ClosetlyTheme { WishlistDemoRoot() } }
        composeRule.onNodeWithContentDescription("Buka filter Wishlist").performClick()
        composeRule.onAllNodes(isDialog()).assertCountEquals(1)
        composeRule.onNodeWithText("Sepatu").performClick()
        composeRule.onNodeWithText("Terapkan filter").performClick()
        composeRule.onNodeWithText("Sneakers putih").assertIsDisplayed()
        composeRule.onNodeWithText("Jaket denim biru").assertDoesNotExist()
    }

    @Test fun createdWishlistEntry_survivesSavedStateRestoration() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent { ClosetlyTheme { WishlistDemoRoot() } }
        createEntry("Wishlist snapshot")
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Detail barang").assertIsDisplayed()
        composeRule.onNodeWithText("Wishlist snapshot").assertIsDisplayed()
    }

    @Test fun purchasedStatus_survivesSavedStateRestoration() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent { ClosetlyTheme { WishlistDemoRoot() } }
        createEntry("Wishlist purchased")
        composeRule.onNodeWithText("Tandai sudah dibeli").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Tandai belum dibeli").performScrollTo().assertIsDisplayed()
    }

    @Test fun homeShortcutRequest_isConsumedWithoutOverridingLaterCollectionSelection() {
        var request by mutableStateOf(true)
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            ClosetlyTheme {
                WishlistDemoRoot(
                    collectionContent = { Text("Koleksi uji") },
                    openWishlist = request,
                    onWishlistOpened = { request = false },
                )
            }
        }
        composeRule.onNodeWithText("Wishlist Saya").assertIsDisplayed()
        composeRule.onNodeWithText("Koleksi").performClick()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Koleksi uji").assertIsDisplayed()
        composeRule.onNodeWithText("Wishlist Saya").assertDoesNotExist()
    }

    private fun createEntry(name: String) {
        composeRule.onNodeWithText("Tambah barang").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement(name)
        composeRule.onNodeWithText("Pilih kategori").performScrollTo().performClick()
        composeRule.onNodeWithText("Pakaian").performClick()
        composeRule.onNodeWithText("Simpan ke Wishlist").performScrollTo().performClick()
    }
}
