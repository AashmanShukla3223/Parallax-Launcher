package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * The status-strip battery indicator.
 *
 * The stock V3i indicator is a capsule divided by tick marks into quarters,
 * filled left to right, with the terminal nub on the right. It is drawn on a
 * Canvas rather than composed from bars because the fill has to be continuous
 * across the whole 1-100% range: a segment count cannot express "6%", and a
 * 1% charge still has to show a visible sliver or the icon reads as empty.
 *
 * Level handling, matching the manual's battery indicator table:
 *   100%      full, white
 *   ~50%      half, white
 *   <= 20%    turns red, as the stock UI does to prompt a charge
 *   <= 5%     red and pulsing is *not* attempted here; the red plus the
 *             near-empty fill is the whole signal at this size
 *   charging  green with the bolt, regardless of level
 *
 * @param percent charge level 0..100; values outside are clamped.
 */
@Composable
fun RazrBatteryGlyph(
    percent: Int,
    charging: Boolean,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    val level = percent.coerceIn(0, 100)
    // Red only when genuinely low, and never while charging -- a green bolt
    // over a red bar is the classic indicator bug.
    val low = !charging && level <= 20
    val fill = when {
        charging -> Color(0xFF7DFF9E)
        low -> Color(0xFFFF4B3E)
        else -> ink
    }
    val empty = ink.copy(alpha = 0.26f)

    Canvas(modifier.width(9.dp).height(5.dp)) {
        val nubW = size.width * 0.13f
        val bodyW = size.width - nubW
        val r = size.height * 0.28f
        val stroke = size.height * 0.15f

        // Terminal nub.
        drawRoundRect(
            color = if (level > 0) ink else empty,
            topLeft = Offset(bodyW, size.height * 0.30f),
            size = Size(nubW, size.height * 0.40f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height * 0.12f),
        )

        // Capsule body.
        drawRoundRect(
            color = ink,
            topLeft = Offset(stroke * 0.5f, stroke * 0.5f),
            size = Size(bodyW - stroke, size.height - stroke),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r),
            style = Stroke(width = stroke),
        )

        // Continuous fill across the full 1-100% range, inset inside the
        // outline so it never overlaps the stroke.
        val inset = stroke + size.height * 0.10f
        val innerW = bodyW - inset * 2f
        val innerH = size.height - inset * 2f
        val frac = (level.coerceIn(1, 100)) / 100f
        if (level > 0 && innerW > 0f && innerH > 0f) {
            drawRoundRect(
                color = fill,
                topLeft = Offset(inset, inset),
                size = Size(innerW * frac, innerH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(innerH * 0.30f),
            )
        }

        // Quarter ticks, matching the stock indicator.
        val tickX = { t: Float -> inset + innerW * t }
        listOf(0.25f, 0.5f, 0.75f).forEach { t ->
            drawLine(
                color = if (innerW * frac > innerW * t) empty else ink.copy(alpha = 0.45f),
                start = Offset(tickX(t), inset),
                end = Offset(tickX(t), inset + innerH),
                strokeWidth = size.height * 0.07f,
                cap = StrokeCap.Butt,
            )
        }

        // Charging bolt, centred over the fill.
        if (charging) {
            val cx = size.width * 0.46f
            val cy = size.height * 0.5f
            val h = size.height * 0.40f
            val w = size.height * 0.22f
            val bolt = Path().apply {
                moveTo(cx + w * 0.35f, cy - h)
                lineTo(cx - w * 0.55f, cy + h * 0.10f)
                lineTo(cx - w * 0.05f, cy + h * 0.10f)
                lineTo(cx - w * 0.35f, cy + h)
                lineTo(cx + w * 0.55f, cy - h * 0.10f)
                lineTo(cx + w * 0.05f, cy - h * 0.10f)
                close()
            }
            drawPath(bolt, Color(0xFF06301A))
        }
    }
}

/**
 * The three chrome bars that frame every stock V3i screen.
 *
 * Modelled directly on photographs of the real handset: a very thin navy status
 * strip with white glyphs, a pale blue-grey gradient title bar with bold black
 * centred text, and a pale soft-key bar with bold black labels and the menu
 * indicator in the middle.
 */
@Composable
fun RazrStatusStrip(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(15.dp)
            .background(Brush.verticalGradient(listOf(palette.statusTop, palette.statusLow)))
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart,
    ) { content() }
}

@Composable
fun RazrTitleBar(
    palette: RazrPalette,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(Brush.verticalGradient(listOf(palette.titleTop, palette.titleLow))),
        contentAlignment = Alignment.Center,
    ) {
        Text2(
            text = title,
            color = palette.titleInk,
            size = 15.9.sp,
            weight = FontWeight.Bold,
        )
    }
}

@Composable
fun RazrSoftKeyBar(
    palette: RazrPalette,
    left: String,
    center: String,
    right: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(Brush.verticalGradient(listOf(palette.softTop, palette.softLow)))
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftLabel(palette.softInk, left, Modifier.width(52.dp), Alignment.CenterStart)
        // The stock menu indicator sits dead centre in every soft-key bar.
        SoftLabel(palette.softInk, center.ifBlank { "☰" }, Modifier.weight(1f), Alignment.Center)
        SoftLabel(palette.softInk, right, Modifier.width(52.dp), Alignment.CenterEnd)
    }
}

@Composable
private fun SoftLabel(color: Color, text: String, modifier: Modifier, align: Alignment) {
    Box(modifier, contentAlignment = align) {
        Text2(
            text = text,
            color = color,
            size = 14.5.sp,
            weight = FontWeight.Bold,
            align = align,
        )
    }
}

/**
 * The internal 2.2" TFT: 176 x 220, so a true 4:5 portrait window.
 *
 * The stock panel is a colour TFT, not a monochrome LCD, so instead of a dark
 * green field it gets a near-white backlight and a fine blue subpixel stripe.
 */
@Composable
fun RazrPixelScreen(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    columns: Int = 176,
    rows: Int = 220,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(columns.toFloat() / rows.toFloat())
            .background(palette.field)
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        Canvas(Modifier.fillMaxSize()) { drawSubpixels() }
    }
}

/**
 * The 96 x 80 external CSTN cover display.
 *
 * Sized explicitly rather than with `aspectRatio`, which does not resolve
 * reliably inside a wrap-content parent and was rendering this panel portrait.
 * Landscape 96:80 means height = width * 80/96.
 */
@Composable
fun RazrCoverScreen(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val height = maxWidth * (80f / 96f)
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(palette.field)
        ) {
            Box(Modifier.fillMaxSize()) { content() }
            Canvas(Modifier.fillMaxSize()) { drawSubpixels() }
        }
    }
}

/**
 * Fine RGB subpixel stripe. Visible in close-up photographs of the panel, and
 * the thing that stops the screen reading as flat vector output.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSubpixels() {
    val stripe = size.width / 176f / 3f
    if (stripe < 0.4f) return
    var x = 0f
    while (x < size.width) {
        drawRect(Color(0xFF2A44FF).copy(alpha = 0.055f), Offset(x, 0f), Size(stripe, size.height))
        drawRect(Color(0xFF00B04A).copy(alpha = 0.035f), Offset(x + stripe, 0f), Size(stripe, size.height))
        drawRect(Color(0xFFFF2A44).copy(alpha = 0.045f), Offset(x + stripe * 2, 0f), Size(stripe, size.height))
        x += stripe * 3f
    }
}

/** Shared bold-sans text helper for the display chrome. */
@Composable
internal fun Text2(
    text: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    align: Alignment = Alignment.CenterStart,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Text(
        text = text,
        color = color,
        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
        fontSize = size,
        fontWeight = weight,
        letterSpacing = (-0.1).sp,
        maxLines = maxLines,
        modifier = modifier,
        textAlign = when (align) {
            Alignment.CenterEnd -> androidx.compose.ui.text.style.TextAlign.End
            Alignment.Center -> androidx.compose.ui.text.style.TextAlign.Center
            else -> androidx.compose.ui.text.style.TextAlign.Start
        },
    )
}