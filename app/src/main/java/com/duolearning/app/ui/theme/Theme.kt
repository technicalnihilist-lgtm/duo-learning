package com.duolearning.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuoDarkColors = darkColorScheme(
    primary = DuoPurple,
    onPrimary = Color.White,
    primaryContainer = DuoPurpleDeep,
    onPrimaryContainer = DuoWhite,
    secondary = DuoBlue,
    onSecondary = Color.White,
    tertiary = DuoCyan,
    onTertiary = Color(0xFF06202A),
    background = DuoBlack,
    onBackground = DuoWhite,
    surface = DuoSurface,
    onSurface = DuoWhite,
    surfaceVariant = DuoSurfaceHigh,
    onSurfaceVariant = DuoGrey,
    outline = DuoBorder,
    error = DuoRed,
    onError = Color.White
)

/**
 * Duo Learning is dark by default and uses a violet/blue gradient identity.
 * A light scheme is intentionally not provided.
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
