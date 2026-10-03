package com.parallax.parallaxlauncher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

enum class ThemeFont(val displayName: String, val fontFamily: FontFamily) {
    MONOSPACE("Monospace", FontFamily.Monospace),
    SANS_SERIF("Sans-Serif", FontFamily.SansSerif),
    SERIF("Serif", FontFamily.Serif),
    DEFAULT("System", FontFamily.Default);

    companion object {
        fun fromId(id: Int): ThemeFont = entries.getOrElse(id) { MONOSPACE }
    }
}

enum class AccentColor(val displayName: String, val color: Color, val dimColor: Color) {
    AMBER("Amber", Color(0xFFFF9500), Color(0x99FF9500)),
    CYAN("Cyan", Color(0xFF00E5FF), Color(0x9900E5FF)),
    EMERALD("Emerald", Color(0xFF00FF66), Color(0x9900FF66)),
    CRIMSON("Crimson", Color(0xFFFF003C), Color(0x99FF003C)),
    SOLAR("Solar Gold", Color(0xFFFFD60A), Color(0x99FFD60A)),
    MONOCHROME("Clean White", Color(0xFFFFFFFF), Color(0x99FFFFFF));

    companion object {
        fun fromId(id: Int): AccentColor = entries.getOrElse(id) { AMBER }
    }
}

enum class ThemeBackground(val displayName: String, val color: Color, val surfaceColor: Color) {
    OBSIDIAN("Obsidian", Color(0xFF0A0A0B), Color(0xFF15161A)),
    CHARCOAL("Charcoal", Color(0xFF121314), Color(0xFF1E2024)),
    DEEP_NAVY("Deep Space", Color(0xFF030508), Color(0xFF0A101D)),
    PAPER_CREAM("Newsprint Paper", Color(0xFFF4F1EA), Color(0xFFEBE6DC));

    companion object {
        fun fromId(id: Int): ThemeBackground = entries.getOrElse(id) { CHARCOAL }
    }
}