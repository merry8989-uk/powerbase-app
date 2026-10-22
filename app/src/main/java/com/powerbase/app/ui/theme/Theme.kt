package com.powerbase.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

/**
 * Theme wrapper. The Settings screen drives every argument here, so changing
 * the theme mode, accent colour, text size or density takes effect on the next
 * recomposition — no restart, no dead toggles (Rule 4).
 */
@Composable
fun PowerbaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: Color? = null,
    textScale: Float = 1f,
    densityScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val base = LocalDensity.current
    val scaled = Density(
        density = base.density * densityScale.coerceIn(0.8f, 1.3f),
        fontScale = base.fontScale * textScale.coerceIn(0.8f, 1.6f),
    )

    val scheme = if (darkTheme) DarkColors else LightColors
    val finalScheme = if (accent != null) {
        scheme.copy(
            primary = accent,
            // Keep the label readable on top of whatever accent was chosen.
            onPrimary = if (accent.luminance() > 0.5f) Color.Black else Color.White,
        )
    } else {
        scheme
    }

    CompositionLocalProvider(LocalDensity provides scaled) {
        MaterialTheme(colorScheme = finalScheme, content = content)
    }
}
