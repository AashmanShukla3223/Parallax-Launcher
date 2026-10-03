package com.parallax.parallaxlauncher.ui.modes.industrial.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Vertical lever toggle; up = armed. */
@Composable
fun SafetyArmToggle(
    armed: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pos by animateFloatAsState(
        if (armed) 1f else 0f,
        spring(dampingRatio = 0.45f, stiffness = 900f),
        label = "lever",
    )
    val slot = 64.dp
    val knob = 30.dp
    Box(
        modifier
            .width(48.dp)
            .height(slot + 24.dp)
            .background(Color(0xFF15161A), RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF2A2B2F), RoundedCornerShape(8.dp))
            .clickable { onToggle(!armed) },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(8.dp).height(slot).background(Color.Black, RoundedCornerShape(4.dp)))
        Box(
            Modifier
                .size(knob)
                .offset(y = (slot - knob) / 2 * (1f - 2f * pos))
                .background(
                    if (armed) Color(0xFFFF3B30) else Color(0xFF8A8D94),
                    RoundedCornerShape(50),
                )
                .border(2.dp, Color(0xFF0A0A0B), RoundedCornerShape(50)),
        )
        Text(
            if (armed) "ARM" else "SAFE",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9500),
            ),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
