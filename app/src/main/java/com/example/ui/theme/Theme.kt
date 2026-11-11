package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = OnPrimaryDark,
    primaryContainer = EthiopianGreen,
    onPrimaryContainer = Color.White,
    secondary = SecondaryBlue,
    onSecondary = OnSecondaryBlue,
    secondaryContainer = SecondaryContainerBlue,
    tertiary = TertiaryPink,
    error = ErrorRed,
    errorContainer = ErrorContainerRed,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EthiopianGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5), // Soft emerald green container
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF06B6D4), // Cyan/Blue
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = Color(0xFFF59E0B), // Bright Amber Gold
    onTertiary = Color.White,
    error = Color(0xFFEF4444), // Soft bright red
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF8FAFC), // Slate 50 background
    onBackground = Color(0xFF0F172A), // Slate 900 text
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569), // Slate 600 text
    outline = Color(0xFFCBD5E1), // Slate 300
    outlineVariant = Color(0xFFE2E8F0) // Slate 200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
