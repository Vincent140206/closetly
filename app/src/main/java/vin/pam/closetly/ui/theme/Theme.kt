package vin.pam.closetly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GoldenPrimary,
    onPrimary = Heading,
    primaryContainer = SoftYellow,
    onPrimaryContainer = Heading,
    secondary = Body,
    onSecondary = CardWhite,
    secondaryContainer = Vanilla,
    onSecondaryContainer = Body,
    tertiary = GoldenPressed,
    onTertiary = Heading,
    background = OffWhite,
    onBackground = Heading,
    surface = CardWhite,
    onSurface = Body,
    surfaceVariant = Vanilla,
    onSurfaceVariant = Muted,
    outline = Border,
    outlineVariant = Border,
    error = Error,
    onError = CardWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldenPrimary,
    onPrimary = Heading,
    primaryContainer = GoldenPressed,
    onPrimaryContainer = Heading,
    secondary = DarkText,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,
    tertiary = SoftYellow,
    onTertiary = Heading,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = ErrorDark,
    onError = Heading,
)

@Composable
fun ClosetlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = ClosetlyShapes,
        content = content,
    )
}
