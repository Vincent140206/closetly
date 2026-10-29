package vin.pam.closetly

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.di.AppContainer

@Composable
fun ClosetlyApp(container: AppContainer) {
    AppNavHost(navController = rememberNavController(), factory = container.viewModelFactory)
}
