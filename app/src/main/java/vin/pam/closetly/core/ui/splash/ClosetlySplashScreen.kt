package vin.pam.closetly.core.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun ClosetlySplashScreen(
    modifier: Modifier = Modifier,
    letterModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        ClosetlyLogoAnimation(letterModifier = letterModifier)
    }
}

@Preview(name = "Closetly splash", widthDp = 393, heightDp = 852)
@Composable
private fun ClosetlySplashScreenPreview() {
    ClosetlyTheme(darkTheme = false) {
        var replay by remember { mutableIntStateOf(0) }
        Box(Modifier.fillMaxSize()) {
            key(replay) {
                ClosetlySplashScreen()
            }
            TextButton(
                onClick = { replay++ },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Text("Replay", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

