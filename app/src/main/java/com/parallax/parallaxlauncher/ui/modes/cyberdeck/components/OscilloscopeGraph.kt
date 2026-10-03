package com.parallax.parallaxlauncher.ui.modes.cyberdeck.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette

val Alert = Color(0xFFFF003C)

@Composable
fun term(size: Int, color: Color? = null): TextStyle {
    val palette = LocalParallaxPalette.current
    return TextStyle(
        fontFamily = palette.font,
        fontSize = size.sp,
        color = color ?: palette.accent,
        letterSpacing = 1.sp,
    )
}

/** Rolling oscilloscope: [samples] oldest-first, scaled against [max]. */
@Composable
fun OscilloscopeGraph(
    label: String,
    value: String,
    samples: List<Float>,
    max: Float,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    color: Color? = null,
) {
    val palette = LocalParallaxPalette.current
    val traceColor = color ?: palette.accent
    val dimColor = palette.accentDim

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text("[$label]", style = term(11, dimColor), modifier = Modifier.weight(1f))
            Text(value, style = term(11, traceColor))
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 2.dp)
                .border(1.dp, dimColor.copy(alpha = 0.5f)),
        ) {
            // Graticule
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(dimColor.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), 1f)
            }
            for (i in 1..9) {
                val x = size.width * i / 10f
                drawLine(dimColor.copy(alpha = 0.15f), Offset(x, 0f), Offset(x, size.height), 1f)
            }
            if (samples.size < 2) return@Canvas
            val step = size.width / (samples.size - 1).coerceAtLeast(1)
            val path = Path()
            val safeMax = if (max <= 0f) 1f else max
            for (i in samples.indices) {
                val norm = (samples[i] / safeMax).coerceIn(0f, 1f)
                val x = i * step
                val y = size.height - (norm * (size.height - 4f)) - 2f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, traceColor, style = Stroke(width = 2.dp.toPx()))
        }
    }
}
