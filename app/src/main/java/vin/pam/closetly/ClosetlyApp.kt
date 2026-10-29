package vin.pam.closetly

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import vin.pam.closetly.core.navigation.AppNavHost

@Composable
fun ClosetlyApp() {
    AppNavHost(navController = rememberNavController())
}
