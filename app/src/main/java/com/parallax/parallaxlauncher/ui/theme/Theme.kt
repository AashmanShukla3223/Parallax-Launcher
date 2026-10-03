package com.parallax.parallaxlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class ParallaxPalette(
    val accent: Color = Color(0xFFFF9500),
    val accentDim: Color = Color(0x99FF9500),
    val bg: Color = Color(0xFF121314),
    val surface: Color = Color(0xFF1E2024),
    val border: Color = Color(0xFF2A2B2F),
    val font: FontFamily = FontFamily.Monospace,
)

val LocalParallaxPalette = staticCompositionLocalOf { ParallaxPalette() }

@Composable
fun ParallaxLauncherTheme(
    accentId: Int = 0,
    backgroundId: Int = 1,
    fontId: Int = 0,
    content: @Composable () -> Unit,
) {
    val accentEnum = AccentColor.fromId(accentId)
    val bgEnum = ThemeBackground.fromId(backgroundId)
    val fontEnum = ThemeFont.fromId(fontId)

    val customPalette = ParallaxPalette(
        accent = accentEnum.color,
        accentDim = accentEnum.dimColor,
        bg = bgEnum.color,
        surface = bgEnum.surfaceColor,
        border = Color(0xFF2A2B2F),
        font = fontEnum.fontFamily,
    )

    val colorScheme = darkColorScheme(
        primary = customPalette.accent,
        background = customPalette.bg,
        surface = customPalette.surface,
    )

    CompositionLocalProvider(LocalParallaxPalette provides customPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}