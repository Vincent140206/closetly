package vin.pam.closetly.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyBottomBar
import vin.pam.closetly.core.ui.components.ClosetlyBottomBarItem
import vin.pam.closetly.feature.onboarding.OnboardingScreen
import vin.pam.closetly.core.ui.shell.HomeScreen
import vin.pam.closetly.core.ui.shell.ModulePlaceholderScreen
import vin.pam.closetly.di.AppContainer
import vin.pam.closetly.feature.auth.AuthSessionState
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.feature.auth.LoginScreen
import vin.pam.closetly.feature.auth.RegisterScreen
import vin.pam.closetly.feature.profile.ProfileScreen
import vin.pam.closetly.feature.profile.ProfileViewModel
import vin.pam.closetly.feature.wishlist.ui.WishlistDemoRoot

private const val HomeDestinationId = "home"
private const val WardrobeDestinationId = "wardrobe"
private const val OutfitDestinationId = "outfit"
private const val PlannerDestinationId = "planner"
private const val CommunityDestinationId = "community"

private val PrimaryBottomBarItems = listOf(
    ClosetlyBottomBarItem(HomeDestinationId, "Beranda", R.drawable.ic_closetly_home),
    ClosetlyBottomBarItem(WardrobeDestinationId, "Closet", R.drawable.ic_closetly_wardrobe),
    ClosetlyBottomBarItem(OutfitDestinationId, "Outfit", R.drawable.ic_closetly_outfit),
    ClosetlyBottomBarItem(PlannerDestinationId, "Planner", R.drawable.ic_closetly_planner),
    ClosetlyBottomBarItem(CommunityDestinationId, "Komunitas", R.drawable.ic_closetly_community),
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(container.authRepository),
    ),
    startDestination: Any = HomeRoute,
) {
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(container.profileRepository),
    )
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedPrimaryDestination = primaryDestinationId(currentRoute)
    val authenticatedUser =
        (authUiState.sessionState as? AuthSessionState.Authenticated)?.user

    LaunchedEffect(authenticatedUser?.id) {
        profileViewModel.onSessionChanged(authenticatedUser?.id)
    }

    LaunchedEffect(authUiState.sessionState, currentRoute) {
        when (authUiState.sessionState) {
            is AuthSessionState.Authenticated -> {
                val isEntryScreen =
                    currentRoute == OnboardingRoute.serializer().descriptor.serialName ||
                        currentRoute == LoginRoute.serializer().descriptor.serialName ||
                        currentRoute == RegisterRoute.serializer().descriptor.serialName
                if (isEntryScreen) {
                    navController.navigate(HomeRoute) {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
            AuthSessionState.NotAuthenticated -> {
                val isEntryScreen =
                    currentRoute == OnboardingRoute.serializer().descriptor.serialName ||
                        currentRoute == LoginRoute.serializer().descriptor.serialName ||
                        currentRoute == RegisterRoute.serializer().descriptor.serialName
                if (currentRoute != null && !isEntryScreen) {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(navController.graph.id) { inclusive = false }
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedPrimaryDestination != null) {
                ClosetlyBottomBar(
                    items = PrimaryBottomBarItems,
                    selectedItemId = selectedPrimaryDestination,
                    onItemSelected = { item ->
                        navController.navigateToPrimaryDestination(item.id)
                    },
                )
            }
        },
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(outerPadding),
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    modifier = Modifier.consumeWindowInsets(outerPadding),
                    onGetStarted = {
                        authViewModel.clearMessages()
                        navController.navigate(RegisterRoute) { launchSingleTop = true }
                    },
                    onLogin = {
                        authViewModel.clearMessages()
                        navController.navigate(LoginRoute) { launchSingleTop = true }
                    },
                )
            }
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
                            AppModule.Wardrobe -> navController.navigateToPrimaryDestination(
                                WardrobeDestinationId,
                            )
                            AppModule.Outfit -> navController.navigateToPrimaryDestination(
                                OutfitDestinationId,
                            )
                            AppModule.Planner -> navController.navigateToPrimaryDestination(
                                PlannerDestinationId,
                            )
                            AppModule.Inspiration -> navController.navigateToPrimaryDestination(
                                CommunityDestinationId,
                            )
                            AppModule.Wishlist -> navController.navigateToPrimaryDestination(
                                WardrobeDestinationId,
                            )
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
                    modifier = Modifier.consumeWindowInsets(outerPadding),
                    uiState = authUiState,
                    onLogin = authViewModel::login,
                    onRequestPasswordReset = authViewModel::requestPasswordReset,
                    onClearMessages = authViewModel::clearMessages,
                    onRegister = {
                        authViewModel.clearMessages()
                        navController.navigate(RegisterRoute) { launchSingleTop = true }
                    },
                    onBack = navController::popBackStack,
                )
            }
            composable<RegisterRoute> {
                RegisterScreen(
                    modifier = Modifier.consumeWindowInsets(outerPadding),
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
                WishlistDemoRoot(
                    modifier = Modifier.consumeWindowInsets(outerPadding),
                    collectionContent = {
                        ModulePlaceholderScreen(
                            AppModule.Wardrobe,
                            navController::popBackStack,
                        )
                    },
                )
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
}

private fun primaryDestinationId(route: String?): String? = when (route) {
    HomeRoute.serializer().descriptor.serialName -> HomeDestinationId
    WardrobeRoute.serializer().descriptor.serialName -> WardrobeDestinationId
    OutfitRoute.serializer().descriptor.serialName -> OutfitDestinationId
    PlannerRoute.serializer().descriptor.serialName -> PlannerDestinationId
    InspirationRoute.serializer().descriptor.serialName -> CommunityDestinationId
    else -> null
}

private fun NavHostController.navigateToPrimaryDestination(destinationId: String) {
    when (destinationId) {
        HomeDestinationId -> navigate(HomeRoute) { primaryDestinationOptions() }
        WardrobeDestinationId -> navigate(WardrobeRoute) { primaryDestinationOptions() }
        OutfitDestinationId -> navigate(OutfitRoute) { primaryDestinationOptions() }
        PlannerDestinationId -> navigate(PlannerRoute) { primaryDestinationOptions() }
        CommunityDestinationId -> navigate(InspirationRoute) { primaryDestinationOptions() }
    }
}

private fun NavOptionsBuilder.primaryDestinationOptions() {
    popUpTo<HomeRoute> { saveState = true }
    launchSingleTop = true
    restoreState = true
}
