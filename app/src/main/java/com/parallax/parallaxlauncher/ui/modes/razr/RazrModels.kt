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
    /** RAZR-exclusive game picker. */
    GAMES,
    /** A RAZR-exclusive game in progress. */
    GAME,
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

data class RazrMenuItem(val id: Int, val title: String, val icon: RazrIcon)

/** Main menu, mirroring the V3i manual's menu map. */
val RAZR_MAIN_MENU = listOf(
    RazrMenuItem(1, "Recent Calls", RazrIcon.CALLS),
    RazrMenuItem(2, "Messages", RazrIcon.MESSAGES),
    RazrMenuItem(3, "Phonebook", RazrIcon.PHONEBOOK),
    RazrMenuItem(4, "Web Access", RazrIcon.WEB_ACCESS),
    RazrMenuItem(5, "Games & Apps", RazrIcon.GAMES),
    RazrMenuItem(6, "Multimedia", RazrIcon.MULTIMEDIA),
    RazrMenuItem(7, "Tools", RazrIcon.TOOLS),
    RazrMenuItem(8, "Settings", RazrIcon.SETTINGS),
    RazrMenuItem(9, "Camera", RazrIcon.CAMERA),
)

/** Settings sub-menu, mirroring the V3i manual's settings menu. */
val RAZR_SETTINGS_MENU = listOf(
    RazrMenuItem(101, "Personalize", RazrIcon.SETTINGS),
    RazrMenuItem(102, "Ring Styles", RazrIcon.MULTIMEDIA),
    RazrMenuItem(103, "Connection", RazrIcon.WEB_ACCESS),
    RazrMenuItem(104, "In-Call Setup", RazrIcon.CALLS),
    RazrMenuItem(105, "Initial Setup", RazrIcon.ALARM),
    RazrMenuItem(106, "Phone Status", RazrIcon.FILES),
    RazrMenuItem(107, "Security", RazrIcon.SECURITY),
    RazrMenuItem(108, "Tools", RazrIcon.TOOLS),
    RazrMenuItem(109, "Airplane Mode", RazrIcon.NETWORK),
)

val RAZR_TOOLS_MENU = listOf(
    RazrMenuItem(201, "Calculator", RazrIcon.CALCULATOR),
    RazrMenuItem(202, "Datebook", RazrIcon.CALENDAR),
    RazrMenuItem(203, "Shortcuts", RazrIcon.CHECKLIST),
    RazrMenuItem(204, "Voice Records", RazrIcon.VOICEMAIL),
    RazrMenuItem(205, "Alarm Clock", RazrIcon.ALARM),
    RazrMenuItem(206, "Dialing Services", RazrIcon.CALLS),
    RazrMenuItem(207, "Quick Dial", RazrIcon.PLAYLIST),
)

/** Ring style profiles from the manual's Customize chapter. */
data class RazrRingStyle(val id: Int, val name: String)

val RAZR_RING_STYLES = listOf(
    RazrRingStyle(0, "Loud"),
    RazrRingStyle(1, "Soft"),
    RazrRingStyle(2, "Vibrate"),
    RazrRingStyle(3, "Vibe & Ring"),
    RazrRingStyle(4, "Vibe then Ring"),
    RazrRingStyle(5, "Silent"),
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
data class RazrKey(
    val main: String,
    val letters: String,
    /** Small secondary legend (punctuation, `+`, music note). */
    val sub: String = "",
    /** True when the letter group is etched to the *left* of the digit. */
    val lettersFirst: Boolean = false,
)

object RazrKeypadLayout {
    /**
     * Rows 1-3 mirror at the centre column, exactly as on the handset: the
     * letter group sits to the right of 2, 5 and 8 but to the left of 3, 6
     * and 9. The bottom row carries `0 +` in a single well, with `*` left and
     * the music-note key right.
     */
    val rows: List<List<RazrKey>> = listOf(
        listOf(
            RazrKey("1", "", ".,@"),
            RazrKey("2", "ABC"),
            RazrKey("3", "DEF", lettersFirst = true),
        ),
        listOf(
            RazrKey("4", "GHI"),
            RazrKey("5", "JKL"),
            RazrKey("6", "MNO", lettersFirst = true),
        ),
        listOf(
            RazrKey("7", "PQRS"),
            RazrKey("8", "TUV"),
            RazrKey("9", "WXYZ", lettersFirst = true),
        ),
        listOf(
            RazrKey("*", "", "␣"),
            RazrKey("0", "", "+"),
            RazrKey("#", "", "♫", lettersFirst = true),
        ),
    )
}