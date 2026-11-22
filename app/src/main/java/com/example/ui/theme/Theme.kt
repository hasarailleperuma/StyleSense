package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StyleSensePrimary,
    onPrimary = StyleSenseOnPrimary,
    primaryContainer = StyleSensePrimaryContainer,
    onPrimaryContainer = StyleSenseOnPrimaryContainer,
    secondary = StyleSenseSecondary,
    onSecondary = StyleSenseOnSecondary,
    secondaryContainer = StyleSenseSecondaryContainer,
    onSecondaryContainer = StyleSenseOnSecondaryContainer,
    tertiary = StyleSenseTertiary,
    onTertiary = StyleSenseOnTertiary,
    tertiaryContainer = StyleSenseTertiaryContainer,
    onTertiaryContainer = StyleSenseOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = StyleSensePrimary,
    onPrimary = StyleSenseOnPrimary,
    primaryContainer = StyleSensePrimaryContainer,
    onPrimaryContainer = StyleSenseOnPrimaryContainer,
    secondary = StyleSenseSecondary,
    onSecondary = StyleSenseOnSecondary,
    secondaryContainer = StyleSenseSecondaryContainer,
    onSecondaryContainer = StyleSenseOnSecondaryContainer,
    tertiary = StyleSenseTertiary,
    onTertiary = StyleSenseOnTertiary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun StyleSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our bespoke couture colors for colorimetry consistency
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StyleSenseTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
