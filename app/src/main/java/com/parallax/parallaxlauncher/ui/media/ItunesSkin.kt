package com.parallax.parallaxlauncher.ui.media

import androidx.compose.ui.graphics.Color

/**
 * The visual language of iTunes 4/5, circa 2005.
 *
 * ## Why this exists in a RAZR mode
 *
 * The V3i's "i" is not an iteration number. It shipped in 2005 as the first
 * Motorola handset to carry iTunes, under a deal with Apple — so a music player
 * on this phone is supposed to look like iTunes, not like the rest of the
 * handset. That makes this the one place in Mode 7 where the palette is
 * brushed metal and LCD-sage rather than the near-white field and navy strip
 * used everywhere else. It is intentional, and it is the only such exception.
 *
 * ## Colours are sampled, not invented
 *
 * iTunes 5 on Windows XP was Aqua-brushed: a light silver face with fine
 * vertical pinstripes, a bevelled edge lit from the top, round chrome buttons,
 * and a sunken pale-sage LCD with dark engraved text. These are hand-picked
 * approximations of that, chosen to survive being scaled down to a 176x220
 * panel, where a subtle gradient just reads as mud.
 */
object ItunesSkin {
    /** Brushed face, lit from the top-left as the OS drew it. */
    val MetalHi = Color(0xFFE6E6E9)
    val MetalMid = Color(0xFFD2D2D7)
    val MetalLo = Color(0xFFB9B9C0)
    val MetalEdgeHi = Color(0xFFF7F7F8)
    val MetalEdgeLo = Color(0xFF8A8A91)
    val MetalLine = Color(0x33000000)
    val MetalLineHi = Color(0x22FFFFFF)

    /** The sunken display. Sage rather than green, which is what it actually was. */
    val LcdHi = Color(0xFFC8CEB9)
    val LcdLo = Color(0xFFADB49F)
    val LcdInk = Color(0xFF2F332A)
    val LcdInkDim = Color(0xFF6B7261)
    val LcdEdge = Color(0xFF7E8474)

    /** Selection blue, the accent iTunes used for the playing row and active toggles. */
    val Accent = Color(0xFF2F6FD0)
    val AccentHi = Color(0xFF5A93E8)

    /** Round chrome buttons. */
    val Chrome = Color(0xFFDCDCDF)
    val ChromeEdge = Color(0xFF8F8F96)
    val ChromeGloss = Color(0xFFFFFFFF)
    val ChromeInk = Color(0xFF3A3A40)
}