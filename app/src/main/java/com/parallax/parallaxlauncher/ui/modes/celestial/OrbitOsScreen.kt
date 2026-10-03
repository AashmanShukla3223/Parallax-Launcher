package com.parallax.parallaxlauncher.ui.modes.celestial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.sensor.SensorHub
import com.parallax.parallaxlauncher.ui.modes.celestial.physics.CelestialNode
import com.parallax.parallaxlauncher.ui.modes.celestial.physics.VerletSimulation
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import kotlin.random.Random

private const val MAX_NODES = 36

@Composable
fun OrbitOsScreen(
    repo: AppsRepository,
    sensors: SensorHub,
    haptics: HapticEngine,
    gravity: Float = 0.55f,
    maxNodes: Int = MAX_NODES,
) {
    val palette = LocalParallaxPalette.current
    val nodePalette = remember(palette.accent) {
        listOf(
            palette.accent,
            Color(0xFF64D2FF),
            Color(0xFFBF5AF2),
            Color(0xFFFFD60A),
            Color(0xFFFFFFFF),
        )
    }

    val apps by repo.apps.collectAsState()
    val counts by repo.launchCounts.collectAsState()
    val tilt by sensors.tilt.collectAsState()
    val tiltNow by rememberUpdatedState(tilt)
    val sim = remember { VerletSimulation() }
    sim.gravity = gravity
    val measurer = rememberTextMeasurer()
    val baseR = with(LocalDensity.current) { 22.dp.toPx() }
    val stars = remember {
        val r = Random(7)
        List(90) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) } // x, y, depth
    }
    var frame by remember { mutableIntStateOf(0) }
    var dragged by remember { mutableStateOf<CelestialNode?>(null) }

    // Rebuild node set when the app list changes (keep positions of survivors).
    LaunchedEffect(apps, maxNodes) {
        val old = sim.nodes.associateBy { it.app.packageName }
        val top = apps.sortedByDescending { counts[it.packageName] ?: 0 }.take(maxNodes)
        val rnd = Random(3)
        sim.nodes = top.map { a ->
            old[a.packageName] ?: CelestialNode(
                a,
                rnd.nextFloat() * sim.width.coerceAtLeast(1f),
                rnd.nextFloat() * sim.height.coerceAtLeast(1f),
                counts[a.packageName] ?: 0,
                baseR,
            )
        }
    }
    // Launch frequency changes mass live.
    LaunchedEffect(counts) {
        sim.nodes.forEach { it.setWeight(counts[it.app.packageName] ?: 0, baseR) }
    }
    LaunchedEffect(Unit) {
        var t0 = 0L
        while (true) {
            androidx.compose.runtime.withFrameNanos { n ->
                if (t0 == 0L) t0 = n
                sim.step(tiltNow.gx, tiltNow.gy, (n - t0) / 1e9f)
                frame++
            }
        }
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(palette.bg)
            .onSizeChanged { sim.width = it.width.toFloat(); sim.height = it.height.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    sim.hit(p.x, p.y)?.let { haptics.click(); repo.launch(it.app) }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { p -> dragged = sim.hit(p.x, p.y)?.also { it.pinned = true } },
                    onDrag = { c, d ->
                        c.consume()
                        dragged?.let { it.oldX = it.oldY; it.oldY = it.y; it.x += d.x; it.y += d.y }
                    },
                    onDragEnd = { dragged?.pinned = false; dragged = null },
                    onDragCancel = { dragged?.pinned = false; dragged = null },
                )
            },
    ) {
        @Suppress("UNUSED_VARIABLE") val tick = frame // subscribe to physics frames
        // Parallax starfield drifts opposite to tilt.
        for ((sx, sy, depth) in stars) {
            val x = (sx * size.width - tiltNow.gx * depth * 24f).mod(size.width)
            val y = (sy * size.height - tiltNow.gy * depth * 24f).mod(size.height)
            drawCircle(Color.White.copy(alpha = 0.15f + 0.5f * depth), 0.6f + depth * 1.4f, Offset(x, y))
        }
        // Gravity rings
        val c = Offset(size.width / 2f, size.height / 2f)
        for (i in 1..4) {
            drawCircle(palette.accentDim.copy(alpha = 0.12f), size.minDimension * 0.18f * i, c, style = Stroke(1f))
        }
        for ((i, n) in sim.nodes.withIndex()) {
            val col = nodePalette[i % nodePalette.size]
            val o = Offset(n.x, n.y)
            drawCircle(
                Brush.radialGradient(listOf(col.copy(alpha = 0.35f), Color.Transparent), o, n.radius * 2.2f),
                n.radius * 2.2f, o,
            )
            drawCircle(Color(0xFF05070C), n.radius, o)
            drawCircle(col, n.radius, o, style = Stroke(2f))
            val glyph = measurer.measure(
                n.app.label.take(1).uppercase(),
                TextStyle(fontFamily = palette.font, color = col, fontSize = (n.radius * 0.7f / density).sp, fontWeight = FontWeight.Light),
            )
            drawText(glyph, topLeft = Offset(o.x - glyph.size.width / 2f, o.y - glyph.size.height / 2f))
            if (n.mass > 1.6f) {
                val lbl = measurer.measure(
                    n.app.label.take(12).uppercase(),
                    TextStyle(fontFamily = palette.font, color = col.copy(alpha = 0.8f), fontSize = 9.sp, letterSpacing = 1.sp),
                )
                drawText(lbl, topLeft = Offset(o.x - lbl.size.width / 2f, o.y + n.radius + 4f))
            }
        }
    }
}
