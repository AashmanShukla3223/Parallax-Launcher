package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * The upper clamshell: earpiece slot, Motorola batwing medallion, MOTOROLA
 * wordmark, then the 2.2" panel recessed inside a black bezel.
 */
@Composable
fun RazrUpperShell(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
            .background(
                Brush.verticalGradient(
                    listOf(palette.chassisTop, palette.chassisMid, palette.chassisLow)
                )
            )
            .border(
                1.5.dp,
                palette.chassisEdge,
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Earpiece slot.
        Box(
            Modifier
                .width(38.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.chassisEdge)
        )
        Spacer(Modifier.height(3.dp))
        // Medallion + wordmark.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Medallion(palette, 11.dp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "MOTOROLA",
                color = palette.chassisHighlight,
                fontFamily = FontFamily.SansSerif,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
        Spacer(Modifier.height(4.dp))
        // Panel recess.
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.bezel)
                .border(1.dp, palette.bezelEdge, RoundedCornerShape(3.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/** Circular Motorola "M" medallion, drawn as vector art. */
@Composable
fun Medallion(palette: RazrPalette, size: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2f
        drawCircle(color = palette.chassisHighlight, radius = r)
        drawCircle(color = palette.chassisEdge, radius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.14f))
        // Stylised batwing M.
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val s = r * 0.58f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - s, cy + s * 0.7f)
            lineTo(cx - s, cy - s * 0.15f)
            quadraticBezierTo(cx - s * 0.5f, cy + s * 0.35f, cx, cy - s * 0.55f)
            quadraticBezierTo(cx + s * 0.5f, cy + s * 0.35f, cx + s, cy - s * 0.15f)
            lineTo(cx + s, cy + s * 0.7f)
            quadraticBezierTo(cx + s * 0.45f, cy - s * 0.1f, cx, cy + s * 0.15f)
            quadraticBezierTo(cx - s * 0.45f, cy - s * 0.1f, cx - s, cy + s * 0.7f)
            close()
        }
        drawPath(path, palette.chassisEdge)
    }
}

/**
 * The hinge barrel between the two halves, with the flip seam highlight.
 * Tapping it opens or closes the clamshell.
 */
@Composable
fun RazrHinge(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    open: Boolean,
    onToggle: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        palette.chassisLow,
                        palette.chassisMid,
                        palette.chassisHighlight,
                        palette.chassisMid,
                        palette.chassisLow,
                    )
                )
            )
            .border(1.dp, palette.chassisEdge, RoundedCornerShape(3.dp))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (open) "▼ CLOSE FLIP" else "▲ OPEN FLIP",
            color = palette.chassisHighlight.copy(alpha = 0.85f),
            fontSize = 5.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

/**
 * The lower clamshell when closed: the outer face with the camera lens,
 * the cover display, and the bottom speaker grille.
 */
@Composable
fun RazrCoverShell(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(palette.chassisTop, palette.chassisMid, palette.chassisLow)
                )
            )
            .border(
                1.5.dp,
                palette.chassisEdge,
                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Camera lens + mirror window.
        Box(
            Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(palette.bezel)
                .border(1.dp, palette.chassisEdge, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(13.dp)) {
                drawCircle(color = Color(0xFF1B2A44), radius = size.minDimension / 2f)
                drawCircle(color = Color(0xFF3A5C8C), radius = size.minDimension / 4.4f)
            }
        }
        Spacer(Modifier.height(6.dp))

        // Outer display. Expands to fill the available height so the wallpaper,
        // clock, notifications and messages all fit while the flip is closed.
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.bezel)
                .border(1.dp, palette.bezelEdge, RoundedCornerShape(3.dp))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) { content() }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "MOTOROLA",
            color = palette.chassisHighlight,
            fontSize = 6.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(6.dp))
        SpeakerGrille(palette)
    }
}

/** Bottom chin speaker slots. */
@Composable
fun SpeakerGrille(palette: RazrPalette, modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(9.dp)
    ) {
        val slots = 9
        val gap = size.width / (slots * 2f - 1f)
        val w = gap * 0.7f
        for (i in 0 until slots) {
            val x = i * gap * 2f + gap * 0.2f
            drawRoundRect(
                color = palette.chassisEdge,
                topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                size = androidx.compose.ui.geometry.Size(w, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2f),
            )
        }
    }
}

/** Small persistent banner used for call summaries and ring-style changes. */
@Composable
fun RazrToast(
    palette: RazrPalette,
    text: String,
    modifier: Modifier = Modifier,
    tone: Color? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background((tone ?: palette.ink).copy(alpha = 0.9f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = palette.lcdBacklight,
            fontFamily = FontFamily.Monospace,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
        )
    }
}

/** Screen-lock overlay drawn over whichever view is active. */
@Composable
fun RazrDim(palette: RazrPalette, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
}