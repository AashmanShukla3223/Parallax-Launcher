package com.parallax.parallaxlauncher.ui.onboarding

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF121314)
private val Amber = Color(0xFFFF9500)
private val AmberDim = Color(0x99FF9500)
private val Steel = Color(0xFF2A2B2F)

private data class Step(val tag: String, val title: String, val body: String)

private val steps = listOf(
    Step("01 / 03", "PARALLAX", "Five instrument panels for your home screen. Industrial Rig is online. More modes unlock soon."),
    Step("02 / 03", "TACTILE BY DESIGN", "Spin the dial to select an app. Flip the arm switch. Press LAUNCH. Every action has a detent and a thud."),
    Step("03 / 03", "ACTIVATE", "Set Parallax as your default home app. You can revert anytime in Settings > Apps > Default apps > Home app."),
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
    onFinish: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val last = step == steps.lastIndex

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
        Spacer(Modifier.height(32.dp))

        Crossfade(step, Modifier.weight(1f), label = "step") { s ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(steps[s].tag, style = mono(12, AmberDim))
                Text(steps[s].title, style = mono(32, weight = FontWeight.Bold, ls = 3))
                Text(steps[s].body, style = mono(14, Color(0xFFE5E5EA), ls = 1))
                if (s == steps.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isDefaultHome) "STATUS: ACTIVE" else "STATUS: STANDBY",
                        style = mono(14, if (isDefaultHome) Color(0xFF34C759) else Color(0xFFFF3B30)),
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            steps.indices.forEach {
                Box(
                    Modifier
                        .size(width = if (it == step) 28.dp else 12.dp, height = 4.dp)
                        .background(if (it == step) Amber else Steel),
                )
            }
        }

        val shape = RoundedCornerShape(8.dp)
        if (last && !isDefaultHome) {
            KeyButton("SET AS DEFAULT HOME", filled = true, onClick = onSetDefault)
            Spacer(Modifier.height(12.dp))
            KeyButton("SKIP FOR NOW", filled = false, onClick = onFinish)
        } else {
            KeyButton(
                if (last) "ENTER" else "NEXT",
                filled = true,
                onClick = { if (last) onFinish() else step++ },
            )
            if (!last) {
                Spacer(Modifier.height(12.dp))
                KeyButton("SKIP", filled = false, onClick = onFinish)
            }
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
