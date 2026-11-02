package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = !isDayTheme,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val mode = ThemeState.themeMode
    val accent = ThemeState.accentColor

    val colorScheme = if (mode == AppThemeMode.DAY) {
        lightColorScheme(
            primary = HealthTeal,
            onPrimary = IconOnAccent,
            primaryContainer = HealthSurfaceVariant,
            onPrimaryContainer = TextHealthPrimary,
            secondary = BioHealingGreen,
            onSecondary = IconOnAccent,
            secondaryContainer = BioGreenContainer,
            onSecondaryContainer = BioGreenGlow,
            background = HealthBg,
            surface = HealthSurface,
            onBackground = TextHealthPrimary,
            onSurface = TextHealthPrimary,
            surfaceVariant = HealthSurfaceVariant,
            onSurfaceVariant = TextHealthSecondary,
            outline = HealthBorder,
            outlineVariant = HealthBorder.copy(alpha = 0.5f),
            error = AlertPharmacyRuby
        )
    } else {
        darkColorScheme(
            primary = HealthTealBright,
            onPrimary = HealthBg,
            primaryContainer = HealthSurfaceVariant,
            onPrimaryContainer = TextHealthPrimary,
            secondary = BioHealingGreen,
            onSecondary = HealthBg,
            secondaryContainer = BioGreenContainer,
            onSecondaryContainer = BioGreenGlow,
            background = HealthBg,
            surface = HealthSurface,
            onBackground = TextHealthPrimary,
            onSurface = TextHealthPrimary,
            surfaceVariant = HealthSurfaceVariant,
            onSurfaceVariant = TextHealthSecondary,
            outline = HealthBorder,
            outlineVariant = HealthBorder.copy(alpha = 0.5f),
            error = AlertPharmacyRuby
        )
    }

    key(mode, accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
