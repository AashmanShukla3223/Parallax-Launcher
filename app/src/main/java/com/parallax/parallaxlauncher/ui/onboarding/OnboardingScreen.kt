package com.parallax.parallaxlauncher.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

private val Bg = Color(0xFF121314)
private val Amber = Color(0xFFFF9500)
private val AmberDim = Color(0x99FF9500)
private val Steel = Color(0xFF2A2B2F)
private val Green = Color(0xFF34C759)

private data class Step(val tag: String, val title: String, val body: String)

private val steps = listOf(
    Step("01 / 05", "PARALLAX", "Seven instrument panels for your home screen. Industrial Rig is online. Tap the MODE chip to switch between all seven."),
    Step("02 / 05", "TACTILE BY DESIGN", "Spin the dial to select an app. Flip the arm switch. Press LAUNCH. Every action has a detent and a thud."),
    Step("03 / 05", "ACTIVATE", "Hand your home screen and your dialler to Parallax. Both are optional and either can be reverted in Android settings."),
)

/** Short labels for the onboarding mode picker. Kept terse so seven fit on one row. */
private val modeLabels = listOf(
    "INDUSTRIAL", "CELESTIAL", "SWISS", "CYBERDECK", "CINECAM", "ROTARY", "RAZR V3i",
)

private fun mono(size: Int, color: Color = Amber, weight: FontWeight = FontWeight.Normal, ls: Int = 2) =
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = size.sp,
        color = color,
        fontWeight = weight,
        letterSpacing = ls.sp,
    )

@Composable
fun OnboardingScreen(
    isDefaultHome: Boolean,
    onSetDefault: () -> Unit,
    homeLabel: String,
    isDefaultDialer: Boolean,
    onSetDialer: () -> Unit,
    dialerLabel: String,
    mode: Int,
    onMode: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val last = steps.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
    ) {
        // Hazard-stripe header
        Canvas(Modifier.fillMaxWidth().height(10.dp)) {
            val w = 14.dp.toPx()
            var x = -size.height
            var on = true
            while (x < size.width) {
                drawLine(
                    if (on) Amber else Color(0xFF1A1B1E),
                    Offset(x, size.height), Offset(x + size.height, 0f), w / 2f,
                )
                x += w / 2f
                on = !on
            }
        }
        Spacer(Modifier.height(24.dp))

        Crossfade(step, Modifier.weight(1f), label = "step") { s ->
            when (s) {
                in steps.indices -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(steps[s].tag, style = mono(12, AmberDim))
                    Text(steps[s].title, style = mono(32, weight = FontWeight.Bold, ls = 3))
                    Text(steps[s].body, style = mono(14, Color(0xFFE5E5EA), ls = 1))
                    if (s == last) {
                        Spacer(Modifier.height(8.dp))
                        RoleRow(
                            label = "HOME",
                            current = homeLabel,
                            active = isDefaultHome,
                            onClick = onSetDefault,
                        )
                        Spacer(Modifier.height(10.dp))
                        RoleRow(
                            label = "DIALER",
                            current = dialerLabel,
                            active = isDefaultDialer,
                            onClick = onSetDialer,
                        )
                    }
                }

                // ---- Congrats ------------------------------------------------
                3 -> Box(Modifier.fillMaxSize()) {
                    ConfettiBackdrop(Modifier.fillMaxSize())
                    Balloons(Modifier.fillMaxSize())
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("04 / 05", style = mono(12, AmberDim))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "CONGRATS!",
                            style = mono(40, Green, FontWeight.Bold, 4),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You are ready to go.",
                            style = mono(15, Color(0xFFE5E5EA), ls = 1),
                        )
                        Spacer(Modifier.height(28.dp))
                        Text("PICK YOUR PANEL", style = mono(11, AmberDim))
                        Spacer(Modifier.height(12.dp))
                        ModeSelector(mode = mode, onMode = onMode)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "You can switch any time with the MODE chip.",
                            style = mono(11, Color(0xFF8E8E93), ls = 1),
                        )
                    }
                }

                // ---- Enjoy ----------------------------------------------------
                else -> Box(Modifier.fillMaxSize()) {
                    Balloons(Modifier.fillMaxSize(), count = 3, speed = 0.6f)
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("05 / 05", style = mono(12, AmberDim))
                        Spacer(Modifier.height(10.dp))
                        Text("ENJOY!", style = mono(46, Amber, FontWeight.Bold, 6))
                        Spacer(Modifier.height(14.dp))
                        Text(
                            modeLabels[mode - 1],
                            style = mono(14, AmberDim, ls = 3),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Your first panel is ${modeLabels[mode - 1]}.",
                            style = mono(12, Color(0xFFE5E5EA), ls = 1),
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            (0..last + 2).forEach {
                Box(
                    Modifier
                        .size(width = if (it == step) 28.dp else 12.dp, height = 4.dp)
                        .background(if (it == step) Amber else Steel),
                )
            }
        }

        when {
            // Roles step: each role gets its own button, and either can be skipped.
            step == last -> {
                if (!isDefaultHome) {
                    KeyButton("SET AS HOME APP", filled = true, onClick = onSetDefault)
                    Spacer(Modifier.height(10.dp))
                }
                if (!isDefaultDialer) {
                    KeyButton("SET AS DIALER", filled = !isDefaultHome, onClick = onSetDialer)
                    Spacer(Modifier.height(10.dp))
                }
                KeyButton("CONTINUE", filled = false, onClick = { step++ })
            }
            step == last + 2 -> KeyButton("ENJOY!", filled = true, onClick = onFinish)
            else -> {
                KeyButton("NEXT", filled = true, onClick = { step++ })
                if (step == 0) {
                    Spacer(Modifier.height(12.dp))
                    KeyButton("SKIP", filled = false, onClick = onFinish)
                }
            }
        }
    }
}

/**
 * One role, its current holder, and a button to change it.
 *
 * Shows the real app label rather than a bare ACTIVE/STANDBY, because "your
 * dialler is Phone" is the thing worth confirming before handing it over --
 * and when the user is *not* Parallax, naming what currently holds the role is
 * the only way they can tell whether the change took effect.
 */
@Composable
private fun RoleRow(
    label: String,
    current: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, if (active) Green else Steel, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = mono(11, AmberDim, ls = 3))
            Spacer(Modifier.height(3.dp))
            Text(
                if (active) "$current (PARALLAX)" else current,
                style = mono(13, if (active) Green else Color(0xFFE5E5EA), ls = 0),
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .background(if (active) Green.copy(alpha = 0.18f) else Color.Transparent, shape)
                .border(1.dp, if (active) Green else Amber, shape)
                .clickable(enabled = !active, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                if (active) "SET" else "CHANGE",
                style = mono(11, if (active) Green else Amber, FontWeight.Bold, 2),
            )
        }
    }
}

/** Seven tappable chips. Tapping one sets the panel the app opens on. */
@Composable
private fun ModeSelector(mode: Int, onMode: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modeLabels.chunked(3).forEachIndexed { rowIndex, row ->
            val start = rowIndex * 3
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                row.forEachIndexed { i, name ->
                    val m = start + i + 1
                    val selected = m == mode
                    val shape = RoundedCornerShape(6.dp)
                    Box(
                        Modifier
                            .background(if (selected) Amber else Color.Transparent, shape)
                            .border(
                                1.dp,
                                if (selected) Amber else Steel,
                                shape,
                            )
                            .clickable { onMode(m) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                    ) {
                        Text(
                            name,
                            style = mono(
                                10,
                                if (selected) Color(0xFF0A0A0B) else AmberDim,
                                if (selected) FontWeight.Bold else FontWeight.Normal,
                                1,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

// ---- Celebration -------------------------------------------------------------

/**
 * Confetti, drawn on Canvas.
 *
 * No image assets: the house rule for this project is that marks are drawn, not
 * pasted in, and an asset pack of confetti would not survive the release APK's
 * shrinker with the same grace. Each piece gets a deterministic pseudo-random
 * phase so the field is fixed per particle and never re-randomised per frame.
 */
@Composable
private fun ConfettiBackdrop(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "confetti")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confettiPhase",
    )
    val colors = listOf(Amber, Color(0xFF34C759), Color(0xFF3A5BFF), Color(0xFFE8453A), Color(0xFFE5E5EA))

    Canvas(modifier) {
        val n = 46
        repeat(n) { i ->
            // Cheap LCG: stable per index, so pieces fall consistently.
            val r1 = ((i * 1103515245 + 12345) % 100000) / 100000f
            val r2 = ((i * 69069 + 1) % 100000) / 100000f
            val r3 = ((i * 22695477 + 7) % 100000) / 100000f
            val speed = 0.55f + r3 * 0.75f
            val y = ((phase * speed + r1) % 1f) * (size.height + 60f) - 30f
            val x = r2 * size.width + sin(((phase * 2.2f) + r1 * 6f) * PI.toFloat()) * 22f
            val w = 5f + r3 * 4f
            val h = 9f + r1 * 7f
            // Flutter each piece so the fall does not read as a grid.
            rotate(degrees = (phase * 420f * (0.4f + r2)) % 360f, pivot = Offset(x, y)) {
                drawRect(
                    color = colors[i % colors.size],
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                )
            }
        }
    }
}

/**
 * Balloons rising and swaying.
 *
 * Also Canvas, for the same reason as the confetti. Strings are drawn so they
 * read as balloons rather than circles, which is the entire difference between
 * a celebration and a loading spinner.
 */
@Composable
private fun Balloons(modifier: Modifier = Modifier, count: Int = 5, speed: Float = 1f) {
    val t = rememberInfiniteTransition(label = "balloons")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "balloonPhase",
    )
    val colors = listOf(
        Color(0xFFFF9500), Color(0xFF3A5BFF), Color(0xFF34C759),
        Color(0xFFE8453A), Color(0xFF7D5FFF),
    )

    Canvas(modifier) {
        val step = size.width / (count + 1)
        repeat(count) { i ->
            val r1 = ((i * 7919 + 3) % 100000) / 100000f
            val sp = speed * (0.7f + r1 * 0.6f)
            val travel = size.height + 260f
            val y = size.height + 120f - ((phase * sp + r1) % 1f) * travel
            val sway = sin(((phase * 2f) + i * 1.3f) * PI.toFloat()) * 16f
            val cx = step * (i + 1) + sway
            val rad = size.minDimension * 0.055f
            val c = colors[i % colors.size]

            // String first, so the knot sits over it.
            drawLine(
                color = c.copy(alpha = 0.75f),
                start = Offset(cx, y + rad),
                end = Offset(cx + sway * 0.6f, y + rad + rad * 2.6f),
                strokeWidth = 2.2f,
            )
            // Body plus the little triangle knot underneath.
            drawOval(
                color = c,
                topLeft = Offset(cx - rad * 0.82f, y - rad),
                size = Size(rad * 1.64f, rad * 2.05f),
            )
            drawPath(
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - rad * 0.16f, y + rad * 0.95f)
                    lineTo(cx + rad * 0.16f, y + rad * 0.95f)
                    lineTo(cx, y + rad * 1.35f)
                    close()
                },
                c,
            )
            // Highlight, so it reads as a sphere and not a flat disc.
            drawOval(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(cx - rad * 0.52f, y - rad * 0.66f),
                size = Size(rad * 0.44f, rad * 0.56f),
            )
        }
    }
}

@Composable
private fun KeyButton(label: String, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (filled) Amber else Color.Transparent, shape)
            .border(2.dp, if (filled) Color(0xFF0A0A0B) else Steel, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = mono(16, if (filled) Color(0xFF0A0A0B) else AmberDim, FontWeight.Bold, 3),
        )
    }
}