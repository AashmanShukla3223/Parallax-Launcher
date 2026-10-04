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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.ui.modes.razr.RazrKeypadLayout
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

private val GreenCall = Color(0xFF17C964)
private val GreenCallDim = Color(0xFF0B5C2E)
private val RedEnd = Color(0xFFE23B2E)
private val RedEndDim = Color(0xFF5E1610)

/**
 * The V3i lower clamshell: laser-etched electric-blue grooves tracing the
 * signature "keyhole" outline around the D-pad deck, then three rows of keys
 * hung beneath it, with the green send and red power keys flanking the D-pad.
 *
 * Layout mirrors the physical handset exactly: soft keys outermost, globe and
 * envelope keys next to the nav rocker, green/red below them, 12 keys last.
 */
@Composable
fun RazrKeypad(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
    haptics: HapticEngine,
    /** Navigation rocker. */
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    /** Left/right soft keys. */
    onSoftLeft: () -> Unit,
    onSoftRight: () -> Unit,
    /** Green handset / red power. */
    onCall: () -> Unit,
    onEnd: () -> Unit,
    /** Globe (browser) and envelope (messages) dedicated keys. */
    onGlobe: () -> Unit,
    onEnvelope: () -> Unit,
    /** Voice key, above the D-pad. */
    onVoice: () -> Unit,
    onKey: (Char) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(
                Brush.verticalGradient(listOf(palette.deckTop, palette.deckMid, palette.deckLow))
            )
            .border(1.5.dp, palette.deckEdge, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blue etch groove network painted across the whole deck.
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val g = size.minDimension * 0.028f
            val topY = size.height * 0.055f
            val deckBottom = size.height * 0.395f
            val grooveColor = palette.groove

            // Outer "keyhole" ring around the D-pad deck.
            val ringLeft = size.width * 0.06f
            val ringRight = size.width * 0.94f
            drawRoundRect(
                color = grooveColor,
                topLeft = Offset(ringLeft, topY),
                size = Size(ringRight - ringLeft, deckBottom - topY),
                cornerRadius = CornerRadius(g * 3.2f),
                style = Stroke(width = g),
            )
            // Glow pass under the etch for the backlit look.
            drawRoundRect(
                color = palette.grooveGlow.copy(alpha = 0.35f),
                topLeft = Offset(ringLeft, topY),
                size = Size(ringRight - ringLeft, deckBottom - topY),
                cornerRadius = CornerRadius(g * 3.2f),
                style = Stroke(width = g * 2.4f),
            )

            // Descending channels between each numeric row.
            val rowGaps = listOf(0.445f, 0.605f, 0.765f)
            val rowLines = listOf(0.425f, 0.585f, 0.745f, 0.905f)
            rowLines.forEach { f ->
                val y = size.height * f
                drawLine(grooveColor, Offset(ringLeft + g, y), Offset(ringRight - g, y), strokeWidth = g, cap = StrokeCap.Round)
            }
            // Vertical dividers between key columns, running through the numeric block.
            val colX = listOf(0.345f, 0.655f)
            colX.forEach { fx ->
                val x = size.width * fx
                drawLine(grooveColor, Offset(x, size.height * 0.425f), Offset(x, size.height * 0.905f), strokeWidth = g, cap = StrokeCap.Round)
            }
            // Waist channels that pinch the middle column, echoing the V3i outline.
            rowGaps.forEach { f ->
                val y = size.height * f
                drawLine(grooveColor, Offset(size.width * 0.06f, y), Offset(size.width * 0.345f, y), strokeWidth = g, cap = StrokeCap.Round)
                drawLine(grooveColor, Offset(size.width * 0.655f, y), Offset(size.width * 0.94f, y), strokeWidth = g, cap = StrokeCap.Round)
            }
        }

        // --- D-pad deck row ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(1.05f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftKeyCap(palette, "–", Modifier.size(width = 34.dp, height = 15.dp), haptics, onSoftLeft)
            GlyphKeyCap(palette, "◍", Modifier.size(width = 26.dp, height = 15.dp), haptics, onGlobe)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VoiceKeyCap(palette, onVoice, haptics)
                Spacer(Modifier.height(1.dp))
                NavRocker(
                    palette = palette,
                    onUp = onUp, onDown = onDown, onLeft = onLeft, onRight = onRight,
                    onCenter = onCenter, haptics = haptics,
                )
            }

            GlyphKeyCap(palette, "✉", Modifier.size(width = 26.dp, height = 15.dp), haptics, onEnvelope)
            SoftKeyCap(palette, "–", Modifier.size(width = 34.dp, height = 15.dp), haptics, onSoftRight)
        }

        // --- Green send / red power row ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.5f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallKeyCap(palette, "✆", GreenCall, GreenCallDim, Modifier.size(width = 40.dp, height = 17.dp), haptics, onCall)
            CallKeyCap(palette, "⏻", RedEnd, RedEndDim, Modifier.size(width = 40.dp, height = 17.dp), haptics, onEnd)
        }

        // --- 12-key numeric block ---
        Column(
            modifier = Modifier.fillMaxWidth().weight(2.4f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            RazrKeypadLayout.rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    row.forEach { key ->
                        NumericKeyCap(
                            palette = palette,
                            digit = key.main,
                            letters = key.letters,
                            icon = key.icon,
                            modifier = Modifier.weight(1f).height(26.dp),
                            onClick = {
                                haptics.click()
                                onKey(key.main[0])
                            },
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun SoftKeyCap(
    palette: RazrPalette,
    glyph: String,
    modifier: Modifier,
    haptics: HapticEngine,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(palette.wellFace)
            .border(0.8.dp, palette.wellEdge, RoundedCornerShape(3.dp))
            .clickable {
                haptics.click()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = palette.grooveGlow, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun GlyphKeyCap(
    palette: RazrPalette,
    glyph: String,
    modifier: Modifier,
    haptics: HapticEngine,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(palette.wellFace.copy(alpha = 0.6f))
            .clickable {
                haptics.click()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = palette.groove, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun VoiceKeyCap(palette: RazrPalette, onClick: () -> Unit, haptics: HapticEngine) {
    Box(
        modifier = Modifier
            .size(width = 16.dp, height = 7.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(palette.wellFace.copy(alpha = 0.6f))
            .clickable {
                haptics.click()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text("♩", color = palette.groove, fontSize = 5.sp)
    }
}

@Composable
private fun CallKeyCap(
    palette: RazrPalette,
    glyph: String,
    tint: Color,
    tintDim: Color,
    modifier: Modifier,
    haptics: HapticEngine,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Brush.verticalGradient(listOf(tintDim.copy(alpha = 0.55f), tintDim)))
            .border(1.dp, tint.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
            .clickable {
                haptics.thud()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NumericKeyCap(
    palette: RazrPalette,
    digit: String,
    letters: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Brush.verticalGradient(listOf(palette.wellFace, palette.wellFace.copy(alpha = 0.72f))))
            .border(0.8.dp, palette.wellEdge.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = digit,
                color = palette.legend,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            if (letters.isNotEmpty()) {
                Spacer(Modifier.width(1.5.dp))
                Text(
                    text = letters,
                    color = palette.legendDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 5.5.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
            if (icon.isNotEmpty()) {
                Spacer(Modifier.width(1.dp))
                Text(text = icon, color = palette.legendDim, fontSize = 5.sp)
            }
        }
    }
}

/** Four-way navigation rocker with the moulded centre select button. */
@Composable
private fun NavRocker(
    palette: RazrPalette,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    haptics: HapticEngine,
) {
    val outer = 54.dp
    Box(
        modifier = Modifier.size(outer),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(outer)) {
            val r = size.minDimension / 2f
            val w = size.minDimension * 0.06f
            // Outer moulded ring.
            drawCircle(color = palette.dpadRing, radius = r, style = Stroke(width = w * 1.6f))
            drawCircle(
                color = palette.groove.copy(alpha = 0.85f),
                radius = r,
                style = Stroke(width = w * 0.9f),
            )
            drawCircle(
                color = palette.grooveGlow.copy(alpha = 0.28f),
                radius = r - w,
                style = Stroke(width = w * 2.4f),
            )
        }
        // Direction pads.
        DirectionPad(Modifier.align(Alignment.TopCenter), "▲", palette) { haptics.click(); onUp() }
        DirectionPad(Modifier.align(Alignment.BottomCenter), "▼", palette) { haptics.click(); onDown() }
        DirectionPad(Modifier.align(Alignment.CenterStart), "◀", palette) { haptics.click(); onLeft() }
        DirectionPad(Modifier.align(Alignment.CenterEnd), "▶", palette) { haptics.click(); onRight() }

        // Centre select.
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(palette.dpadCenter, palette.dpadFace)
                    )
                )
                .border(1.dp, palette.groove, CircleShape)
                .clickable {
                    haptics.thud()
                    onCenter()
                }
        )
    }
}

@Composable
private fun DirectionPad(
    modifier: Modifier,
    glyph: String,
    palette: RazrPalette,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(15.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = palette.legend, fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}