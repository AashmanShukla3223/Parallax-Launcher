package com.parallax.parallaxlauncher.ui.modes.razr

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * The four RAZR V3i Quartz finishes documented in the reference material.
 *
 * Each finish carries two independent palettes:
 *  * [RazrHardware] — chassis metal, keypad deck, laser etch, key legends.
 *  * [RazrSkin] — the display chrome. The V3i shipped five themes and the title
 *    bar, content field and selection colour all changed with them.
 */
enum class RazrFinish(val label: String, val short: String) {
    LIGHT_QUARTZ("Light Quartz", "LIGHT"),
    DARK_QUARTZ("Dark Quartz", "DARK"),
    BLUE_QUARTZ("Blue Quartz", "BLUE"),
    ROSE_QUARTZ("Rose Quartz", "ROSE"),
}

/** Hardware colours: chassis, keypad deck, laser etch and key legends. */
data class RazrHardware(
    val chassisTop: Color,
    val chassisMid: Color,
    val chassisLow: Color,
    val chassisEdge: Color,
    val chassisHighlight: Color,
    val bezel: Color,
    val bezelEdge: Color,
    val deckTop: Color,
    val deckMid: Color,
    val deckLow: Color,
    val deckEdge: Color,
    val wellFace: Color,
    val groove: Color,
    val grooveGlow: Color,
    val legend: Color,
    val legendDim: Color,
    val dpadRing: Color,
    val dpadCenter: Color,
    val grilleGlow: Color,
)

/**
 * Display chrome.
 *
 * These values were sampled from photographs of a real handset rather than
 * invented: the stock screen is a *light* UI — a navy status strip, a pale
 * blue-grey title bar, a near-white content field, and a medium-blue selection
 * bar carrying bold white text.
 */
data class RazrSkin(
    val statusTop: Color,
    val statusLow: Color,
    val statusInk: Color,

    val titleTop: Color,
    val titleLow: Color,
    val titleInk: Color,

    val field: Color,
    val fieldAlt: Color,
    val ink: Color,
    val inkDim: Color,

    val selectTop: Color,
    val selectLow: Color,
    val selectInk: Color,

    val softTop: Color,
    val softLow: Color,
    val softInk: Color,

    /** Rounded badge behind the selected icon-grid cell. */
    val badgeFill: Color,
    /** Label colour inside that badge — the stock handsets used yellow. */
    val badgeInk: Color,

    val alert: Color,
)

/** Combined palette used throughout the RAZR UI. */
data class RazrPalette(
    val hw: RazrHardware,
    val ui: RazrSkin,
) {
    val chassisTop get() = hw.chassisTop
    val chassisMid get() = hw.chassisMid
    val chassisLow get() = hw.chassisLow
    val chassisEdge get() = hw.chassisEdge
    val chassisHighlight get() = hw.chassisHighlight
    val bezel get() = hw.bezel
    val bezelEdge get() = hw.bezelEdge
    val deckTop get() = hw.deckTop
    val deckMid get() = hw.deckMid
    val deckLow get() = hw.deckLow
    val deckEdge get() = hw.deckEdge
    val wellFace get() = hw.wellFace
    val groove get() = hw.groove
    val grooveGlow get() = hw.grooveGlow
    val legend get() = hw.legend
    val legendDim get() = hw.legendDim
    val dpadRing get() = hw.dpadRing
    val dpadCenter get() = hw.dpadCenter
    val grilleGlow get() = hw.grilleGlow

    val statusTop get() = ui.statusTop
    val statusLow get() = ui.statusLow
    val statusInk get() = ui.statusInk
    val titleTop get() = ui.titleTop
    val titleLow get() = ui.titleLow
    val titleInk get() = ui.titleInk
    val field get() = ui.field
    val fieldAlt get() = ui.fieldAlt
    val ink get() = ui.ink
    val inkDim get() = ui.inkDim
    val selectTop get() = ui.selectTop
    val selectLow get() = ui.selectLow
    val selectInk get() = ui.selectInk
    val softTop get() = ui.softTop
    val softLow get() = ui.softLow
    val softInk get() = ui.softInk
    val badgeFill get() = ui.badgeFill
    val badgeInk get() = ui.badgeInk
    val alert get() = ui.alert

    /**
     * The stock handsets used Univers, a humanist grotesque. SansSerif is the
     * closest thing available without bundling a licensed face, and the single
     * most important change from the earlier monospace build: bold, mixed case,
     * tight tracking.
     */
    val font: FontFamily = FontFamily.SansSerif

    companion object {
        /** Electric blue laser etch, identical on every Quartz finish. */
        private val Groove = Color(0xFF1E4AE0)
        private val GrooveGlow = Color(0xFF7FA0FF)

        // ---- Hardware ------------------------------------------------------------

        private val SilverHw = RazrHardware(
            chassisTop = Color(0xFFE2E6EB),
            chassisMid = Color(0xFFB4BAC3),
            chassisLow = Color(0xFF7C838D),
            chassisEdge = Color(0xFF4A5058),
            chassisHighlight = Color(0xFFF6F8FA),
            bezel = Color(0xFF1B1E23),
            bezelEdge = Color(0xFF0A0B0D),
            deckTop = Color(0xFFD2D7DD),
            deckMid = Color(0xFF9BA2AC),
            deckLow = Color(0xFF6A717A),
            deckEdge = Color(0xFF3B4048),
            wellFace = Color(0xFFC3C9D1),
            groove = Groove,
            grooveGlow = GrooveGlow,
            legend = Color(0xFFFAFBFD),
            legendDim = Color(0xFFCDD4DE),
            dpadRing = Color(0xFF161A20),
            dpadCenter = Color(0xFF1E2733),
            grilleGlow = Color(0xFF2A57E8),
        )

        private val DarkHw = SilverHw.copy(
            chassisTop = Color(0xFF61666E),
            chassisMid = Color(0xFF3C4148),
            chassisLow = Color(0xFF212429),
            chassisEdge = Color(0xFF131519),
            chassisHighlight = Color(0xFF949BA4),
            bezel = Color(0xFF0E1013),
            deckTop = Color(0xFF585D65),
            deckMid = Color(0xFF383C42),
            deckLow = Color(0xFF1F2226),
            deckEdge = Color(0xFF111316),
            wellFace = Color(0xFF2B2F35),
            dpadRing = Color(0xFF0C0E11),
            dpadCenter = Color(0xFF141A22),
        )

        private val BlueHw = SilverHw.copy(
            chassisTop = Color(0xFF5E93D6),
            chassisMid = Color(0xFF3567A8),
            chassisLow = Color(0xFF1B3A62),
            chassisEdge = Color(0xFF10233C),
            chassisHighlight = Color(0xFFA3C6F0),
            deckTop = Color(0xFF4E82C4),
            deckMid = Color(0xFF2E5A94),
            deckLow = Color(0xFF173257),
            deckEdge = Color(0xFF0D2038),
            wellFace = Color(0xFF2A5580),
            legendDim = Color(0xFFB6CDE8),
        )

        private val RoseHw = SilverHw.copy(
            chassisTop = Color(0xFFE0B2BF),
            chassisMid = Color(0xFFC08B9A),
            chassisLow = Color(0xFF875967),
            chassisEdge = Color(0xFF553440),
            chassisHighlight = Color(0xFFF6DDE3),
            deckTop = Color(0xFFC996A5),
            deckMid = Color(0xFFA06B7A),
            deckLow = Color(0xFF663F4C),
            deckEdge = Color(0xFF40252F),
            wellFace = Color(0xFF8A5764),
            legendDim = Color(0xFFE8C6CE),
        )

        // ---- Display skins -------------------------------------------------------

        /** The stock light theme: pale field, navy chrome, blue selection. */
        private val LightSkin = RazrSkin(
            statusTop = Color(0xFF22386E),
            statusLow = Color(0xFF101E45),
            statusInk = Color(0xFFFFFFFF),
            titleTop = Color(0xFFDCE3F0),
            titleLow = Color(0xFFB7C3D9),
            titleInk = Color(0xFF12161F),
            field = Color(0xFFE9ECF2),
            fieldAlt = Color(0xFFD7DBE4),
            ink = Color(0xFF15204A),
            inkDim = Color(0xFF6A7290),
            selectTop = Color(0xFF3A82D6),
            selectLow = Color(0xFF1E5BAC),
            selectInk = Color(0xFFFFFFFF),
            softTop = Color(0xFFE0E6F0),
            softLow = Color(0xFFC0C9DC),
            softInk = Color(0xFF12161F),
            badgeFill = Color(0xFF1B2A5E),
            badgeInk = Color(0xFFF7C948),
            alert = Color(0xFFC0392B),
        )

        private val DarkSkin = LightSkin.copy(
            statusTop = Color(0xFF2A3038),
            statusLow = Color(0xFF14181D),
            titleTop = Color(0xFF4A525C),
            titleLow = Color(0xFF2E343C),
            titleInk = Color(0xFFF2F5F9),
            field = Color(0xFF1B1F25),
            fieldAlt = Color(0xFF262B33),
            ink = Color(0xFFE8EDF4),
            inkDim = Color(0xFF8B95A4),
            selectTop = Color(0xFF2F6CC4),
            selectLow = Color(0xFF16437E),
            softTop = Color(0xFF39404A),
            softLow = Color(0xFF22272E),
            softInk = Color(0xFFF2F5F9),
            badgeFill = Color(0xFF39465A),
            alert = Color(0xFFFF6B5A),
        )

        private val BlueSkin = LightSkin.copy(
            statusTop = Color(0xFF14406F),
            statusLow = Color(0xFF082A4E),
            titleTop = Color(0xFFCFE1F2),
            titleLow = Color(0xFFA3C4E0),
            ink = Color(0xFF0E2A4A),
            selectTop = Color(0xFF2E8AD0),
            selectLow = Color(0xFF12558F),
            badgeFill = Color(0xFF0F3557),
        )

        private val RoseSkin = LightSkin.copy(
            statusTop = Color(0xFF6B2A44),
            statusLow = Color(0xFF3D1324),
            titleTop = Color(0xFFF3DDE3),
            titleLow = Color(0xFFDDB4C0),
            ink = Color(0xFF4A1F2E),
            selectTop = Color(0xFFD0567C),
            selectLow = Color(0xFF9C3355),
            badgeFill = Color(0xFF4A1F2E),
        )

        fun of(finish: RazrFinish): RazrPalette = when (finish) {
            RazrFinish.LIGHT_QUARTZ -> RazrPalette(SilverHw, LightSkin)
            RazrFinish.DARK_QUARTZ -> RazrPalette(DarkHw, DarkSkin)
            RazrFinish.BLUE_QUARTZ -> RazrPalette(BlueHw, BlueSkin)
            RazrFinish.ROSE_QUARTZ -> RazrPalette(RoseHw, RoseSkin)
        }
    }
}