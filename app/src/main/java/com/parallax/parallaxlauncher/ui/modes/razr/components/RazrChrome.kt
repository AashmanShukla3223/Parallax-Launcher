package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

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
            .height(11.dp)
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
            .height(15.dp)
            .background(Brush.verticalGradient(listOf(palette.titleTop, palette.titleLow))),
        contentAlignment = Alignment.Center,
    ) {
        Text2(
            text = title,
            color = palette.titleInk,
            size = 11.sp,
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
            .height(15.dp)
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
            size = 10.sp,
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

/** The 96 x 80 external CSTN cover display. */
@Composable
fun RazrCoverScreen(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(96f / 80f)
            .background(palette.field)
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        Canvas(Modifier.fillMaxSize()) { drawSubpixels() }
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