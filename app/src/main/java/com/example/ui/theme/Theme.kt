package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PolishDarkPrimary,
    onPrimary = PolishDarkOnPrimary,
    primaryContainer = PolishDarkPrimaryContainer,
    onPrimaryContainer = PolishDarkOnPrimaryContainer,
    secondary = PolishSecondary,
    onSecondary = Color.White,
    secondaryContainer = PolishDarkSurfaceVariant,
    onSecondaryContainer = PolishDarkOnSurface,
    tertiary = IgPink,
    background = PolishDarkBackground,
    onBackground = PolishDarkOnSurface,
    surface = PolishDarkSurface,
    onSurface = PolishDarkOnSurface,
    surfaceVariant = PolishDarkSurfaceVariant,
    onSurfaceVariant = PolishDarkOnSurfaceVariant,
    outline = PolishDarkOutline,
    outlineVariant = PolishDarkOutline.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = PolishOnPrimary,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishSecondary,
    onSecondary = Color.White,
    secondaryContainer = PolishSecondaryContainer,
    onSecondaryContainer = PolishOnPrimaryContainer,
    tertiary = IgPink,
    background = PolishBackground,
    onBackground = PolishOnPrimaryContainer,
    surface = PolishSurface,
    onSurface = PolishOnSurface,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = PolishOnSurfaceVariant,
    outline = PolishOutline,
    outlineVariant = PolishOutline.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


