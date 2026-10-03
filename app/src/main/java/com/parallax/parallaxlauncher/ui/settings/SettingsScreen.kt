package com.parallax.parallaxlauncher.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.settings.Settings
import com.parallax.parallaxlauncher.ui.theme.AccentColor
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import com.parallax.parallaxlauncher.ui.theme.ThemeBackground
import com.parallax.parallaxlauncher.ui.theme.ThemeFont
import kotlin.math.roundToInt

private val Steel = Color(0xFF2A2B2F)

private val styles = listOf(
    1 to "INDUSTRIAL RIG",
    2 to "CELESTIAL ORBITOS",
    3 to "SWISS BROADSHEET",
    4 to "CYBERDECK HUD",
    5 to "CINECAM RANGEFINDER",
)

@Composable
fun SettingsScreen(
    settings: Settings,
    onChange: ((Settings) -> Settings) -> Unit,
    isDefaultHome: Boolean,
    onSetDefault: () -> Unit,
    onClearCounts: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalParallaxPalette.current
    val accent = palette.accent
    val accentDim = palette.accentDim

    fun mono(size: Int, color: Color = accent, bold: Boolean = false) = TextStyle(
        fontFamily = palette.font, fontSize = size.sp, color = color,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, letterSpacing = 1.sp,
    )

    BackHandler(onBack = onClose)
    Column(
        Modifier.fillMaxSize().background(palette.bg).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("CONFIG", style = mono(24, bold = true), modifier = Modifier.weight(1f))
            Key("CLOSE", onClick = onClose, style = mono(13, accent, true))
        }

        Section("STYLE", mono(11, accentDim, true))
        styles.forEach { (id, name) ->
            val sel = settings.mode == id
            Row(
                Modifier.fillMaxWidth()
                    .background(if (sel) accent else Color.Transparent, RoundedCornerShape(8.dp))
                    .border(2.dp, if (sel) Color(0xFF0A0A0B) else Steel, RoundedCornerShape(8.dp))
                    .clickable { onChange { it.copy(mode = id) } }
                    .padding(16.dp),
            ) {
                Text("0$id", style = mono(14, if (sel) Color.Black else accentDim), modifier = Modifier.padding(end = 14.dp))
                Text(name, style = mono(15, if (sel) Color.Black else accent, true))
            }
        }

        Section("THEME & ACCENT COLOR", mono(11, accentDim, true))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccentColor.entries.forEachIndexed { id, acc ->
                val sel = settings.accentColorId == id
                Box(
                    Modifier.size(40.dp)
                        .background(acc.color, CircleShape)
                        .border(if (sel) 3.dp else 1.dp, if (sel) Color.White else Steel, CircleShape)
                        .clickable { onChange { it.copy(accentColorId = id) } },
                )
            }
        }

        Section("BACKGROUND THEME", mono(11, accentDim, true))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeBackground.entries.forEachIndexed { id, bgTheme ->
                val sel = settings.backgroundColorId == id
                Box(
                    Modifier.weight(1f)
                        .background(if (sel) accent else Steel, RoundedCornerShape(6.dp))
                        .clickable { onChange { it.copy(backgroundColorId = id) } }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        bgTheme.displayName.take(8).uppercase(),
                        style = mono(11, if (sel) Color.Black else Color.White, bold = sel),
                    )
                }
            }
        }

        Section("TYPOGRAPHY / FONT", mono(11, accentDim, true))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeFont.entries.forEachIndexed { id, fontTheme ->
                val sel = settings.fontId == id
                Box(
                    Modifier.weight(1f)
                        .background(if (sel) accent else Steel, RoundedCornerShape(6.dp))
                        .clickable { onChange { it.copy(fontId = id) } }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        fontTheme.displayName.uppercase(),
                        style = mono(10, if (sel) Color.Black else Color.White, bold = sel),
                    )
                }
            }
        }

        Section("GENERAL", mono(11, accentDim, true))
        ToggleRow("HAPTIC FEEDBACK", settings.haptics, accent, accentDim, mono(13)) { v -> onChange { it.copy(haptics = v) } }
        ToggleRow("SHOW MODE CHIP", settings.showModeChip, accent, accentDim, mono(13)) { v -> onChange { it.copy(showModeChip = v) } }

        Section("01 INDUSTRIAL RIG", mono(11, accentDim, true))
        SliderRow("DETENT ANGLE", "${settings.detentDeg.roundToInt()}°", settings.detentDeg, 5f..45f, accent, mono(13)) { v ->
            onChange { it.copy(detentDeg = v) }
        }

        Section("02 CELESTIAL", mono(11, accentDim, true))
        SliderRow("GRAVITY", "%.2f".format(settings.gravity), settings.gravity, 0.1f..1.5f, accent, mono(13)) { v ->
            onChange { it.copy(gravity = v) }
        }
        SliderRow("MAX NODES", "${settings.maxNodes}", settings.maxNodes.toFloat(), 8f..60f, accent, mono(13)) { v ->
            onChange { it.copy(maxNodes = v.roundToInt()) }
        }

        Section("04 CYBERDECK", mono(11, accentDim, true))
        ToggleRow("CRT EFFECT (FALLBACK ON OLDER OS)", settings.crt, accent, accentDim, mono(13)) { v -> onChange { it.copy(crt = v) } }

        Section("05 CINECAM", mono(11, accentDim, true))
        ToggleRow("ASPECT 2.39:1 (OFF = 3:2)", settings.cineAspect == 0, accent, accentDim, mono(13)) { v ->
            onChange { it.copy(cineAspect = if (v) 0 else 1) }
        }
        ToggleRow("HISTOGRAM", settings.histogram, accent, accentDim, mono(13)) { v -> onChange { it.copy(histogram = v) } }
        ToggleRow("RULE-OF-THIRDS GRID", settings.grid, accent, accentDim, mono(13)) { v -> onChange { it.copy(grid = v) } }

        Section("SYSTEM", mono(11, accentDim, true))
        Text(if (isDefaultHome) "HOME APP: ACTIVE" else "HOME APP: STANDBY", style = mono(12, accentDim))
        if (!isDefaultHome) Key("SET AS DEFAULT HOME", onClick = onSetDefault, style = mono(13, accent, true))
        Key("RESET LAUNCH COUNTS", onClick = onClearCounts, style = mono(13, accent, true))
        Key("REPLAY ONBOARDING", onClick = onReplayOnboarding, style = mono(13, accent, true))
        Key("RESET ALL SETTINGS", onClick = onReset, style = mono(13, accent, true))
    }
}

@Composable
private fun Section(title: String, style: TextStyle) {
    Text(title, style = style, modifier = Modifier.padding(top = 10.dp))
    Row(Modifier.fillMaxWidth().background(Steel).padding(top = 1.dp)) {}
}

@Composable
private fun Key(label: String, onClick: () -> Unit, style: TextStyle) {
    Text(
        label,
        style = style,
        modifier = Modifier.fillMaxWidth()
            .border(2.dp, Steel, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    )
}

@Composable
private fun ToggleRow(label: String, value: Boolean, accent: Color, dim: Color, style: TextStyle, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Switch(
            checked = value, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black, checkedTrackColor = accent,
                uncheckedThumbColor = dim, uncheckedTrackColor = Steel, uncheckedBorderColor = Steel,
            ),
        )
    }
}

@Composable
private fun SliderRow(
    label: String, display: String, value: Float, range: ClosedFloatingPointRange<Float>,
    accent: Color, style: TextStyle,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = style, modifier = Modifier.weight(1f))
            Text(display, style = style.copy(color = Color.White))
        }
        Slider(
            value = value, onValueChange = onChange, valueRange = range,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent, inactiveTrackColor = Steel),
        )
    }
}
