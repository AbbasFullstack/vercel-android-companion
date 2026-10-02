package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VercelColorScheme = darkColorScheme(
    primary = VercelWhitePure,
    onPrimary = VercelBlack,
    primaryContainer = VercelSurfaceElevated,
    onPrimaryContainer = VercelWhite,
    secondary = VercelBlue,
    onSecondary = VercelWhitePure,
    secondaryContainer = VercelSurfaceVariant,
    onSecondaryContainer = VercelWhite,
    tertiary = StatusReady,
    onTertiary = VercelBlack,
    background = VercelBlack,
    onBackground = VercelWhite,
    surface = VercelSurface,
    onSurface = VercelWhite,
    surfaceVariant = VercelSurfaceVariant,
    onSurfaceVariant = VercelGrayLight,
    outline = VercelBorder,
    outlineVariant = VercelBorderSubtle,
    error = StatusError,
    onError = VercelWhitePure
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VercelColorScheme,
        typography = Typography,
        content = content
    )
}
