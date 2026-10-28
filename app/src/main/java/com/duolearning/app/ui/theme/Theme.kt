package com.duolearning.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuoDarkColors = darkColorScheme(
    primary = DuoGreen,
    onPrimary = Color(0xFF06210A),
    primaryContainer = DuoGreenDark,
    onPrimaryContainer = DuoWhite,
    secondary = DuoBlue,
    onSecondary = Color(0xFF04222F),
    tertiary = DuoPurple,
    onTertiary = Color(0xFF1A1030),
    background = DuoBlack,
    onBackground = DuoWhite,
    surface = DuoSurface,
    onSurface = DuoWhite,
    surfaceVariant = DuoSurfaceHigh,
    onSurfaceVariant = DuoGrey,
    outline = DuoBorder,
    error = DuoRed,
    onError = DuoWhite
)

/**
 * Duo Learning is dark by default. A light scheme is intentionally not
 * provided: the product spec fixes the dark theme as the default look, and
 * offering a light theme would add visual surface area without a user benefit.
 */
@Composable
fun DuoLearningTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DuoDarkColors,
        typography = DuoTypography,
        content = content
    )
}
