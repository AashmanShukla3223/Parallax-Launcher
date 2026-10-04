package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
 * the electric-blue laser etch cut into it. The etch forms three vertical
 * channels — outer two wide, centre one narrow — and each key is simply the gap
 * bracketed by a horizontal etch line across its channel. That narrow centre
 * channel is what gives the V3i deck its waisted silhouette.
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
                val cellY = keyRowTop(rowIndex)
                row.forEachIndexed { colIndex, key ->
                    val cellX = when (colIndex) {
                        0 -> 0.065f
                        1 -> 0.375f
                        else -> 0.675f
                    }
                    val cellW = channelWidth(colIndex)
                    KeyLegend(
                        key = key,
                        tint = palette.legend,
                        dim = palette.legendDim,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * cellX, y = h * cellY)
                            .size(w * cellW, h * 0.112f),
                    )
                }
            }

            // ---- Hit targets ---------------------------------------------------
            // Rocker directions sit just inside the ring.
            hit(w * 0.155f, h * 0.135f, w * 0.19f, h * 0.10f) { haptics.click(); onUp() }
            hit(w * 0.155f, h * 0.345f, w * 0.19f, h * 0.10f) { haptics.click(); onDown() }
            hit(w * 0.215f, h * 0.240f, w * 0.10f, h * 0.20f) { haptics.click(); onLeft() }
            hit(w * 0.595f, h * 0.240f, w * 0.10f, h * 0.20f) { haptics.click(); onRight() }
            hit(w * 0.405f, h * 0.205f, w * 0.19f, h * 0.16f) { haptics.thud(); onCenter() }

            // Soft keys, outermost in the top corners.
            hit(w * 0.060f, h * 0.160f, w * 0.150f, h * 0.090f) { haptics.click(); onSoftLeft() }
            hit(w * 0.790f, h * 0.160f, w * 0.150f, h * 0.090f) { haptics.click(); onSoftRight() }

            // Voice key, top centre.
            hit(w * 0.455f, h * 0.030f, w * 0.090f, h * 0.060f) { haptics.click(); onVoice() }

            // Dedicated function keys.
            hit(w * 0.060f, h * 0.255f, w * 0.150f, h * 0.090f) { haptics.click(); onGlobe() }
            hit(w * 0.790f, h * 0.255f, w * 0.150f, h * 0.090f) { haptics.click(); onEnvelope() }

            // Green send / red power, low on the flanks.
            hit(w * 0.060f, h * 0.335f, w * 0.150f, h * 0.090f) { haptics.thud(); onCall() }
            hit(w * 0.790f, h * 0.335f, w * 0.150f, h * 0.090f) { haptics.thud(); onEnd() }

            // 12 keys.
            RazrKeypadLayout.rows.forEachIndexed { rowIndex, row ->
                val y = keyRowTop(rowIndex)
                row.forEachIndexed { colIndex, key ->
                    hit(
                        w * keyChannelStart(colIndex),
                        h * y,
                        w * channelWidth(colIndex),
                        h * 0.112f,
                    ) {
                        haptics.click()
                        onKey(key.main[0])
                    }
                }
            }
        }
    }
}

// ---- Numeric block geometry --------------------------------------------------

/** Left edge fraction of the three etch channels. */
private fun keyChannelStart(col: Int): Float = when (col) {
    0 -> 0.065f
    1 -> 0.375f
    else -> 0.675f
}

/** Width fraction — the centre channel is deliberately the narrow one. */
private fun channelWidth(col: Int): Float = when (col) {
    0 -> 0.260f
    1 -> 0.250f
    else -> 0.260f
}

/** Top edge of each key row. */
private fun keyRowTop(row: Int): Float = 0.455f + row * 0.1125f

@Composable
private fun Modifier.hitOffset(x: Dp, y: Dp) = this.offset(x = x, y = y)

@Composable
private fun androidx.compose.foundation.layout.BoxScope.hit(
    x: Dp,
    y: Dp,
    hitW: Dp,
    hitH: Dp,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .hitOffset(x, y)
            .size(hitW, hitH)
            .clickable(onClick = onClick)
    )
}

// ---- Legend ------------------------------------------------------------------

/**
 * One key's legend: a large digit with a small, raised letter group beside it,
 * mirrored to the other side for the right-hand column.
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
            // Letters to the left of the digit (column 3 and "#").
            key.lettersFirst && letters.isNotEmpty() -> Row(
                Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = letters,
                    color = dim,
                    fontSize = 7.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
                Text(
                    text = key.main,
                    color = tint,
                    fontSize = 17.sp,
                    maxLines = 1,
                )
            }

            // Letters to the right of the digit (columns 1 and 2).
            letters.isNotEmpty() -> Row(
                Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = key.main,
                    color = tint,
                    fontSize = 17.sp,
                    maxLines = 1,
                )
                Text(
                    text = letters,
                    color = dim,
                    fontSize = 7.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }

            else -> Text(
                text = key.main,
                color = tint,
                fontSize = 17.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 1.dp),
            )
        }
    }
}

// ---- Etched groove network ---------------------------------------------------

private fun DrawScope.drawEtch(palette: RazrPalette) {
    val w = size.width
    val h = size.height
    val g = minOf(w, h) * 0.011f

    fun etch(path: Path, width: Float = g) {
        drawPath(
            path, palette.grooveGlow.copy(alpha = 0.26f),
            style = Stroke(width = width * 2.8f, cap = StrokeCap.Round),
        )
        drawPath(path, palette.groove, style = Stroke(width = width, cap = StrokeCap.Round))
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = g) {
        etch(Path().apply { moveTo(x1, y1); lineTo(x2, y2) }, width)
    }

    // --- Top deck: the "keyhole" arch around the rocker.
    val deckBottom = h * 0.440f
    val left = w * 0.065f
    val right = w * 0.935f
    val shoulder = h * 0.160f
    val peak = h * 0.100f
    val innerL = w * 0.400f
    val innerR = w * 0.600f

    etch(
        Path().apply {
            moveTo(left, deckBottom)
            lineTo(left, shoulder)
            quadraticTo(left, shoulder - h * 0.030f, left + w * 0.050f, shoulder - h * 0.030f)
            lineTo(innerL - w * 0.030f, shoulder - h * 0.030f)
            quadraticTo(innerL, shoulder - h * 0.070f, innerL + w * 0.030f, peak)
            lineTo(innerR - w * 0.030f, peak)
            quadraticTo(innerR, shoulder - h * 0.070f, innerR + w * 0.030f, shoulder - h * 0.030f)
            lineTo(right - w * 0.050f, shoulder - h * 0.030f)
            quadraticTo(right, shoulder - h * 0.030f, right, shoulder)
            lineTo(right, deckBottom)
        }
    )
    line(left, deckBottom, right, deckBottom)

    // Voice-key well at the very top centre.
    etch(
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.440f, top = h * 0.042f,
                    right = w * 0.560f, bottom = h * 0.060f,
                    cornerRadius = CornerRadius(h * 0.009f),
                )
            )
        },
        width = g * 0.7f,
    )

    // --- Numeric block: three channels, centre one narrower.
    val starts = listOf(0.065f, 0.375f, 0.675f)
    val ends = listOf(0.325f, 0.625f, 0.935f)
    val rowLines = listOf(0.455f, 0.567f, 0.680f, 0.792f, 0.905f, 0.985f)
    val blockTop = h * rowLines.first()
    val blockBottom = h * rowLines.last()

    starts.forEach { line(w * it, blockTop, w * it, blockBottom) }
    ends.forEach { line(w * it, blockTop, w * it, blockBottom) }
    rowLines.forEach { f ->
        val y = h * f
        starts.indices.forEach { i -> line(w * starts[i], y, w * ends[i], y) }
    }
}

/** Moulded navigation rocker with its raised silver select button. */
private fun DrawScope.drawDpad(palette: RazrPalette) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.280f
    val r = size.width * 0.190f
    val ring = size.width * 0.014f

    drawCircle(
        color = palette.grooveGlow.copy(alpha = 0.30f),
        radius = r + ring * 1.5f,
        style = Stroke(width = ring * 3f),
    )
    drawCircle(color = palette.groove, radius = r + ring * 0.5f, style = Stroke(width = ring))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.wellFace, palette.deckLow),
            center = Offset(cx - r * 0.25f, cy - r * 0.30f),
        ),
        radius = r,
        center = Offset(cx, cy),
    )
    drawCircle(Color.Black.copy(alpha = 0.30f), r, Offset(cx, cy), style = Stroke(width = ring * 0.6f))

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