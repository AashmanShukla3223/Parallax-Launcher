package com.parallax.parallaxlauncher.ui.modes.razr

import androidx.compose.ui.graphics.Color

/**
 * The four RAZR V3i Quartz finishes documented in the reference material.
 * The default finish is Silver Quartz (dark anodised metal + electric-blue
 * laser-etched grooves), matching the shipped keypad photograph.
 */
enum class RazrSkin(val label: String, val short: String) {
    DARK_QUARTZ("Dark Quartz", "DARK"),
    SILVER_QUARTZ("Silver Quartz", "SILVER"),
    BLUE_QUARTZ("Blue Quartz", "BLUE"),
    ROSE_QUARTZ("Rose Quartz", "ROSE"),
}

/** Every colour the chassis, keypad and status indicators need for one finish. */
data class RazrPalette(
    // Chassis (upper shell, chin, hinge barrel)
    val chassisTop: Color,
    val chassisMid: Color,
    val chassisLow: Color,
    val chassisEdge: Color,
    val chassisHighlight: Color,
    // Inner display bezel + surround
    val bezel: Color,
    val bezelEdge: Color,
    // Keypad deck
    val deckTop: Color,
    val deckMid: Color,
    val deckLow: Color,
    val deckEdge: Color,
    // Keypad wells
    val wellFace: Color,
    val wellEdge: Color,
    // Laser-etched groove (constant electric blue on every V3i finish)
    val groove: Color,
    val grooveGlow: Color,
    // Key legends
    val legend: Color,
    val legendDim: Color,
    // D-pad
    val dpadFace: Color,
    val dpadRing: Color,
    val dpadCenter: Color,
    // Screen pixels
    val lcdBacklight: Color,
    val lcdOff: Color,
    val lcdGrid: Color,
    // Status bar / UI ink
    val ink: Color,
    val inkDim: Color,
    val accent: Color,
    val alert: Color,
) {
    companion object {
        /** Electric blue laser etch — identical on all four Quartz finishes. */
        private val Groove = Color(0xFF2B4CFF)
        private val GrooveGlow = Color(0xFF7FA4FF)

        val DarkQuartz = RazrPalette(
            chassisTop = Color(0xFF5A5F66),
            chassisMid = Color(0xFF3A3E45),
            chassisLow = Color(0xFF1E2126),
            chassisEdge = Color(0xFF14161A),
            chassisHighlight = Color(0xFF8E959E),
            bezel = Color(0xFF101216),
            bezelEdge = Color(0xFF07080A),
            deckTop = Color(0xFF4E535A),
            deckMid = Color(0xFF2E3238),
            deckLow = Color(0xFF17191D),
            deckEdge = Color(0xFF101215),
            wellFace = Color(0xFF22262C),
            wellEdge = Color(0xFF0C0E11),
            groove = Groove,
            grooveGlow = GrooveGlow,
            legend = Color(0xFFF2F5FA),
            legendDim = Color(0xFFA9B3C2),
            dpadFace = Color(0xFF3A4049),
            dpadRing = Color(0xFF4C545F),
            dpadCenter = Color(0xFFB9C0C9),
            lcdBacklight = Color(0xFFBFD8C8),
            lcdOff = Color(0xFF10160F),
            lcdGrid = Color(0x14000000),
            ink = Color(0xFF0E1A10),
            inkDim = Color(0xFF3C5544),
            accent = Color(0xFF1E7B3C),
            alert = Color(0xFFB3261E),
        )

        val SilverQuartz = RazrPalette(
            chassisTop = Color(0xFFD7DBE1),
            chassisMid = Color(0xFF9DA4AE),
            chassisLow = Color(0xFF5F666F),
            chassisEdge = Color(0xFF3A4048),
            chassisHighlight = Color(0xFFF2F5F9),
            bezel = Color(0xFF1B1E23),
            bezelEdge = Color(0xFF0A0B0D),
            deckTop = Color(0xFFB9C0C9),
            deckMid = Color(0xFF868E99),
            deckLow = Color(0xFF4E555E),
            deckEdge = Color(0xFF333940),
            wellFace = Color(0xFF6A727C),
            wellEdge = Color(0xFF2A2F35),
            groove = Groove,
            grooveGlow = GrooveGlow,
            legend = Color(0xFF11151A),
            legendDim = Color(0xFF39424E),
            dpadFace = Color(0xFF8C949F),
            dpadRing = Color(0xFF6D757F),
            dpadCenter = Color(0xFFF0F3F7),
            lcdBacklight = Color(0xFFCFE0CE),
            lcdOff = Color(0xFF121810),
            lcdGrid = Color(0x12000000),
            ink = Color(0xFF12200F),
            inkDim = Color(0xFF415A44),
            accent = Color(0xFF1E7B3C),
            alert = Color(0xFFB3261E),
        )

        val BlueQuartz = RazrPalette(
            chassisTop = Color(0xFF4E7FC4),
            chassisMid = Color(0xFF2E5590),
            chassisLow = Color(0xFF17304F),
            chassisEdge = Color(0xFF0D1D31),
            chassisHighlight = Color(0xFF8FB6E8),
            bezel = Color(0xFF0C1520),
            bezelEdge = Color(0xFF05080D),
            deckTop = Color(0xFF3F6BAC),
            deckMid = Color(0xFF254A7C),
            deckLow = Color(0xFF122741),
            deckEdge = Color(0xFF0A1727),
            wellFace = Color(0xFF1B3355),
            wellEdge = Color(0xFF06101D),
            groove = Color(0xFF63E0FF),
            grooveGlow = Color(0xFFB4F0FF),
            legend = Color(0xFFF0F7FF),
            legendDim = Color(0xFFA8C4E0),
            dpadFace = Color(0xFF2C5484),
            dpadRing = Color(0xFF3E6EA6),
            dpadCenter = Color(0xFFCFE2F7),
            lcdBacklight = Color(0xFFC6DCEF),
            lcdOff = Color(0xFF0C141C),
            lcdGrid = Color(0x14000000),
            ink = Color(0xFF0C1A28),
            inkDim = Color(0xFF37536E),
            accent = Color(0xFF2B6CB0),
            alert = Color(0xFFC62828),
        )

        val RoseQuartz = RazrPalette(
            chassisTop = Color(0xFFD9A9B6),
            chassisMid = Color(0xFFB87E8F),
            chassisLow = Color(0xFF7E5161),
            chassisEdge = Color(0xFF4E303B),
            chassisHighlight = Color(0xFFF3D5DD),
            bezel = Color(0xFF1B1216),
            bezelEdge = Color(0xFF090607),
            deckTop = Color(0xFFBE8797),
            deckMid = Color(0xFF96606F),
            deckLow = Color(0xFF5E3946),
            deckEdge = Color(0xFF3A222B),
            wellFace = Color(0xFF7C4E5C),
            wellEdge = Color(0xFF2E1B23),
            groove = Color(0xFF7C4CFF),
            grooveGlow = Color(0xFFC0A8FF),
            legend = Color(0xFFFFF4F7),
            legendDim = Color(0xFFE0BFC9),
            dpadFace = Color(0xFF9A6273),
            dpadRing = Color(0xFFB07F90),
            dpadCenter = Color(0xFFF6DEE5),
            lcdBacklight = Color(0xFFEDD3D8),
            lcdOff = Color(0xFF180F12),
            lcdGrid = Color(0x14000000),
            ink = Color(0xFF221016),
            inkDim = Color(0xFF6B4753),
            accent = Color(0xFFA03A55),
            alert = Color(0xFFB3261E),
        )

        fun of(skin: RazrSkin): RazrPalette = when (skin) {
            RazrSkin.DARK_QUARTZ -> DarkQuartz
            RazrSkin.SILVER_QUARTZ -> SilverQuartz
            RazrSkin.BLUE_QUARTZ -> BlueQuartz
            RazrSkin.ROSE_QUARTZ -> RoseQuartz
        }
    }
}