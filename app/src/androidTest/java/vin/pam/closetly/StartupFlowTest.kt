package vin.pam.closetly

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.auth.AuthenticatedUser
import vin.pam.closetly.feature.auth.AuthRepository
import vin.pam.closetly.feature.auth.AuthSessionState
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.feature.auth.RegistrationResult
import vin.pam.closetly.ui.theme.ClosetlyTheme

class StartupFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splashPrecedesOnboarding_andDoesNotReturnAfterStateRestoration() {
        composeRule.mainClock.autoAdvance = false
        val restorationTester = StateRestorationTester(composeRule)
        val container = AppContainer(AppConfig(null, null))
        val repository = FakeAuthRepository(AuthSessionState.NotAuthenticated)
        restorationTester.setContent {
            ClosetlyTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.factory(repository),
                )
                ClosetlyApp(container = container, authViewModel = authViewModel)
            }
        }

        composeRule.onNodeWithContentDescription("Closetly").assertIsDisplayed()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("onboarding_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Closetly").assertDoesNotExist()
        composeRule.onNodeWithTag("onboarding_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()
        composeRule.onNodeWithText("Beranda").assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithContentDescription("Closetly").assertDoesNotExist()
        composeRule.onNodeWithTag("onboarding_screen").assertIsDisplayed()
    }

    @Test
    fun restoredSession_revealsHomeWithoutShowingLogin() {
        composeRule.mainClock.autoAdvance = false
        showApp(FakeAuthRepository(authenticatedSession))

        composeRule.onNodeWithContentDescription("Closetly").assertIsDisplayed()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.mainClock.autoAdvance = true
        waitForHome()
        composeRule.onNodeWithTag("onboarding_screen").assertDoesNotExist()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Closetly").assertDoesNotExist()
    }

    @Test
    fun pendingSession_keepsSplashUntilDestinationIsKnown() {
        composeRule.mainClock.autoAdvance = false
        val repository = FakeAuthRepository(AuthSessionState.Initializing)
        showApp(repository)

        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.onNodeWithContentDescription("Closetly").assertIsDisplayed()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()
        composeRule.onNodeWithText("Beranda").assertDoesNotExist()

        composeRule.runOnIdle { repository.sessionState.value = authenticatedSession }
        composeRule.mainClock.autoAdvance = true
        waitForHome()
        composeRule.onNodeWithText("Selamat Datang\nKembali").assertDoesNotExist()
    }

    @Test
    fun recoveryLink_savesNewPasswordWithoutSigningIn() {
        val repository = FakeAuthRepository(AuthSessionState.NotAuthenticated)
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.factory(repository),
                )
                LaunchedEffect(authViewModel) {
                    authViewModel.openPasswordRecovery("test-recovery-code")
                }
                ClosetlyApp(container = container, authViewModel = authViewModel)
            }
        }

        composeRule.onNodeWithText("Buat password baru").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Password baru")
            .performScrollTo().performTextInput("newpass123")
        composeRule.onNodeWithContentDescription("Konfirmasi password baru")
            .performScrollTo().performTextInput("newpass123")
        composeRule.onNodeWithText("Simpan password").performClick()

        composeRule.onNodeWithText("Password berhasil diperbarui").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("newpass123", repository.recoveredPassword)
            assertEquals(AuthSessionState.NotAuthenticated, repository.sessionState.value)
        }
    }

    private fun showApp(repository: AuthRepository) {
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            ClosetlyTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.factory(repository),
                )
                ClosetlyApp(container = container, authViewModel = authViewModel)
            }
        }
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Beranda").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Beranda").assertIsDisplayed()
    }

    private val authenticatedSession = AuthSessionState.Authenticated(
        AuthenticatedUser(id = "startup-test-user", email = "startup@example.com"),
    )
}

private class FakeAuthRepository(initialState: AuthSessionState) : AuthRepository {
    override val sessionState = MutableStateFlow(initialState)
    var recoveredPassword: String? = null
        private set
    override suspend fun restoreSession() = Unit
    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        username: String,
    ): RegistrationResult = error("Registration is outside the startup tests")

    override suspend fun signIn(email: String, password: String) {
        sessionState.value = AuthSessionState.Authenticated(AuthenticatedUser("test-user", email))
    }

    override suspend fun requestPasswordReset(email: String) = Unit

    override suspend fun openPasswordRecovery(code: String): String {
        check(code == "test-recovery-code")
        return "recovery@example.com"
    }

    override suspend fun updateRecoveredPassword(password: String) {
        recoveredPassword = password
    }

    override fun clearPasswordRecovery() = Unit

    override suspend fun signOut() {
        sessionState.value = AuthSessionState.NotAuthenticated
    }

    override fun currentUserId(): String? =
        (sessionState.value as? AuthSessionState.Authenticated)?.user?.id

    override fun currentUserEmail(): String? =
        (sessionState.value as? AuthSessionState.Authenticated)?.user?.email
}
