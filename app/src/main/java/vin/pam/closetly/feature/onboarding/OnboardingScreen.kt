package vin.pam.closetly.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.feature.onboarding.ui.components.OnboardingActions
import vin.pam.closetly.feature.onboarding.ui.components.OnboardingHero
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen")
            .background(colors.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ClosetlySpacing.Xxl),
        ) {
            OnboardingHero()
            Spacer(Modifier.height(ClosetlySpacing.Lg))
            Text(
                text = stringResource(R.string.onboarding_heading),
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
            )
            Spacer(Modifier.height(ClosetlySpacing.Md))
            Text(
                text = stringResource(R.string.onboarding_description),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(ClosetlySpacing.Xl))
            OnboardingActions(onGetStarted = onGetStarted, onLogin = onLogin)
        }
    }
}

@Preview(name = "Closetly Onboarding", widthDp = 393, heightDp = 852)
@Preview(name = "Onboarding narrow, large text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Preview(name = "Onboarding landscape", widthDp = 852, heightDp = 393)
@Composable
private fun OnboardingScreenPreview() {
    ClosetlyTheme(darkTheme = false) {
        OnboardingScreen(onGetStarted = {}, onLogin = {})
    }
}

@Preview(name = "Onboarding dark", widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingDarkPreview() {
    ClosetlyTheme(darkTheme = true) {
        OnboardingScreen(onGetStarted = {}, onLogin = {})
    }
}
