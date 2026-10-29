package vin.pam.closetly.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import vin.pam.closetly.core.ui.shell.HomeScreen
import vin.pam.closetly.core.ui.shell.ModulePlaceholderScreen
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.auth.AuthSessionState
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.feature.auth.LoginScreen
import vin.pam.closetly.feature.auth.RegisterScreen
import vin.pam.closetly.feature.profile.ProfileScreen
import vin.pam.closetly.feature.profile.ProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(container.authRepository),
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(container.profileRepository),
    )
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val authenticatedUser =
        (authUiState.sessionState as? AuthSessionState.Authenticated)?.user

    LaunchedEffect(authenticatedUser?.id) {
        profileViewModel.onSessionChanged(authenticatedUser?.id)
    }

    LaunchedEffect(authUiState.sessionState, currentRoute) {
        when (authUiState.sessionState) {
            is AuthSessionState.Authenticated -> {
                val isAuthenticationScreen =
                    currentRoute == LoginRoute.serializer().descriptor.serialName ||
                        currentRoute == RegisterRoute.serializer().descriptor.serialName
                if (isAuthenticationScreen) {
                    navController.navigate(HomeRoute) {
                        popUpTo<HomeRoute> { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
            AuthSessionState.NotAuthenticated -> {
                if (currentRoute == ProfileRoute.serializer().descriptor.serialName) {
                    navController.navigate(LoginRoute) {
                        popUpTo<ProfileRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AuthSessionState.Initializing,
            is AuthSessionState.Failure,
            is AuthSessionState.Unavailable,
            -> Unit
        }
    }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            val statusMessage = when (val state = authUiState.sessionState) {
                AuthSessionState.Initializing -> "Restoring saved session..."
                is AuthSessionState.Authenticated ->
                    "Signed in as ${state.user.email.ifBlank { "authenticated user" }}"
                AuthSessionState.NotAuthenticated ->
                    "Development Home is available without login. Supabase profile data is not."
                is AuthSessionState.Failure -> state.message
                is AuthSessionState.Unavailable -> state.message
            }
            val canOpenAuthentication =
                authUiState.sessionState !is AuthSessionState.Initializing

            HomeScreen(
                authStatus = statusMessage,
                authActionLabel = if (authenticatedUser == null) "Login or register" else "Open profile",
                isAuthActionEnabled = canOpenAuthentication,
                onModuleClick = { module ->
                    when (module) {
                        AppModule.Wardrobe -> navController.navigate(WardrobeRoute)
                        AppModule.Outfit -> navController.navigate(OutfitRoute)
                        AppModule.Planner -> navController.navigate(PlannerRoute)
                        AppModule.Inspiration -> navController.navigate(InspirationRoute)
                        AppModule.Wishlist -> navController.navigate(WishlistRoute)
                        AppModule.WearHistory -> navController.navigate(WearHistoryRoute)
                    }
                },
                onProfileClick = { navController.navigate(ProfileRoute) },
                onAuthClick = {
                    if (authenticatedUser == null) {
                        authViewModel.clearMessages()
                        navController.navigate(LoginRoute)
                    } else {
                        navController.navigate(ProfileRoute)
                    }
                },
            )
        }
        composable<LoginRoute> {
            LoginScreen(
                uiState = authUiState,
                onLogin = authViewModel::login,
                onRegister = {
                    authViewModel.clearMessages()
                    navController.navigate(RegisterRoute) { launchSingleTop = true }
                },
                onBack = navController::popBackStack,
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                uiState = authUiState,
                onRegister = authViewModel::register,
                onLogin = {
                    authViewModel.clearMessages()
                    if (!navController.popBackStack<LoginRoute>(inclusive = false)) {
                        navController.navigate(LoginRoute) { launchSingleTop = true }
                    }
                },
                onBack = navController::popBackStack,
            )
        }
        composable<ProfileRoute> {
            ProfileScreen(
                authState = authUiState.sessionState,
                uiState = profileUiState,
                onUpdateDisplayName = profileViewModel::updateDisplayName,
                onRefresh = profileViewModel::refresh,
                onLogout = authViewModel::logout,
                onLogin = { navController.navigate(LoginRoute) { launchSingleTop = true } },
                onBack = navController::popBackStack,
            )
        }
        composable<WardrobeRoute> {
            ModulePlaceholderScreen(AppModule.Wardrobe, navController::popBackStack)
        }
        composable<OutfitRoute> {
            ModulePlaceholderScreen(AppModule.Outfit, navController::popBackStack)
        }
        composable<PlannerRoute> {
            ModulePlaceholderScreen(AppModule.Planner, navController::popBackStack)
        }
        composable<InspirationRoute> {
            ModulePlaceholderScreen(AppModule.Inspiration, navController::popBackStack)
        }
        composable<WishlistRoute> {
            ModulePlaceholderScreen(AppModule.Wishlist, navController::popBackStack)
        }
        composable<WearHistoryRoute> {
            ModulePlaceholderScreen(AppModule.WearHistory, navController::popBackStack)
        }
    }
}
