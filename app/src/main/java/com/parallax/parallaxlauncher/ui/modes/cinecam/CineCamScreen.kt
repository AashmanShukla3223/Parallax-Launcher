package com.parallax.parallaxlauncher.ui.modes.cinecam

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.sensor.SensorHub
import com.parallax.parallaxlauncher.core.settings.Settings
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.atan2

/** Dual-stage shutter driven by the hardware volume rocker (see MainActivity.onKeyDown/Up). */
object ShutterEvents {
    val events = MutableSharedFlow<Boolean>(extraBufferCapacity = 8) // true = half-press (down), false = release
}

private val Level = Color(0xFF00FF88)
private val DistanceScale = listOf("0.7m", "1m", "1.5m", "3m", "5m", "∞")

@Composable
fun CineCamScreen(
    repo: AppsRepository,
    sensors: SensorHub,
    telemetry: TelemetryService,
    haptics: HapticEngine,
    settings: Settings,
) {
    val palette = LocalParallaxPalette.current
    val accent = palette.accent
    val font = palette.font

    fun osd(size: Int, color: Color = Color.White, bold: Boolean = false) = TextStyle(
        fontFamily = font, fontSize = size.sp, color = color,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, letterSpacing = 1.sp,
    )

    val ctx = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    val apps by repo.apps.collectAsState()
    val tilt by sensors.tilt.collectAsState()
    val tel by telemetry.state.collectAsState()
    var index by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var dragAcc by remember { mutableFloatStateOf(0f) }
    val hist = remember { mutableStateOf(FloatArray(64)) }
    val selected = apps.getOrNull(index)

    // Volume rocker: press = focus lock (half), release = shutter (launch).
    LaunchedEffect(selected) {
        ShutterEvents.events.collect { down ->
            if (down) { locked = true; haptics.click() }
            else {
                if (locked) { haptics.thud(); selected?.let(repo::launch) }
                locked = false
            }
        }
    }

    // Level detent when crossing into +-0.5 degrees
    val roll = Math.toDegrees(atan2(tilt.gx.toDouble(), tilt.gy.toDouble())).toFloat()
    val isLevel = abs(roll) < 0.5f
    LaunchedEffect(isLevel) { if (isLevel) haptics.tick() }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        if (granted) CameraLayer(hist) else {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NO LIGHT", style = osd(24, accent, true))
                Text("CAMERA ACCESS REQUIRED", style = osd(11, Color.Gray))
                Box(
                    Modifier.padding(top = 16.dp).border(1.dp, accent)
                        .clickable { askPermission.launch(Manifest.permission.CAMERA) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) { Text("GRANT", style = osd(14, accent, true)) }
            }
        }

        // Letterbox bars + frame lines + horizon
        Canvas(Modifier.fillMaxSize()) {
            val ratio = if (settings.cineAspect == 0) 2.39f else 1.5f
            val frameH = size.width / ratio
            val bar = ((size.height - frameH) / 2f).coerceAtLeast(0f)
            val body = palette.bg
            drawRect(body, Offset.Zero, androidx.compose.ui.geometry.Size(size.width, bar))
            drawRect(body, Offset(0f, size.height - bar), androidx.compose.ui.geometry.Size(size.width, bar))
            val top = bar
            val bottom = size.height - bar
            if (settings.grid) {
                for (i in 1..2) {
                    val x = size.width * i / 3f
                    val y = top + (bottom - top) * i / 3f
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(x, top), Offset(x, bottom), 1f)
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, y), Offset(size.width, y), 1f)
                }
            }
            // Corner framelines
            val m = 28f; val l = 36f
            val fl = if (locked) Level else accent
            fun c(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(fl, Offset(x, y), Offset(x + dx * l, y), 3f)
                drawLine(fl, Offset(x, y), Offset(x, y + dy * l), 3f)
            }
            c(m, top + m, 1f, 1f); c(size.width - m, top + m, -1f, 1f)
            c(m, bottom - m, 1f, -1f); c(size.width - m, bottom - m, -1f, -1f)
            // Artificial horizon (roll)
            val cx = size.width / 2f; val cy = (top + bottom) / 2f
            val col = if (isLevel) Level else Color.White.copy(alpha = 0.8f)
            rotate(-roll, Offset(cx, cy)) {
                drawLine(col, Offset(cx - 120.dp.toPx(), cy), Offset(cx - 30.dp.toPx(), cy), 2.dp.toPx())
                drawLine(col, Offset(cx + 30.dp.toPx(), cy), Offset(cx + 120.dp.toPx(), cy), 2.dp.toPx())
            }
            drawLine(col, Offset(cx - 10.dp.toPx(), cy), Offset(cx + 10.dp.toPx(), cy), 1.dp.toPx())
            drawLine(col, Offset(cx, cy - 10.dp.toPx()), Offset(cx, cy + 10.dp.toPx()), 1.dp.toPx())
            // Histogram
            if (settings.histogram) {
                val h = hist.value
                val hx = size.width - 140.dp.toPx() - 16f
                val hy = bottom - 60.dp.toPx() - 16f
                drawRect(Color.Black.copy(alpha = 0.45f), Offset(hx, hy), androidx.compose.ui.geometry.Size(140.dp.toPx(), 60.dp.toPx()))
                val bw = 140.dp.toPx() / h.size
                for (i in h.indices) {
                    val bh = h[i] * 56.dp.toPx()
                    drawRect(Color.White.copy(alpha = 0.8f), Offset(hx + i * bw, hy + 60.dp.toPx() - bh), androidx.compose.ui.geometry.Size(bw, bh))
                }
            }
        }

        // OSD top / bottom
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("f/1.4  1/250  ISO 400", style = osd(11))
                Text(if (locked) "AF-L" else "AF-S", style = osd(11, if (locked) Level else accent, true))
                Text("BAT ${if (tel.batteryPct >= 0) tel.batteryPct else "--"}%", style = osd(11))
            }
            Box(Modifier.weight(1f))
            // Focus ring app selector
            Column(
                Modifier.fillMaxWidth().pointerInput(apps.size) {
                    detectHorizontalDragGestures { c, d ->
                        c.consume()
                        dragAcc += d
                        val step = 70f
                        while (abs(dragAcc) >= step && apps.isNotEmpty()) {
                            val dir = if (dragAcc > 0) -1 else 1
                            index = (index + dir).mod(apps.size)
                            dragAcc -= step * (if (dragAcc > 0) 1 else -1)
                            haptics.tick()
                        }
                    }
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(selected?.label?.uppercase() ?: "NO SUBJECT", style = osd(22, accent, true), maxLines = 1)
                Text(
                    DistanceScale[if (apps.isEmpty()) 0 else index % DistanceScale.size],
                    style = osd(11, Color.White),
                )
                // Focus scale ticks
                Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                    val n = 41
                    for (i in 0 until n) {
                        val x = size.width * i / (n - 1)
                        val major = i % 5 == 0
                        val dist = abs(i - n / 2) / (n / 2f)
                        drawLine(
                            Color.White.copy(alpha = 1f - dist * 0.8f),
                            Offset(x, size.height), Offset(x, size.height - if (major) 18f else 9f), 2f,
                        )
                    }
                    drawLine(accent, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 3f)
                }
                Text("◀ DRAG FOCUS RING ▶", style = osd(9, Color.Gray))
            }
            // On-screen shutter
            Box(
                Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
                    .size(64.dp).border(3.dp, Color.White, CircleShape).padding(6.dp)
                    .background(if (locked) Level else Color(0xFFE0E0E0), CircleShape)
                    .clickable(enabled = selected != null) { haptics.thud(); selected?.let(repo::launch) },
            )
        }
    }
}

@Composable
private fun CameraLayer(hist: androidx.compose.runtime.MutableState<FloatArray>) {
    val ctx = LocalContext.current
    val owner = ctx as? LifecycleOwner ?: return
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { c ->
            PreviewView(c).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
        },
        update = { view ->
            val future = ProcessCameraProvider.getInstance(ctx)
            future.addListener({
                val provider = future.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                analysis.setAnalyzer(executor) { image ->
                    val bins = IntArray(64)
                    val buf = image.planes[0].buffer
                    val step = 8 // subsample for zero-frame-drop
                    var n = 0
                    var i = 0
                    val lim = buf.limit()
                    while (i < lim) {
                        bins[(buf.get(i).toInt() and 0xFF) shr 2]++
                        n++; i += step
                    }
                    val mx = (bins.maxOrNull() ?: 1).coerceAtLeast(1)
                    hist.value = FloatArray(64) { bins[it].toFloat() / mx }
                    image.close()
                }
                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                }
            }, ContextCompat.getMainExecutor(ctx))
        },
    )
}
