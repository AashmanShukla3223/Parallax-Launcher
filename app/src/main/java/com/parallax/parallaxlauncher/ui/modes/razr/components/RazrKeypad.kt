package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.ui.modes.razr.RazrKey
import com.parallax.parallaxlauncher.ui.modes.razr.RazrKeypadLayout
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

private val GreenCall = Color(0xFF3FD35F)
private val RedPower = Color(0xFFE8453A)
private val BracketBlue = Color(0xFF3A5BFF)

/**
 * The V3i lower clamshell.
 *
 * The key insight from the reference photograph is that the keypad is **not** a
 * grid of separate key tiles. It is one continuous sheet of brushed metal with
 * the electric-blue laser etch cut into it. There are no vertical walls between
 * the columns at all — a single groove runs left to right across each row
 * boundary, stepping down into a narrow centre channel before returning to the
 * outer ones. That narrow centre channel is what gives the V3i deck its waisted
 * silhouette, and the step is what makes it read as one sheet rather than twelve
 * buttons.
 *
 * Legends mirror at the centre column exactly as on the handset: the letter
 * group sits to the *right* of 2, 5 and 8 but to the *left* of 3, 6 and 9, with
 * letters small and raised while digits are large.
 */
@Composable
fun RazrKeypad(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    haptics: HapticEngine,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    onSoftLeft: () -> Unit,
    onSoftRight: () -> Unit,
    onCall: () -> Unit,
    onEnd: () -> Unit,
    onGlobe: () -> Unit,
    onEnvelope: () -> Unit,
    onVoice: () -> Unit,
    onKey: (Char) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val w = maxWidth
        val h = maxHeight

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to palette.deckTop,
                        0.45f to palette.deckMid,
                        1f to palette.deckLow,
                    )
                )
        ) {
            // Etch network, rocker and dedicated-key glyphs.
            Canvas(Modifier.fillMaxSize()) {
                drawEtch(palette)
                drawDpad(palette)
                drawPeripheralGlyphs(palette)
            }

            // ---- Legends -------------------------------------------------------
            // Digit sits low in its cell; the letter group is small and raised.
            RazrKeypadLayout.rows.forEachIndexed { rowIndex, row ->
                val cellY = Channels.rowTop(rowIndex)
                row.forEachIndexed { colIndex, key ->
                    val cellX = Channels.start(colIndex)
                    val cellW = Channels.width(colIndex)
                    KeyLegend(
                        key = key,
                        tint = palette.legend,
                        dim = palette.legendDim,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * cellX, y = h * cellY)
                            .size(w * cellW, h * Channels.ROW_HEIGHT),
                    )
                }
            }


            // ---- Hit targets ---------------------------------------------------
            // Everything lives in one overlay drawn last, and the rectangles form a
            // strict partition of the deck so no key can shadow another.
            Box(Modifier.fillMaxSize()) {
                // D-pad block, x 0.22..0.78 / y 0.10..0.44
                hit(w, h, 0.220f, 0.100f, 0.560f, 0.110f) { haptics.click(); onUp() }
                hit(w, h, 0.220f, 0.330f, 0.560f, 0.110f) { haptics.click(); onDown() }
                hit(w, h, 0.220f, 0.210f, 0.160f, 0.120f) { haptics.click(); onLeft() }
                hit(w, h, 0.620f, 0.210f, 0.160f, 0.120f) { haptics.click(); onRight() }
                hit(w, h, 0.380f, 0.210f, 0.240f, 0.120f) { haptics.thud(); onCenter() }

                // Voice key, top centre.
                hit(w, h, 0.420f, 0.015f, 0.160f, 0.080f) { haptics.click(); onVoice() }

                // Left flank: soft key, globe, green send. Right flank mirrored.
                hit(w, h, 0.050f, 0.115f, 0.150f, 0.105f) { haptics.click(); onSoftLeft() }
                hit(w, h, 0.050f, 0.235f, 0.150f, 0.095f) { haptics.click(); onGlobe() }
                hit(w, h, 0.050f, 0.345f, 0.150f, 0.095f) { haptics.thud(); onCall() }

                hit(w, h, 0.800f, 0.115f, 0.150f, 0.105f) { haptics.click(); onSoftRight() }
                hit(w, h, 0.800f, 0.235f, 0.150f, 0.095f) { haptics.click(); onEnvelope() }
                hit(w, h, 0.800f, 0.345f, 0.150f, 0.095f) { haptics.thud(); onEnd() }

                // 12 keys.
                RazrKeypadLayout.rows.forEachIndexed { rowIndex, row ->
                    val top = Channels.rowTop(rowIndex)
                    row.forEachIndexed { colIndex, key ->
                        hit(
                            w, h,
                            Channels.start(colIndex), top,
                            Channels.width(colIndex), Channels.ROW_HEIGHT,
                        ) {
                            haptics.click()
                            onKey(key.main[0])
                        }
                    }
                }
            }

        }
    }
}

// ---- Numeric block geometry --------------------------------------------------

/**
 * Fractional geometry of the three etched channels.
 *
 * Held in one place because the painted groove and the tap targets are derived
 * from the same numbers — when they lived in two lists they drifted apart and
 * the keys stopped lining up with what was drawn.
 *
 * The centre channel is deliberately narrower than the outer two: that pinch,
 * repeated on every separator, is what gives the deck its waisted silhouette.
 */
private object Channels {
    val leftStart = 0.065f
    val leftEnd = 0.345f
    val centreStart = 0.385f
    val centreEnd = 0.615f
    val rightStart = 0.655f
    val rightEnd = 0.935f

    /** Top edge of each key row, plus the closing edge below the last one. */
    val rowTops = floatArrayOf(0.455f, 0.587f, 0.720f, 0.852f, 0.985f)
    const val ROW_HEIGHT = 0.1325f

    /**
     * How far the centre run of each separator sits below the outer runs.
     *
     * On the handset the groove steps *down* into the narrow centre channel and
     * back out again, rather than running straight across. Without this step the
     * sheet reads as a plain grid and the waist disappears.
     */
    const val CENTRE_DIP = 0.018f

    fun start(col: Int): Float = when (col) {
        0 -> leftStart
        1 -> centreStart
        else -> rightStart
    }

    fun width(col: Int): Float = when (col) {
        0 -> leftEnd - leftStart
        1 -> centreEnd - centreStart
        else -> rightEnd - rightStart
    }

    fun rowTop(row: Int): Float = rowTops[row]
}

/**
 * Places a transparent tap target at fractional deck coordinates.
 *
 * Uses explicit fractions rather than pixel offsets so the targets scale with
 * the deck and never drift out of sync with the painted artwork.
 */
@Composable
private fun BoxScope.hit(
    w: Dp,
    h: Dp,
    xFrac: Float,
    yFrac: Float,
    wFrac: Float,
    hFrac: Float,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset(x = w * xFrac, y = h * yFrac)
            .size(w * wFrac, h * hFrac)
            .clickable(onClick = onClick)
    )
}

// ---- Legend ------------------------------------------------------------------

/**
 * One key's legend: a large digit with a small, raised letter group beside it,
 * mirrored to the other side for the right-hand column of each numeric row.
 *
 * Letters sit raised and small against the large digit, exactly as etched on the
 * handset, so both stay legible at 176x220.
 */
@Composable
private fun KeyLegend(
    key: RazrKey,
    tint: Color,
    dim: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val letters = if (key.letters.isNotEmpty()) key.letters else key.sub
        when {
            // Letters to the LEFT of the digit (column 3 and "#").
            key.lettersFirst && letters.isNotEmpty() -> Row(
                Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = letters,
                    color = dim,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 7.dp),
                )
                Text(text = key.main, color = tint, fontSize = 24.sp, maxLines = 1)
            }

            // Letters to the RIGHT of the digit (columns 1 and 2).
            letters.isNotEmpty() -> Row(
                Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = key.main, color = tint, fontSize = 24.sp, maxLines = 1)
                Text(
                    text = letters,
                    color = dim,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }

            else -> Text(
                text = key.main,
                color = tint,
                fontSize = 24.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
            )
        }
    }
}

// ---- Etched groove network ---------------------------------------------------

/**
 * The laser-etch network.
 *
 * Rebuilt twice now. First without the old "keyhole arch", which was a single
 * continuous curve rendering as a stray ring over the 2/5/8 column. Then without
 * the vertical channel walls, which closed every cell into its own rectangle and
 * made the deck read as a grid of key tiles rather than one etched sheet.
 *
 * What remains is what the reference photograph actually shows: a rounded
 * rectangle around the D-pad deck, and in the numeric block a set of
 * serpentine separators that step down into a narrow centre channel. Strokes are
 * deliberately light so the etch reads as cut into the metal, not drawn on it.
 */
private fun DrawScope.drawEtch(palette: RazrPalette) {
    val w = size.width
    val h = size.height
    val g = minOf(w, h) * 0.0072f

    fun etch(path: Path, width: Float = g) {
        // Faint bleed only — a heavy glow was reading as a second outline.
        drawPath(
            path, palette.grooveGlow.copy(alpha = 0.16f),
            style = Stroke(width = width * 2.0f, cap = StrokeCap.Round),
        )
        drawPath(path, palette.groove, style = Stroke(width = width, cap = StrokeCap.Round))
    }

    // --- D-pad deck: a plain rounded rectangle, no arch --------------------
    val deckTop = h * 0.105f
    val deckBottom = h * 0.440f
    val left = w * 0.065f
    val right = w * 0.935f
    val r = h * 0.030f
    etch(
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = left, top = deckTop,
                    right = right, bottom = deckBottom,
                    cornerRadius = CornerRadius(r),
                )
            )
        }
    )

    // Voice-key well at the very top centre.
    etch(
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.450f, top = h * 0.038f,
                    right = w * 0.550f, bottom = h * 0.058f,
                    cornerRadius = CornerRadius(h * 0.010f),
                )
            )
        },
        width = g * 0.7f,
    )

    // --- Numeric block: five serpentine separators, no vertical walls --------
    //
    // This is the correction that matters. Drawing a vertical line down both
    // edges of every channel plus a horizontal line per row closes every cell
    // into its own rectangle, which is what made the deck read as a grid of
    // key tiles. The handset has no vertical walls in the numeric block at all:
    // one continuous groove runs left to right, stepping down into the narrow
    // centre channel and back out again.
    val dip = h * Channels.CENTRE_DIP
    Channels.rowTops.forEach { frac ->
        val y = h * frac
        val yMid = y + dip

        // Control-point overshoot, so each transition is an S rather than a kink.
        fun blend(fromX: Float, toX: Float) = fromX + (toX - fromX) * 0.55f

        val leftX = w * Channels.leftStart
        val leftEndX = w * Channels.leftEnd
        val centreStartX = w * Channels.centreStart
        val centreEndX = w * Channels.centreEnd
        val rightStartX = w * Channels.rightStart
        val rightEndX = w * Channels.rightEnd

        etch(
            Path().apply {
                moveTo(leftX, y)
                lineTo(leftEndX, y)
                // Step down into the narrow centre channel.
                cubicTo(
                    blend(leftEndX, centreStartX), y,
                    blend(centreStartX, leftEndX), yMid,
                    centreStartX, yMid,
                )
                lineTo(centreEndX, yMid)
                // ...and back out to the right-hand channel.
                cubicTo(
                    blend(centreEndX, rightStartX), yMid,
                    blend(rightStartX, centreEndX), y,
                    rightStartX, y,
                )
                lineTo(rightEndX, y)
            }
        )
    }
}

/** Moulded navigation rocker with its raised silver select button. */
private fun DrawScope.drawDpad(palette: RazrPalette) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.275f
    val r = size.width * 0.155f
    val ring = size.width * 0.013f

    // These two must name the same centre as the disc below. Omitting it left
    // them at the canvas centre (h/2) while the rocker sat at 0.275h, which is
    // what painted a stray blue ring over the 2/5/8 column.
    drawCircle(
        color = palette.grooveGlow.copy(alpha = 0.30f),
        radius = r + ring * 1.5f,
        center = Offset(cx, cy),
        style = Stroke(width = ring * 3f),
    )
    drawCircle(
        color = palette.groove,
        radius = r + ring * 0.5f,
        center = Offset(cx, cy),
        style = Stroke(width = ring),
    )
    // Opaque backing disc so the rocker reads at its true size rather than
    // blending into the deck.
    drawCircle(color = palette.dpadRing, radius = r - ring * 0.5f, center = Offset(cx, cy))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.wellFace, palette.deckMid),
            center = Offset(cx - r * 0.25f, cy - r * 0.30f),
        ),
        radius = r - ring * 0.5f,
        center = Offset(cx, cy),
    )
    drawCircle(Color.Black.copy(alpha = 0.35f), r, Offset(cx, cy), style = Stroke(width = ring * 0.8f))

    // Four direction pips.
    val pip = r * 0.16f
    val at = r * 0.70f
    triangle(Offset(cx, cy - at), pip, palette.legend, 0f)
    triangle(Offset(cx, cy + at), pip, palette.legend, 180f)
    triangle(Offset(cx - at, cy), pip, palette.legend, 270f)
    triangle(Offset(cx + at, cy), pip, palette.legend, 90f)

    // Raised silver select button — markedly lighter than the deck.
    val sr = r * 0.38f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF4F6F9), Color(0xFFB4BAC2)),
            center = Offset(cx - sr * 0.3f, cy - sr * 0.35f),
        ),
        radius = sr,
        center = Offset(cx, cy),
    )
    drawCircle(palette.groove, sr, Offset(cx, cy), style = Stroke(width = ring * 0.8f))
}

private fun DrawScope.triangle(centre: Offset, size: Float, color: Color, rotationDeg: Float) {
    rotate(rotationDeg, pivot = centre) {
        val p = Path().apply {
            moveTo(centre.x, centre.y - size)
            lineTo(centre.x + size * 0.92f, centre.y + size * 0.72f)
            lineTo(centre.x - size * 0.92f, centre.y + size * 0.72f)
            close()
        }
        drawPath(p, color)
    }
}

/** Soft-key brackets, globe, envelope and the green/red call keys. */
private fun DrawScope.drawPeripheralGlyphs(palette: RazrPalette) {
    val w = size.width
    val h = size.height
    val s = w * 0.050f
    val leftX = w * 0.135f
    val rightX = w * 0.865f
    val softY = h * 0.205f
    val fnY = h * 0.300f
    val callY = h * 0.378f
    val sw = w * 0.011f

    drawBracket(leftX - s * 0.60f, softY, s, false)
    drawBracket(rightX + s * 0.60f, softY, s, true)

    // Globe.
    drawCircle(palette.groove, s * 0.50f, Offset(leftX, fnY), style = Stroke(width = sw * 1.15f))
    drawLine(palette.groove, Offset(leftX, fnY - s * 0.50f), Offset(leftX, fnY + s * 0.50f), sw * 1.05f)
    drawLine(palette.groove, Offset(leftX - s * 0.50f, fnY), Offset(leftX + s * 0.50f, fnY), sw * 1.05f)
    drawArc(
        palette.groove, 90f, 180f, false,
        topLeft = Offset(leftX - s * 0.25f, fnY - s * 0.50f),
        size = Size(s * 0.50f, s),
        style = Stroke(width = sw),
    )

    // Envelope.
    val envW = s * 1.00f
    val envH = s * 0.74f
    drawRoundRect(
        palette.groove,
        Offset(rightX - envW / 2f, fnY - envH / 2f),
        Size(envW, envH),
        CornerRadius(s * 0.10f),
        style = Stroke(width = sw * 1.1f),
    )
    drawLine(
        palette.groove,
        Offset(rightX - envW / 2f, fnY - envH / 2f),
        Offset(rightX, fnY + envH * 0.16f),
        sw * 1.05f,
    )
    drawLine(
        palette.groove,
        Offset(rightX + envW / 2f, fnY - envH / 2f),
        Offset(rightX, fnY + envH * 0.16f),
        sw * 1.05f,
    )

    // Green send: circular arrow.
    drawArc(
        GreenCall, 40f, 285f, false,
        topLeft = Offset(leftX - s * 0.50f, callY - s * 0.50f),
        size = Size(s, s),
        style = Stroke(width = sw * 1.8f, cap = StrokeCap.Round),
    )
    drawPath(
        Path().apply {
            moveTo(leftX + s * 0.18f, callY - s * 0.60f)
            lineTo(leftX + s * 0.62f, callY - s * 0.30f)
            lineTo(leftX + s * 0.10f, callY - s * 0.14f)
            close()
        },
        GreenCall,
    )

    // Red power.
    drawArc(
        RedPower, -60f, 300f, false,
        topLeft = Offset(rightX - s * 0.48f, callY - s * 0.48f),
        size = Size(s * 0.96f, s * 0.96f),
        style = Stroke(width = sw * 1.7f, cap = StrokeCap.Round),
    )
    drawLine(
        RedPower,
        Offset(rightX, callY - s * 0.60f),
        Offset(rightX, callY - s * 0.06f),
        sw * 1.7f,
        StrokeCap.Round,
    )
}

private fun DrawScope.drawBracket(cx: Float, cy: Float, s: Float, mirrored: Boolean) {
    val dir = if (mirrored) -1f else 1f
    val p = Path().apply {
        moveTo(cx - dir * s * 0.55f, cy - s * 0.30f)
        lineTo(cx + dir * s * 0.10f, cy - s * 0.30f)
        quadraticTo(cx + dir * s * 0.48f, cy - s * 0.30f, cx + dir * s * 0.48f, cy + s * 0.20f)
        lineTo(cx + dir * s * 0.48f, cy + s * 0.42f)
    }
    drawPath(
        p,
        BracketBlue,
        style = Stroke(width = size.width * 0.010f, cap = StrokeCap.Round),
    )
}