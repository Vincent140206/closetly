package vin.pam.closetly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Sage,
    onPrimary = Night,
    primaryContainer = Olive,
    onPrimaryContainer = WarmWhite,
    secondary = Sand,
    tertiary = Clay,
    background = Night,
    onBackground = WarmWhite,
    surface = Night,
    onSurface = WarmWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = OliveDark,
    onPrimary = WarmWhite,
    primaryContainer = Sage,
    secondary = Olive,
    secondaryContainer = Sand,
    tertiary = Clay,
    background = WarmWhite,
    surface = WarmWhite,
    onBackground = Charcoal,
    onSurface = Charcoal,
)

@Composable
fun ClosetlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
