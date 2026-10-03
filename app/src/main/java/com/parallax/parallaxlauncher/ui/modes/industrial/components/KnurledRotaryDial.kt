package com.parallax.parallaxlauncher.ui.modes.industrial.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Knurled dial. Rotation (degrees, unbounded) maps to app index in steps of [DETENT_DEG].
 * [onIndexChange] fires on every detent crossing (use for haptic tick + selection).
 */
@Composable
fun KnurledRotaryDial(
    itemCount: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    detentDeg: Float = 15f,
) {
    val palette = LocalParallaxPalette.current
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val count = rememberUpdatedState(itemCount)
    val cb = rememberUpdatedState(onIndexChange)
    val DETENT_DEG = detentDeg

    LaunchedEffect(Unit) {
        snapshotFlow { (rotation.value / DETENT_DEG).roundToInt() }
            .collect { step ->
                val n = count.value
                if (n > 0) cb.value(((step % n) + n) % n)
            }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    scope.launch { rotation.stop() }
                    val c = Offset(size.width / 2f, size.height / 2f)
                    fun ang(p: Offset) =
                        Math.toDegrees(atan2(p.y - c.y, p.x - c.x).toDouble()).toFloat()

                    var prev = ang(down.position)
                    var vel = 0f // deg/s, low-passed
                    var lastT = down.uptimeMillis
                    while (true) {
                        val ev = awaitPointerEvent()
                        val ch = ev.changes.firstOrNull() ?: break
                        if (!ch.pressed) break
                        val a = ang(ch.position)
                        var d = a - prev
                        if (d > 180f) d -= 360f else if (d < -180f) d += 360f
                        prev = a
                        val dt = (ch.uptimeMillis - lastT).coerceAtLeast(1L)
                        lastT = ch.uptimeMillis
                        vel = vel * 0.6f + (d * 1000f / dt) * 0.4f
                        scope.launch { rotation.snapTo(rotation.value + d) }
                        ch.consume()
                    }
                    scope.launch {
                        rotation.animateDecay(vel, exponentialDecay(frictionMultiplier = 2.2f))
                        val target = (rotation.value / DETENT_DEG).roundToInt() * DETENT_DEG
                        rotation.animateTo(target, spring(dampingRatio = 0.55f, stiffness = 500f))
                    }
                }
            },
    ) {
        val r = size.minDimension / 2f
        val center = Offset(r, r)
        // Body
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFF3A3D42), Color(0xFF15161A)), center, r),
            r, center,
        )
        drawCircle(Color(0xFF0A0A0B), r, center, style = Stroke(4.dp.toPx()))
        // Fixed index mark
        drawLine(palette.accent, Offset(r, 0f), Offset(r, 14.dp.toPx()), 3.dp.toPx())
        // Knurling rotates with value
        rotate(rotation.value, center) {
            val teeth = 72
            for (i in 0 until teeth) {
                val a = 2 * PI * i / teeth
                val long = i % 6 == 0
                val r0 = r * (if (long) 0.80f else 0.88f)
                val r1 = r * 0.95f
                drawLine(
                    if (long) palette.accent else Color(0xFF5A5E66),
                    Offset(center.x + (r0 * cos(a)).toFloat(), center.y + (r0 * sin(a)).toFloat()),
                    Offset(center.x + (r1 * cos(a)).toFloat(), center.y + (r1 * sin(a)).toFloat()),
                    if (long) 3.dp.toPx() else 1.5f.dp.toPx(),
                )
            }
            drawCircle(Color(0xFF1E2024), r * 0.55f, center)
            drawLine(palette.accent, center, Offset(center.x, center.y - r * 0.5f), 3.dp.toPx())
        }
    }
}
