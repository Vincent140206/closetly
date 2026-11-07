package vin.pam.closetly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import vin.pam.closetly.core.navigation.AppNavHost
import vin.pam.closetly.core.ui.splash.ClosetlySplashScreen
import vin.pam.closetly.di.AppContainer

@Composable
fun ClosetlyApp(container: AppContainer) {
    val navController = rememberNavController()
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    if (splashFinished) {
        AppNavHost(navController = navController, container = container)
    } else {
        ClosetlySplashScreen(onAnimationFinished = { splashFinished = true })
    }
}
