package vin.pam.closetly.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import vin.pam.closetly.core.ui.shell.HomeScreen
import vin.pam.closetly.core.ui.shell.ModulePlaceholderScreen
import vin.pam.closetly.feature.auth.LoginScreen
import vin.pam.closetly.feature.auth.RegisterScreen
import vin.pam.closetly.feature.profile.ProfileScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
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
                onLoginClick = { navController.navigate(LoginRoute) },
            )
        }
        composable<LoginRoute> {
            LoginScreen(
                onRegister = { navController.navigate(RegisterRoute) },
                onBack = navController::popBackStack,
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onLogin = { navController.navigate(LoginRoute) },
                onBack = navController::popBackStack,
            )
        }
        composable<ProfileRoute> {
            ProfileScreen(
                onLogin = { navController.navigate(LoginRoute) },
                onRegister = { navController.navigate(RegisterRoute) },
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
