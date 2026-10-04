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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.runtime.remember
import com.parallax.parallaxlauncher.core.settings.Settings
import com.parallax.parallaxlauncher.ui.modes.razr.RAZR_RING_STYLES
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette
import com.parallax.parallaxlauncher.ui.modes.razr.RazrFinish
import com.parallax.parallaxlauncher.ui.modes.razr.RazrWallpapers
import com.parallax.parallaxlauncher.ui.theme.AccentColor
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import com.parallax.parallaxlauncher.ui.theme.ThemeBackground
import com.parallax.parallaxlauncher.ui.theme.ThemeFont
import kotlin.math.roundToInt

private val Steel = Color(0xFF3B2A16)

private val styles = listOf(
    1 to "INDUSTRIAL RIG",
    2 to "CELESTIAL ORBITOS",
    3 to "SWISS BROADSHEET",
    4 to "CYBERDECK HUD",
    5 to "CINECAM RANGEFINDER",
    6 to "TELECOM ROTARY DIAL",
    7 to "MOTOROLA RAZR V3i",
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
    val accent = Color(0xFFFFB000)
    val accentDim = Color(0xFFC48626)

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val ringtoneLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == android.app.Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val picked: android.net.Uri? =
                res.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            onChange { it.copy(ringtoneUri = picked?.toString() ?: "silent") }
        }
    }
    val ringtoneTitle = remember(settings.ringtoneUri) {
        when (settings.ringtoneUri) {
            "" -> "SYSTEM DEFAULT"
            "silent" -> "SILENT"
            else -> runCatching {
                android.media.RingtoneManager.getRingtone(ctx, android.net.Uri.parse(settings.ringtoneUri))
                    .getTitle(ctx).uppercase(Locale.ROOT)
            }.getOrDefault("CUSTOM")
        }
    }

    fun mono(size: Int, color: Color = accent, bold: Boolean = false) = TextStyle(
        fontFamily = FontFamily.Monospace, fontSize = size.sp, color = color,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, letterSpacing = 1.sp,
    )

    BackHandler(onBack = onClose)
    Column(
        Modifier.fillMaxSize().background(Color(0xFF090806)).statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Parallax Launcher", style = mono(10, accentDim, true))
                Text("Config", style = mono(24, accent, true))
            }
            Key("Close", onClick = onClose, style = mono(13, accent, true), modifier = Modifier.width(92.dp), fillMax = false)
        }

        Section("STYLE", mono(11, accentDim, true))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.forEach { (id, name) ->
                val sel = settings.mode == id
                Row(
                    Modifier.width(190.dp)
                        .background(if (sel) accent else Color.Transparent, RoundedCornerShape(8.dp))
                        .border(2.dp, if (sel) Color(0xFF0A0A0B) else Steel, RoundedCornerShape(8.dp))
                        .clickable { onChange { it.copy(mode = id) } }
                        .padding(16.dp),
                ) {
                    Text("0$id", style = mono(14, if (sel) Color.Black else accentDim), modifier = Modifier.padding(end = 14.dp))
                    Text(name, style = mono(15, if (sel) Color.Black else accent, true))
                }
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

        Section("07 MOTOROLA RAZR V3i - SKIN & PERSONALIZE", mono(11, accentDim, true))
        Text("QUARTZ FINISH", style = mono(12))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RazrFinish.entries.forEach { skin ->
                val selected = settings.razrSkin == skin
                val swatch = RazrPalette.of(skin)
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(swatch.deckMid)
                        .border(if (selected) 2.dp else 1.dp, if (selected) accent else Steel, RoundedCornerShape(4.dp))
                        .clickable { onChange { it.copy(razrSkin = skin) } }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(swatch.groove)
                            .border(1.dp, swatch.grooveGlow, RoundedCornerShape(2.dp))
                    )
                    Text(
                        skin.short,
                        style = mono(9, if (selected) Color.White else swatch.legendDim, selected),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        Text("WALLPAPER", style = mono(12))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RazrWallpapers.all.forEach { paper ->
                val selected = settings.razrWallpaperIndex == paper.id
                Text(
                    paper.name.take(9).uppercase(Locale.ROOT),
                    style = mono(9, if (selected) Color.Black else Color.White, selected),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) accent else Steel)
                        .clickable { onChange { it.copy(razrWallpaperIndex = paper.id) } }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }

        Text("RING STYLE", style = mono(12))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RAZR_RING_STYLES.forEachIndexed { idx, style ->
                val selected = settings.razrRingStyleIndex == idx
                Text(
                    style.name.uppercase(Locale.ROOT).take(9),
                    style = mono(8, if (selected) Color.Black else Color.White, selected),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) accent else Steel)
                        .clickable { onChange { it.copy(razrRingStyleIndex = idx) } }
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("UNLOCK CODE", style = mono(12), modifier = Modifier.weight(1f))
            Text(settings.razrUnlockCode, style = mono(13, Color.White), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("1234", "0000", "2004").forEach { code ->
                Text(
                    code,
                    style = mono(12, if (settings.razrUnlockCode == code) Color.Black else Color.White, true),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (settings.razrUnlockCode == code) accent else Steel)
                        .clickable { onChange { it.copy(razrUnlockCode = code) } }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("VOICEMAIL NUMBER", style = mono(12), modifier = Modifier.weight(1f))
            Text(
                settings.razrVoicemailNumber.ifBlank { "*123" },
                style = mono(13, Color.White),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("*123", "#646", "123").forEach { num ->
                Text(
                    num,
                    style = mono(12, if (settings.razrVoicemailNumber == num) Color.Black else Color.White, true),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (settings.razrVoicemailNumber == num) accent else Steel)
                        .clickable { onChange { it.copy(razrVoicemailNumber = num) } }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text("DEFAULT UNLOCK CODE IS 1234 PER THE V3i USER GUIDE", style = mono(10, accentDim))

        Section("07 MOTOROLA RAZR V3i TARIFF", mono(11, accentDim, true))
        val currencies = listOf("₹ (Rupees)", "p (Paise)", "$ (Dollars)", "¢ (Cents)")
        Text("CALL BILLING CURRENCY", style = mono(13))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            currencies.forEachIndexed { idx, name ->
                val sym = name.split(" ")[0]
                val selected = (settings.callCurrencyIndex == idx)
                Text(
                    text = sym,
                    style = mono(12, if (selected) Color.Black else Color.White, true),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) accent else Steel)
                        .clickable { onChange { it.copy(callCurrencyIndex = idx) } }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        val minorSymbol = if (settings.callCurrencyIndex <= 1) "p" else "¢"
        val majorSymbol = currencies.getOrElse(settings.callCurrencyIndex) { "₹" }.split(" ")[0]

        SliderRow(
            label = "PER SECOND (MINOR UNITS)",
            display = "$minorSymbol ${trimRate(settings.callPerSecondRate)} / sec",
            value = settings.callPerSecondRate,
            range = 0.1f..100f,
            accent = accent,
            style = mono(13)
        ) { v ->
            onChange { it.copy(callPerSecondRate = (v * 10).roundToInt() / 10f) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "6 PAISE" to 6f,
                "0.20 CENTS" to 0.2f,
                "1 PAISE" to 1f,
            ).forEach { (label, value) ->
                val selected = kotlin.math.abs(settings.callPerSecondRate - value) < 0.05f
                Text(
                    label,
                    style = mono(10, if (selected) Color.Black else Color.White, selected),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) accent else Steel)
                        .clickable { onChange { it.copy(callPerSecondRate = value) } }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }

        SliderRow(
            label = "PER COMPLETED MINUTE (MAJOR UNITS)",
            display = "$majorSymbol ${trimRate(settings.callTariffRate)} / min",
            value = settings.callTariffRate,
            range = 0.1f..10.0f,
            accent = accent,
            style = mono(13)
        ) { v ->
            onChange { it.copy(callTariffRate = (v * 10).roundToInt() / 10f) }
        }

        Text("BOTH RATES ACCRUE AND ARE SUMMED", style = mono(11, accent, true))
        Text(
            "100 PAISE = 1 RUPEE   •   100 CENTS = 1 DOLLAR",
            style = mono(10, Color.White)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("RINGTONE", style = mono(12), modifier = Modifier.weight(1f))
            Text(ringtoneTitle, style = mono(11, Color.White), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key("USE INCLUDED RAZR TONE", onClick = {
                onChange { it.copy(ringtoneUri = "android.resource://${ctx.packageName}/${com.parallax.parallaxlauncher.R.raw.razr_v3_original}") }
            }, style = mono(11, accent, true), modifier = Modifier.weight(1f))
            Key("SYSTEM DEFAULT", onClick = { onChange { it.copy(ringtoneUri = "") } }, style = mono(11, accent, true), modifier = Modifier.weight(1f))
        }
        Key("CHOOSE RINGTONE (PHONE'S SETTINGS)", onClick = {
            val cur = settings.ringtoneUri.takeIf { it.isNotEmpty() && it != "silent" }?.let(android.net.Uri::parse)
            val i = Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_RINGTONE)
                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Incoming call ringtone")
                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, cur)
            }
            runCatching { ringtoneLauncher.launch(i) }
        }, style = mono(13, accent, true))

        Text("RATE IS CHARGED IN WHOLE COMPLETED MINUTES", style = mono(11, accentDim, true))
        Text("${currencies.getOrElse(settings.callCurrencyIndex) { "₹" }.split(" ")[0]} ${String.format(Locale.ROOT, "%.2f", settings.callTariffRate)} per 60 seconds • no fractional per-second charge", style = mono(11, Color.White))

        Text("RAZR V3i HARDWARE PROFILE", style = mono(11, accentDim, true))
        Text("98 × 53 × 13.9 mm  •  100 g  •  2.2 in 176 × 220 display  •  710 mAh removable battery", style = mono(10, Color.White))
        Text("QUAD-BAND GSM  •  BLUETOOTH 1.2  •  MINI-SIM  •  MP3 RINGTONES", style = mono(10, accentDim))

        Key("OPEN MESSAGE NOTIFICATION ACCESS", onClick = {
            runCatching { ctx.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
        }, style = mono(13, accent, true))
        Key("OPEN ANDROID SOUND AND VOLUME SETTINGS", onClick = {
            runCatching { ctx.startActivity(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)) }
        }, style = mono(13, accent, true))
        Key("OPEN ANDROID ALARM SETTINGS", onClick = {
            runCatching { ctx.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS)) }
        }, style = mono(13, accent, true))
        Section("SYSTEM", mono(11, accentDim, true))
        Text(if (isDefaultHome) "HOME APP: ACTIVE" else "HOME APP: STANDBY", style = mono(12, accentDim))
        if (!isDefaultHome) Key("SET AS DEFAULT HOME", onClick = onSetDefault, style = mono(13, accent, true))
        Key("RESET LAUNCH COUNTS", onClick = onClearCounts, style = mono(13, accent, true))
        Key("REPLAY ONBOARDING", onClick = onReplayOnboarding, style = mono(13, accent, true))
        Key("RESET ALL SETTINGS", onClick = onReset, style = mono(13, accent, true))
    }
}

/** Trims trailing zeros so 6.0 reads "6" and 0.20 reads "0.2". */
private fun trimRate(v: Float): String =
    if (v == v.toInt().toFloat()) v.toInt().toString()
    else String.format(Locale.ROOT, "%.2f", v).trimEnd('0').trimEnd('.')

@Composable
private fun Section(title: String, style: TextStyle) {
    Text(title, style = style, modifier = Modifier.padding(top = 10.dp))
    Row(Modifier.fillMaxWidth().background(Steel).padding(top = 1.dp)) {}
}

@Composable
private fun Key(label: String, onClick: () -> Unit, style: TextStyle, modifier: Modifier = Modifier, fillMax: Boolean = true) {
    Text(
        label,
        style = style,
        modifier = (if (fillMax) modifier.fillMaxWidth() else modifier)
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
