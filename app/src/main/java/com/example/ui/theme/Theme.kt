package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioDarkPrimary,
    onPrimary = Color(0xFF11141A),
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = StudioDarkSecondary,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = StudioDarkTertiary,
    onTertiary = Color(0xFF451A03),
    background = StudioDarkBg,
    onBackground = StudioDarkTextPrimary,
    surface = StudioDarkSurface,
    onSurface = StudioDarkTextPrimary,
    surfaceVariant = StudioDarkCard,
    onSurfaceVariant = StudioDarkTextSecondary,
    outline = StudioDarkCardBorder,
    outlineVariant = Color(0xFF333E50),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val StudioLightColorScheme = lightColorScheme(
    primary = StudioLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF9F1239),
    secondary = StudioLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = StudioLightTertiary,
    onTertiary = Color.White,
    background = StudioLightBg,
    onBackground = StudioLightTextPrimary,
    surface = StudioLightSurface,
    onSurface = StudioLightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = StudioLightTextSecondary,
    outline = StudioLightCardBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun DragonAgentTheme(
    darkTheme: Boolean = false, // Light theme by DEFAULT as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun DragonStudioTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = DragonAgentTheme(darkTheme = darkTheme, content = content)
