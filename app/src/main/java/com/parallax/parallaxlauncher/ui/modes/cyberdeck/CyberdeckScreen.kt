package com.parallax.parallaxlauncher.ui.modes.cyberdeck

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.cyberdeck.components.Alert
import com.parallax.parallaxlauncher.ui.modes.cyberdeck.components.OscilloscopeGraph
import com.parallax.parallaxlauncher.ui.modes.cyberdeck.components.term
import com.parallax.parallaxlauncher.ui.modes.cyberdeck.shaders.CrtShader
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import java.util.Locale

private const val HISTORY = 60

private fun addr(a: AppInfo) =
    "0x" + a.packageName.hashCode().toUInt().toString(16).padStart(8, '0').uppercase(Locale.ROOT)

@Composable
fun CyberdeckScreen(
    repo: AppsRepository,
    telemetry: TelemetryService,
    haptics: HapticEngine,
    crtEnabled: Boolean = true,
) {
    val palette = LocalParallaxPalette.current
    val apps by repo.apps.collectAsState()
    val tel by telemetry.state.collectAsState()
    val crt = remember { CrtShader() }
    val cpu = remember { mutableStateListOf<Float>() }
    val net = remember { mutableStateListOf<Float>() }
    val log = remember { mutableStateListOf("[SYS_INIT] SUBSYSTEM 0 ONLINE") }
    var query by remember { mutableStateOf("") }
    var target by remember { mutableStateOf<AppInfo?>(null) }

    LaunchedEffect(tel) {
        // CPU: real load if readable, else frequency relative to the best seen.
        val v = if (tel.cpuLoadPct >= 0) tel.cpuLoadPct.toFloat()
        else if (tel.cpuMhz > 0) tel.cpuMhz / 30f else 0f
        cpu.add(v); if (cpu.size > HISTORY) cpu.removeAt(0)
        net.add(tel.rxKbps + tel.txKbps); if (net.size > HISTORY) net.removeAt(0)
        log.add(
            "[OK] BAT ${tel.batteryMv}mV ${tel.batteryPct}% " +
                (if (tel.tempC.isNaN()) "" else "${"%.1f".format(tel.tempC)}C ") +
                "RX ${"%.0f".format(tel.rxKbps)}K",
        )
        if (log.size > 6) log.removeAt(0)
    }

    val filtered = remember(apps, query) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(palette.bg)
            .graphicsLayer { if (crtEnabled) crt.apply(this) else renderEffect = null },
    ) {
        if (crtEnabled && !crt.supported) {
            Canvas(Modifier.fillMaxSize()) {
                crt.drawFallbackScanlines(this)
            }
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("SUBSYSTEM 0 // TACTICAL HUD", style = term(13))
            OscilloscopeGraph(
                "CPU", if (tel.cpuLoadPct >= 0) "${tel.cpuLoadPct}%" else if (tel.cpuMhz > 0) "${tel.cpuMhz}MHz" else "N/A",
                cpu, if (tel.cpuLoadPct >= 0) 100f else (cpu.maxOrNull() ?: 1f).coerceAtLeast(1f),
            )
            OscilloscopeGraph(
                "NET", "RX ${"%.0f".format(tel.rxKbps)} TX ${"%.0f".format(tel.txKbps)} KB/s",
                net, (net.maxOrNull() ?: 1f).coerceAtLeast(10f), color = palette.accent,
            )
            MemoryBank(tel.ramUsedMb, tel.ramTotalMb)

            // Command-line filter
            Row(Modifier.fillMaxWidth().border(1.dp, palette.accentDim).padding(8.dp)) {
                Text("> ", style = term(14))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = term(14),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier.weight(1f),
                )
            }

            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(filtered, key = { it.packageName }) { app ->
                    val selected = target == app
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .reticle(selected)
                            .clickable {
                                if (selected) { haptics.thud(); repo.launch(app) }
                                else { haptics.click(); target = app }
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    ) {
                        Text(addr(app), style = term(11, palette.accentDim), modifier = Modifier.padding(end = 10.dp))
                        Text(
                            app.label.uppercase().take(24),
                            style = term(13, if (selected) Color.White else palette.accent),
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) Text("[EXEC]", style = term(11, Alert))
                    }
                }
            }

            Column(Modifier.fillMaxWidth().border(1.dp, palette.accentDim.copy(alpha = 0.5f)).padding(6.dp)) {
                log.takeLast(5).forEach { Text(it, style = term(10, palette.accentDim), maxLines = 1) }
            }
        }
    }
}

@Composable
private fun MemoryBank(used: Int, total: Int) {
    val palette = LocalParallaxPalette.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text("[RAM]", style = term(11, palette.accentDim), modifier = Modifier.weight(1f))
            Text(if (total > 0) "$used/${total}MB" else "N/A", style = term(11))
        }
        Canvas(Modifier.fillMaxWidth().height(10.dp).padding(top = 2.dp)) {
            val cells = 40
            val gap = 2f
            val w = (size.width - gap * (cells - 1)) / cells
            val on = if (total > 0) (used.toFloat() / total * cells).toInt() else 0
            for (i in 0 until cells) {
                val c = if (i < on) (if (i > cells * 0.85f) Alert else palette.accent) else palette.accentDim.copy(alpha = 0.2f)
                drawRect(c, Offset(i * (w + gap), 0f), androidx.compose.ui.geometry.Size(w, size.height))
            }
        }
    }
}

/** Corner-bracket targeting reticle drawn around the selected row. */
private fun Modifier.reticle(active: Boolean): Modifier = if (!active) this else drawBehind {
    val l = 14f
    val s = 2f
    val w = size.width; val h = size.height
    fun seg(a: Offset, b: Offset) = drawLine(Alert, a, b, s)
    seg(Offset(0f, 0f), Offset(l, 0f)); seg(Offset(0f, 0f), Offset(0f, l))
    seg(Offset(w, 0f), Offset(w - l, 0f)); seg(Offset(w, 0f), Offset(w, l))
    seg(Offset(0f, h), Offset(l, h)); seg(Offset(0f, h), Offset(0f, h - l))
    seg(Offset(w, h), Offset(w - l, h)); seg(Offset(w, h), Offset(w, h - l))
    drawRect(Alert.copy(alpha = 0.08f))
}
