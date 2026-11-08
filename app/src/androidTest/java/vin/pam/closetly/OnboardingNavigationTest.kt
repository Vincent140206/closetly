package vin.pam.closetly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.core.navigation.LoginRoute
import vin.pam.closetly.core.navigation.OnboardingRoute
import vin.pam.closetly.core.navigation.RegisterRoute
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.onboarding.OnboardingScreen
import vin.pam.closetly.ui.theme.ClosetlyTheme

class OnboardingNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()
    private lateinit var navController: NavHostController

    @Test
    fun getStarted_opensRegister_andBackReturnsToOnboarding() {
        showOnboardingGraph()
        composeRule.onNodeWithText("Mulai Sekarang").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(
                RegisterRoute.serializer().descriptor.serialName,
                navController.currentDestination?.route,
            )
            navController.popBackStack()
        }
        composeRule.onNodeWithTag("onboarding_screen").assertIsDisplayed()
    }

    @Test
    fun login_opensLogin_andBackReturnsToOnboarding() {
        showOnboardingGraph()
        composeRule.onNodeWithText("Sudah punya akun? Masuk").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(
                LoginRoute.serializer().descriptor.serialName,
                navController.currentDestination?.route,
            )
            navController.popBackStack()
        }
        composeRule.onNodeWithTag("onboarding_screen").assertIsDisplayed()
    }

    @Test
    fun smallWindowWithLargeFont_keepsBothActionsReachable() {
        var getStartedClicks = 0
        var loginClicks = 0
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ClosetlyTheme {
                    Box(Modifier.width(280.dp).height(480.dp)) {
                        OnboardingScreen(
                            onGetStarted = { getStartedClicks++ },
                            onLogin = { loginClicks++ },
                        )
                    }
                }
            }
        }
        composeRule.onNodeWithText("Mulai Sekarang")
            .performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Sudah punya akun? Masuk")
            .performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            assertEquals(1, getStartedClicks)
            assertEquals(1, loginClicks)
        }
    }

    private fun showOnboardingGraph() {
        val container = AppContainer(AppConfig(null, null))
        composeRule.setContent {
            navController = rememberNavController()
            ClosetlyTheme {
                AppNavHost(
                    navController = navController,
                    container = container,
                    startDestination = OnboardingRoute,
                )
            }
        }
    }
}
