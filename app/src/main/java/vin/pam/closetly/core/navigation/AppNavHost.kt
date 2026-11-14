package vin.pam.closetly.core.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
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
import vin.pam.closetly.feature.inspiration.InspirationViewModel
import vin.pam.closetly.feature.inspiration.ui.CommunityDestination
import vin.pam.closetly.feature.inspiration.ui.InspirationCollectionDestination
import vin.pam.closetly.feature.inspiration.ui.ManageCollectionsDestination
import vin.pam.closetly.feature.inspiration.ui.MyInspirationDestination
import vin.pam.closetly.feature.inspiration.ui.StyleDetailDestination
import vin.pam.closetly.feature.profile.ProfileScreen
import vin.pam.closetly.feature.profile.ProfileViewModel
import vin.pam.closetly.feature.outfit.OutfitDetailViewModel
import vin.pam.closetly.feature.outfit.OutfitFormViewModel
import vin.pam.closetly.feature.outfit.OutfitGraph
import vin.pam.closetly.feature.outfit.OutfitListViewModel
import vin.pam.closetly.feature.outfit.ui.OutfitDetailScreen
import vin.pam.closetly.feature.outfit.ui.OutfitFormScreen
import vin.pam.closetly.feature.outfit.ui.OutfitScreen
import vin.pam.closetly.feature.wardrobe.WardrobeGraph
import vin.pam.closetly.feature.wardrobe.WardrobeViewModel
import vin.pam.closetly.feature.wardrobe.WardrobeFormViewModel
import vin.pam.closetly.feature.wardrobe.ui.WardrobeScreen
import vin.pam.closetly.feature.wardrobe.ui.WardrobeDetailScreen
import vin.pam.closetly.feature.wardrobe.ui.WardrobeFormDestination

private const val HomeDestinationId = "home"
private const val WardrobeDestinationId = "wardrobe"
private const val OutfitDestinationId = "outfit"
private const val PlannerDestinationId = "planner"
private const val CommunityDestinationId = "community"

private val PrimaryBottomBarItems = listOf(
    ClosetlyBottomBarItem(HomeDestinationId, "Beranda", R.drawable.ic_closetly_home),
    ClosetlyBottomBarItem(WardrobeDestinationId, "Lemari", R.drawable.ic_closetly_wardrobe),
    ClosetlyBottomBarItem(OutfitDestinationId, "Outfit", R.drawable.ic_closetly_outfit),
    ClosetlyBottomBarItem(PlannerDestinationId, "Planner", R.drawable.ic_closetly_planner),
    ClosetlyBottomBarItem(CommunityDestinationId, "Inspirasi", R.drawable.ic_closetly_community),
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
    val applicationContext = LocalContext.current.applicationContext
    val wardrobeGraph = remember(authenticatedUser?.id, applicationContext) {
        WardrobeGraph.forAccount(applicationContext, authenticatedUser?.id)
    }
    val wardrobeViewModel: WardrobeViewModel = viewModel(
        key = "wardrobe-${authenticatedUser?.id ?: "development"}",
        factory = WardrobeViewModel.factory(wardrobeGraph.repository) { OutfitGraph.repository.getOutfits().size },
    )
    val wardrobeState by wardrobeViewModel.uiState.collectAsStateWithLifecycle()

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
            modifier = if (selectedPrimaryDestination == HomeDestinationId) {
                Modifier.padding(top = outerPadding.calculateTopPadding())
            } else {
                Modifier.padding(outerPadding)
            },
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
                HomeScreen(
                    displayName = profileUiState.profile?.displayName?.takeIf { it.isNotBlank() }
                        ?: authenticatedUser?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.home_default_name),
                    bottomContentPadding = outerPadding.calculateBottomPadding(),
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
                            AppModule.Wishlist -> navController.navigate(WishlistRoute)
                            AppModule.WearHistory -> navController.navigate(WearHistoryRoute)
                        }
                    },
                    onProfileClick = { navController.navigate(ProfileRoute) },
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
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { wardrobeViewModel.refresh() }
                WardrobeScreen(
                    state = wardrobeState,
                    onSearch = wardrobeViewModel::search,
                    onCategory = wardrobeViewModel::selectCategory,
                    onFilter = wardrobeViewModel::applyFilter,
                    onCreate = { navController.navigate(WardrobeFormRoute()) },
                    onOpenItem = { navController.navigate(WardrobeDetailRoute(it)) },
                    onEditItem = { navController.navigate(WardrobeFormRoute(it)) },
                    onFavorite = wardrobeViewModel::toggleFavorite,
                    onWishlist = { navController.navigate(WishlistRoute) },
                    onOutfits = { navController.navigateToPrimaryDestination(OutfitDestinationId) },
                    onProfile = { navController.navigate(ProfileRoute) },
                    onRetry = wardrobeViewModel::refresh,
                )
            }
            composable<WardrobeDetailRoute> { entry ->
                val id = entry.toRoute<WardrobeDetailRoute>().itemId
                LaunchedEffect(wardrobeState.archivedItemId) {
                    if (wardrobeState.archivedItemId == id) {
                        wardrobeViewModel.consumeArchive()
                        navController.popBackStack()
                    }
                }
                WardrobeDetailScreen(
                    item = wardrobeState.items.firstOrNull { it.id == id },
                    isLoading = wardrobeState.isLoading,
                    error = wardrobeState.errorMessage,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(WardrobeFormRoute(id)) },
                    onFavorite = wardrobeViewModel::toggleFavorite,
                    onArchive = { wardrobeViewModel.archive(id) },
                    onRestore = { wardrobeViewModel.restore(id) },
                )
            }
            composable<WardrobeFormRoute> { entry ->
                val id = entry.toRoute<WardrobeFormRoute>().itemId
                val formViewModel: WardrobeFormViewModel = viewModel(
                    key = "wardrobe-form-${authenticatedUser?.id ?: "development"}",
                    factory = WardrobeFormViewModel.factory(
                        wardrobeGraph.repository, wardrobeGraph.photoStore, wardrobeGraph.photoProcessor, id,
                    ),
                )
                WardrobeFormDestination(formViewModel, onBack = { navController.popBackStack() })
            }
            composable<OutfitRoute> {
                val outfitViewModel: OutfitListViewModel = viewModel(
                    factory = OutfitListViewModel.factory(OutfitGraph.repository),
                )
                val outfitUiState by outfitViewModel.uiState.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { outfitViewModel.refresh() }
                OutfitScreen(
                    uiState = outfitUiState,
                    onCreateOutfit = { navController.navigate(OutfitFormRoute()) },
                    onOutfitClick = { outfitId ->
                        navController.navigate(OutfitDetailRoute(outfitId = outfitId))
                    },
                    onRetry = outfitViewModel::refresh,
                )
            }
            composable<OutfitDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<OutfitDetailRoute>()
                val detailViewModel: OutfitDetailViewModel = viewModel(
                    factory = OutfitDetailViewModel.factory(OutfitGraph.repository, route.outfitId),
                )
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { detailViewModel.refresh() }
                OutfitDetailScreen(
                    uiState = detailUiState,
                    onBack = navController::popBackStack,
                    onEdit = { navController.navigate(OutfitFormRoute(outfitId = route.outfitId)) },
                    onDeleteClick = detailViewModel::onDeleteClick,
                    onDismissDelete = detailViewModel::onDismissDelete,
                    onConfirmDelete = detailViewModel::onConfirmDelete,
                    onDeleted = navController::popBackStack,
                    // TODO: sambungkan ke detail pakaian milik Digital Wardrobe setelah route-nya ada.
                    onItemClick = {},
                    onRetry = detailViewModel::refresh,
                )
            }
            composable<OutfitFormRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<OutfitFormRoute>()
                val formViewModel: OutfitFormViewModel = viewModel(
                    factory = OutfitFormViewModel.factory(OutfitGraph.repository, route.outfitId),
                )
                val formUiState by formViewModel.uiState.collectAsStateWithLifecycle()
                OutfitFormScreen(
                    uiState = formUiState,
                    onBack = navController::popBackStack,
                    onSaved = navController::popBackStack,
                    onNameChange = formViewModel::onNameChange,
                    onNameFocusLost = formViewModel::onNameFocusLost,
                    onDescriptionChange = formViewModel::onDescriptionChange,
                    onCategorySelected = formViewModel::onCategorySelected,
                    onToggleItem = formViewModel::onToggleItem,
                    onMoveItem = formViewModel::onMoveItem,
                    onRemoveItem = formViewModel::onRemoveItem,
                    onOpenWardrobe = {
                        navController.navigateToPrimaryDestination(WardrobeDestinationId)
                    },
                    onSave = formViewModel::onSave,
                )
            }

            composable<PlannerRoute> {
                ModulePlaceholderScreen(AppModule.Planner, navController::popBackStack)
            }

            //inspiration route
            composable<InspirationRoute> { entry ->
                val vm = viewModel<InspirationViewModel>(entry, factory = InspirationViewModel.Factory)
                CommunityDestination(
                    viewModel = vm,
                    onOpenPost = { navController.navigate(InspirationDetailRoute(it)) },
                    onOpenMyInspiration = { navController.navigate(MyInspirationRoute) },
                )
            }
            composable<InspirationDetailRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry<InspirationRoute>() }
                val vm = viewModel<InspirationViewModel>(parent, factory = InspirationViewModel.Factory)
                StyleDetailDestination(
                    viewModel = vm,
                    postId = entry.toRoute<InspirationDetailRoute>().postId,
                    onBack = { navController.popBackStack() },
                    onOpenPost = { navController.navigate(InspirationDetailRoute(it)) },
                )
            }
            composable<MyInspirationRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry<InspirationRoute>() }
                val vm = viewModel<InspirationViewModel>(parent, factory = InspirationViewModel.Factory)
                MyInspirationDestination(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenManage = { navController.navigate(ManageCollectionsRoute) },
                    onOpenCollection = { navController.navigate(InspirationCollectionRoute(it)) },
                    onOpenPost = { navController.navigate(InspirationDetailRoute(it)) },
                )
            }
            composable<InspirationCollectionRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry<InspirationRoute>() }
                val vm = viewModel<InspirationViewModel>(parent, factory = InspirationViewModel.Factory)
                InspirationCollectionDestination(
                    viewModel = vm,
                    collectionId = entry.toRoute<InspirationCollectionRoute>().collectionId,
                    onBack = { navController.popBackStack() },
                    onOpenManage = { navController.navigate(ManageCollectionsRoute) },
                    onOpenPost = { navController.navigate(InspirationDetailRoute(it)) },
                )
            }
            composable<ManageCollectionsRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry<InspirationRoute>() }
                val vm = viewModel<InspirationViewModel>(parent, factory = InspirationViewModel.Factory)
                ManageCollectionsDestination(viewModel = vm, onBack = { navController.popBackStack() })
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
