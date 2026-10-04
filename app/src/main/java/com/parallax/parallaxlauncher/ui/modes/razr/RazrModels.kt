package com.parallax.parallaxlauncher.ui.modes.razr

/** Every screen the clamshell can show on its 176x220 internal display. */
enum class RazrView {
    /** Flip closed: 96x80 external CSTN cover display only. */
    COVER,

    /** Home screen (flap open, idle). */
    STANDBY,

    /** 4-digit unlock code entry. */
    UNLOCK,

    /** Dialing buffer being typed. */
    DIALING,

    MAIN_MENU,
    APPS_LIST,
    MESSAGES,
    INBOX,
    CALLS,
    NOTEPAD,
    IN_CALL,
    INCOMING,
    TOOLS,
    CALCULATOR,
    DATEBOOK,
    RINGTONES,
    VOICE_DIAL,
    PHOTO,
    SETTINGS,
    THEME,
    CALL_TIMES,
    ABOUT,
}

data class RazrMenuItem(val id: Int, val title: String, val glyph: String)

/** Main menu, mirroring the V3i manual's menu map. */
val RAZR_MAIN_MENU = listOf(
    RazrMenuItem(1, "Recent Calls", "⌸"),
    RazrMenuItem(2, "Messages", "✉"),
    RazrMenuItem(3, "Phonebook", "☰"),
    RazrMenuItem(4, "Web Access", "◍"),
    RazrMenuItem(5, "Games & Apps", "▦"),
    RazrMenuItem(6, "Multimedia", "♪"),
    RazrMenuItem(7, "Tools", "⚙"),
    RazrMenuItem(8, "Settings", "✿"),
    RazrMenuItem(9, "Camera", "◉"),
)

/** Settings sub-menu, mirroring the V3i manual's settings menu. */
val RAZR_SETTINGS_MENU = listOf(
    RazrMenuItem(101, "Personalize", "❐"),
    RazrMenuItem(102, "Ring Styles", "♪"),
    RazrMenuItem(103, "Connection", "⇄"),
    RazrMenuItem(104, "In-Call Setup", "⏱"),
    RazrMenuItem(105, "Initial Setup", "⏻"),
    RazrMenuItem(106, "Phone Status", "▤"),
    RazrMenuItem(107, "Security", "⚿"),
    RazrMenuItem(108, "Tools", "⚙"),
    RazrMenuItem(109, "Airplane Mode", "✈"),
)

val RAZR_TOOLS_MENU = listOf(
    RazrMenuItem(201, "Calculator", "#"),
    RazrMenuItem(202, "Datebook", "▤"),
    RazrMenuItem(203, "Shortcuts", "★"),
    RazrMenuItem(204, "Voice Records", "◉"),
    RazrMenuItem(205, "Alarm Clock", "⏰"),
    RazrMenuItem(206, "Dialing Services", "#"),
    RazrMenuItem(207, "Quick Dial", "⚡"),
)

/** Ring style profiles from the manual's Customize chapter. */
data class RazrRingStyle(val id: Int, val name: String, val glyph: String)

val RAZR_RING_STYLES = listOf(
    RazrRingStyle(0, "Loud", "♪"),
    RazrRingStyle(1, "Soft", "♪"),
    RazrRingStyle(2, "Vibrate", "≈"),
    RazrRingStyle(3, "Vibe & Ring", "≋"),
    RazrRingStyle(4, "Vibe then Ring", "◠"),
    RazrRingStyle(5, "Silent", "⌀"),
)

/** Bundled wallpapers from the reference folder. */
data class RazrWallpaper(val id: Int, val name: String, val res: Int)

object RazrWallpapers {
    val all = listOf(
        RazrWallpaper(0, "Sea & Mountain", com.parallax.parallaxlauncher.R.drawable.razr_mountain_wallpaper),
        RazrWallpaper(1, "V3i Home", com.parallax.parallaxlauncher.R.drawable.razr_v3i_wallpaper),
        RazrWallpaper(2, "Inner Display", com.parallax.parallaxlauncher.R.drawable.razr_v3i_inner_display),
        RazrWallpaper(3, "Silver Quartz", com.parallax.parallaxlauncher.R.drawable.razr_v3i_front_open_closed),
    )
}

/** The 12-key layout exactly as etched on the V3i keypad. */
data class RazrKey(val main: String, val letters: String, val icon: String = "")

object RazrKeypadLayout {
    val rows: List<List<RazrKey>> = listOf(
        listOf(RazrKey("1", "", "@"), RazrKey("2", "ABC"), RazrKey("3", "DEF")),
        listOf(RazrKey("4", "GHI"), RazrKey("5", "JKL"), RazrKey("6", "MNO")),
        listOf(RazrKey("7", "PQRS"), RazrKey("8", "TUV"), RazrKey("9", "WXYZ")),
        listOf(RazrKey("*", ""), RazrKey("0", ""), RazrKey("#", "", "♪")),
    )
}