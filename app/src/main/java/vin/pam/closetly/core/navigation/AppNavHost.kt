package vin.pam.closetly.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.feature.auth.LoginScreen
import vin.pam.closetly.feature.auth.RegisterScreen
import vin.pam.closetly.feature.home.HomeScreen
import vin.pam.closetly.feature.inspiration.InspirationDetailScreen
import vin.pam.closetly.feature.inspiration.InspirationFormScreen
import vin.pam.closetly.feature.inspiration.InspirationScreen
import vin.pam.closetly.feature.inspiration.InspirationViewModel
import vin.pam.closetly.feature.outfit.OutfitCollectionScreen
import vin.pam.closetly.feature.outfit.OutfitDetailScreen
import vin.pam.closetly.feature.outfit.OutfitFormScreen
import vin.pam.closetly.feature.outfit.OutfitViewModel
import vin.pam.closetly.feature.planner.PlanDetailScreen
import vin.pam.closetly.feature.planner.PlanFormScreen
import vin.pam.closetly.feature.planner.PlannerScreen
import vin.pam.closetly.feature.planner.PlannerViewModel
import vin.pam.closetly.feature.profile.EditProfileScreen
import vin.pam.closetly.feature.profile.ProfileScreen
import vin.pam.closetly.feature.wardrobe.ClothingDetailScreen
import vin.pam.closetly.feature.wardrobe.ClothingFormScreen
import vin.pam.closetly.feature.wardrobe.WardrobeScreen
import vin.pam.closetly.feature.wardrobe.WardrobeViewModel
import vin.pam.closetly.feature.wearhistory.WearHistoryDetailScreen
import vin.pam.closetly.feature.wearhistory.WearHistoryFormScreen
import vin.pam.closetly.feature.wearhistory.WearHistoryScreen
import vin.pam.closetly.feature.wearhistory.WearHistoryViewModel
import vin.pam.closetly.feature.wishlist.WishlistDetailScreen
import vin.pam.closetly.feature.wishlist.WishlistFormScreen
import vin.pam.closetly.feature.wishlist.WishlistScreen
import vin.pam.closetly.feature.wishlist.WishlistViewModel

@Composable
fun AppNavHost(navController: NavHostController, factory: ViewModelProvider.Factory) {
    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            val auth: AuthViewModel = viewModel(factory = factory)
            LoginScreen(
                onDemoEntry = { auth.enterDemo(); navController.navigate(HomeRoute) { popUpTo<LoginRoute> { inclusive = true } } },
                onRegister = { navController.navigate(RegisterRoute) },
            )
        }
        composable<RegisterRoute> {
            val auth: AuthViewModel = viewModel(factory = factory)
            RegisterScreen(
                onDemoEntry = { auth.enterDemo(); navController.navigate(HomeRoute) { popUpTo<LoginRoute> { inclusive = true } } },
                onBack = navController::popBackStack,
            )
        }
        composable<HomeRoute> {
            val auth: AuthViewModel = viewModel(factory = factory)
            val session by auth.session.collectAsStateWithLifecycle()
            HomeScreen(
                userName = session?.displayName ?: "Demo",
                onProfile = { navController.navigate(ProfileRoute) },
                onWardrobe = { navController.navigate(WardrobeRoute) },
                onOutfits = { navController.navigate(OutfitCollectionRoute) },
                onPlanner = { navController.navigate(PlannerRoute) },
                onInspiration = { navController.navigate(InspirationRoute) },
                onWishlist = { navController.navigate(WishlistRoute) },
                onWearHistory = { navController.navigate(WearHistoryRoute) },
            )
        }
        composable<ProfileRoute> {
            val auth: AuthViewModel = viewModel(factory = factory)
            val session by auth.session.collectAsStateWithLifecycle()
            ProfileScreen(
                session = session,
                onEdit = { navController.navigate(EditProfileRoute) },
                onSignOut = { auth.signOut(); navController.navigate(LoginRoute) { popUpTo<HomeRoute> { inclusive = true } } },
                onBack = navController::popBackStack,
            )
        }
        composable<EditProfileRoute> {
            val auth: AuthViewModel = viewModel(factory = factory)
            val session by auth.session.collectAsStateWithLifecycle()
            EditProfileScreen(session, onSave = { name, email -> auth.updateProfile(name, email); navController.popBackStack() }, onBack = navController::popBackStack)
        }

        composable<WardrobeRoute> {
            val vm: WardrobeViewModel = viewModel(factory = factory)
            WardrobeScreen(vm, { navController.navigate(ClothingDetailRoute(it)) }, { navController.navigate(ClothingFormRoute()) }, navController::popBackStack)
        }
        composable<ClothingDetailRoute> { entry ->
            val route = entry.toRoute<ClothingDetailRoute>()
            val vm: WardrobeViewModel = viewModel(factory = factory)
            ClothingDetailScreen(vm, route.clothingId, { navController.navigate(ClothingFormRoute(route.clothingId)) }, navController::popBackStack)
        }
        composable<ClothingFormRoute> { entry ->
            val route = entry.toRoute<ClothingFormRoute>()
            val vm: WardrobeViewModel = viewModel(factory = factory)
            ClothingFormScreen(vm, route.clothingId, navController::popBackStack, navController::popBackStack)
        }

        composable<OutfitCollectionRoute> {
            val vm: OutfitViewModel = viewModel(factory = factory)
            OutfitCollectionScreen(vm, { navController.navigate(OutfitDetailRoute(it)) }, { navController.navigate(OutfitFormRoute()) }, navController::popBackStack)
        }
        composable<OutfitDetailRoute> { entry ->
            val route = entry.toRoute<OutfitDetailRoute>()
            val vm: OutfitViewModel = viewModel(factory = factory)
            OutfitDetailScreen(vm, route.outfitId, { navController.navigate(OutfitFormRoute(route.outfitId)) }, navController::popBackStack)
        }
        composable<OutfitFormRoute> { entry ->
            val route = entry.toRoute<OutfitFormRoute>()
            val vm: OutfitViewModel = viewModel(factory = factory)
            OutfitFormScreen(vm, route.outfitId, navController::popBackStack, navController::popBackStack)
        }

        composable<PlannerRoute> {
            val vm: PlannerViewModel = viewModel(factory = factory)
            PlannerScreen(vm, { navController.navigate(PlanDetailRoute(it)) }, { navController.navigate(PlanFormRoute()) }, navController::popBackStack)
        }
        composable<PlanDetailRoute> { entry ->
            val route = entry.toRoute<PlanDetailRoute>()
            val vm: PlannerViewModel = viewModel(factory = factory)
            PlanDetailScreen(vm, route.planId, { navController.navigate(PlanFormRoute(route.planId)) }, navController::popBackStack)
        }
        composable<PlanFormRoute> { entry ->
            val route = entry.toRoute<PlanFormRoute>()
            val vm: PlannerViewModel = viewModel(factory = factory)
            PlanFormScreen(vm, route.planId, navController::popBackStack, navController::popBackStack)
        }

        composable<InspirationRoute> {
            val vm: InspirationViewModel = viewModel(factory = factory)
            InspirationScreen(vm, { navController.navigate(InspirationDetailRoute(it)) }, { navController.navigate(InspirationFormRoute()) }, navController::popBackStack)
        }
        composable<InspirationDetailRoute> { entry ->
            val route = entry.toRoute<InspirationDetailRoute>()
            val vm: InspirationViewModel = viewModel(factory = factory)
            InspirationDetailScreen(vm, route.inspirationId, { navController.navigate(InspirationFormRoute(route.inspirationId)) }, navController::popBackStack)
        }
        composable<InspirationFormRoute> { entry ->
            val route = entry.toRoute<InspirationFormRoute>()
            val vm: InspirationViewModel = viewModel(factory = factory)
            InspirationFormScreen(vm, route.inspirationId, navController::popBackStack, navController::popBackStack)
        }

        composable<WishlistRoute> {
            val vm: WishlistViewModel = viewModel(factory = factory)
            WishlistScreen(vm, { navController.navigate(WishlistDetailRoute(it)) }, { navController.navigate(WishlistFormRoute()) }, navController::popBackStack)
        }
        composable<WishlistDetailRoute> { entry ->
            val route = entry.toRoute<WishlistDetailRoute>()
            val vm: WishlistViewModel = viewModel(factory = factory)
            WishlistDetailScreen(vm, route.wishlistId, { navController.navigate(WishlistFormRoute(route.wishlistId)) }, navController::popBackStack)
        }
        composable<WishlistFormRoute> { entry ->
            val route = entry.toRoute<WishlistFormRoute>()
            val vm: WishlistViewModel = viewModel(factory = factory)
            WishlistFormScreen(vm, route.wishlistId, navController::popBackStack, navController::popBackStack)
        }

        composable<WearHistoryRoute> {
            val vm: WearHistoryViewModel = viewModel(factory = factory)
            WearHistoryScreen(vm, { navController.navigate(WearHistoryDetailRoute(it)) }, { navController.navigate(WearHistoryFormRoute()) }, navController::popBackStack)
        }
        composable<WearHistoryDetailRoute> { entry ->
            val route = entry.toRoute<WearHistoryDetailRoute>()
            val vm: WearHistoryViewModel = viewModel(factory = factory)
            WearHistoryDetailScreen(vm, route.historyId, { navController.navigate(WearHistoryFormRoute(route.historyId)) }, navController::popBackStack)
        }
        composable<WearHistoryFormRoute> { entry ->
            val route = entry.toRoute<WearHistoryFormRoute>()
            val vm: WearHistoryViewModel = viewModel(factory = factory)
            WearHistoryFormScreen(vm, route.historyId, navController::popBackStack, navController::popBackStack)
        }
    }
}
