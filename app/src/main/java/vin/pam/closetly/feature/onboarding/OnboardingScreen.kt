package vin.pam.closetly.feature.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize().testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background,
    ) { }
}
