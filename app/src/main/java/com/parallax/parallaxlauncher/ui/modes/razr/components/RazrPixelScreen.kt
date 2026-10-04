package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * The internal 2.2" TFT panel: 176 x 220 pixels, i.e. a 4:5 portrait window.
 * A faint pixel lattice is drawn on top so text reads as chunky 2005-era LCD
 * glyphs rather than crisp modern type.
 */
@Composable
fun RazrPixelScreen(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    /** Logical pixel grid; 176 x 220 in the real panel. */
    columns: Int = 176,
    rows: Int = 220,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(columns.toFloat() / rows.toFloat())
            .background(palette.lcdOff)
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        Canvas(Modifier.fillMaxSize()) {
            drawLattice(columns, rows, palette.lcdGrid)
            drawInnerShadow()
        }
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
            .background(palette.lcdOff)
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        Canvas(Modifier.fillMaxSize()) {
            drawLattice(96, 80, palette.lcdGrid)
            drawInnerShadow()
        }
    }
}

private fun DrawScope.drawLattice(columns: Int, rows: Int, color: Color) {
    if (columns <= 0 || rows <= 0) return
    val dx = size.width / columns
    val dy = size.height / rows
    val step = maxOf(1, minOf(columns, rows) / 44)
    for (c in 0..columns step step) {
        drawLine(color, Offset(c * dx, 0f), Offset(c * dx, size.height), strokeWidth = 0.5f)
    }
    for (r in 0..rows step step) {
        drawLine(color, Offset(0f, r * dy), Offset(size.width, r * dy), strokeWidth = 0.5f)
    }
}

/** Soft edge falloff so the panel looks recessed behind its bezel. */
private fun DrawScope.drawInnerShadow() {
    val edge = minOf(size.width, size.height) * 0.16f
    val steps = 6
    for (i in steps downTo 1) {
        val a = 0.05f * i
        val inset = edge * (i / steps.toFloat())
        drawRect(
            color = Color.Black.copy(alpha = a / steps * 2.2f),
            topLeft = Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(
                size.width - inset * 2,
                size.height - inset * 2,
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = edge / steps)
        )
    }
}

val RazrBezelCorner: Dp = 4.dp