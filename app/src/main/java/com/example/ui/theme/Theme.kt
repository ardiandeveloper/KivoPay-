package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CleanWhite,
    primaryContainer = SoftBlueBg,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = PrimaryBlueLight,
    onSecondary = CleanWhite,
    tertiary = PrimaryBlueDark,
    background = PageBackground,
    onBackground = TextPrimaryDark,
    surface = CleanWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = SoftBlueBg,
    onSurfaceVariant = TextSecondaryMuted,
    outline = CardBorderColor,
    error = CrimsonError,
    onError = CleanWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = CleanWhite,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = SoftBlueBg,
    secondary = SoftBlueBg,
    onSecondary = PrimaryBlueDark,
    tertiary = PrimaryBlue,
    background = DarkBackground,
    onBackground = CleanWhite,
    surface = DarkSurface,
    onSurface = CleanWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = CrimsonError,
    onError = CleanWhite
)

@Composable
fun KivoPayTheme(
    darkTheme: Boolean = false, // Strictly enforce clean white & royal blue banking palette
    content: @Composable () -> Unit
) {
    // Always prioritize the official KivoPay Light Fintech theme with high contrast dark text
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
