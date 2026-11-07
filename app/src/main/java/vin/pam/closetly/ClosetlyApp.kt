package vin.pam.closetly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.core.navigation.HomeRoute
import vin.pam.closetly.core.navigation.OnboardingRoute
import vin.pam.closetly.core.ui.splash.ClosetlySplashScreen
import vin.pam.closetly.core.ui.splash.CircularRevealTransition
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.auth.AuthSessionState
import vin.pam.closetly.feature.auth.AuthViewModel

@Composable
fun ClosetlyApp(
    container: AppContainer,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(container.authRepository),
    ),
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var logoFinished by rememberSaveable { mutableStateOf(false) }
    var startAtHome by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(authUiState.sessionState) {
        if (startAtHome == null && authUiState.sessionState !is AuthSessionState.Initializing) {
            startAtHome = authUiState.sessionState is AuthSessionState.Authenticated
        }
    }

    CircularRevealTransition(
        reveal = logoFinished && startAtHome != null,
        splash = { ClosetlySplashScreen(onAnimationFinished = { logoFinished = true }) },
    ) {
        AppNavHost(
            navController = navController,
            container = container,
            authViewModel = authViewModel,
            startDestination = if (startAtHome == true) HomeRoute else OnboardingRoute,
        )
    }
}
