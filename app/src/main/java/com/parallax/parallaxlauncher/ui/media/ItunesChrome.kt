package com.parallax.parallaxlauncher.ui.media

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate

/**
 * Paints the brushed face and bevel across the whole [DrawScope] bounds.
 *
 * The pinstripes are the whole point: iTunes 5 read as *brushed* because of a
 * very fine vertical ruling over a horizontal gradient, and a flat grey fill
 * immediately reads as "generic panel" instead. Also draws the bevel — a light
 * top edge and a dark bottom one — which is what made the window look like a
 * physical plate rather than a rectangle.
 */
fun DrawScope.brushedFace(radius: Float, stripe: Float = 1f) {
val path = Path().apply {
    addRoundRect(
        RoundRect(
            left = 0f, top = 0f, right = size.width, bottom = size.height,
            cornerRadius = CornerRadius(radius, radius),
        ),
    )
}
clipPath(path) {
    // Horizontal gradient: lit along the top, falling away to the bottom.
    drawRect(
        brush = Brush.verticalGradient(
            0f to ItunesSkin.MetalHi,
            0.45f to ItunesSkin.MetalMid,
            1f to ItunesSkin.MetalLo,
        ),
    )
    // Fine vertical ruling. Two passes — a dark rule and a light one just
    // beside it — so it catches light the way real brushing does instead of
    // reading as a grid.
    var x = 0f
    while (x < size.width) {
        drawLine(
            ItunesSkin.MetalLine,
            Offset(x, 0f),
            Offset(x, size.height),
            strokeWidth = stripe,
        )
        drawLine(
            ItunesSkin.MetalLineHi,
            Offset(x + stripe, 0f),
            Offset(x + stripe, size.height),
            strokeWidth = stripe,
        )
        x += stripe * 3f
    }
    // Bevel: bright along the top, dark along the bottom.
    drawLine(
        ItunesSkin.MetalEdgeHi,
        Offset(0f, 1f),
        Offset(size.width, 1f),
        strokeWidth = 2f,
    )
    drawLine(
        ItunesSkin.MetalEdgeLo,
        Offset(0f, size.height - 1f),
        Offset(size.width, size.height - 1f),
        strokeWidth = 2f,
    )
}
drawRoundRect(
    color = ItunesSkin.MetalEdgeLo,
    topLeft = Offset(0f, 0f),
    size = Size(size.width, size.height),
    cornerRadius = CornerRadius(radius, radius),
    style = Stroke(width = 1.5f),
)
}

/**
 * The sunken LCD well.
 *
 * Inset rather than raised: dark along the top edge, light along the bottom,
 * the inverse of the outer bevel. Getting that backwards is the single most
 * common way a fake "LCD" reads as a sticker.
 */
fun DrawScope.lcdWell(
left: Float,
top: Float,
width: Float,
height: Float,
radius: Float,
) {
val path = Path().apply {
    addRoundRect(
        RoundRect(
            left = left, top = top,
            right = left + width, bottom = top + height,
            cornerRadius = CornerRadius(radius, radius),
        ),
    )
}
clipPath(path) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to ItunesSkin.LcdLo,
            0.35f to ItunesSkin.LcdHi,
            1f to ItunesSkin.LcdHi,
            startY = top,
            endY = top + height,
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
    )
    drawLine(
        ItunesSkin.LcdEdge,
        Offset(left, top + 1f),
        Offset(left + width, top + 1f),
        strokeWidth = 2f,
    )
    drawLine(
        Color.White.copy(alpha = 0.55f),
        Offset(left, top + height - 1f),
        Offset(left + width, top + height - 1f),
        strokeWidth = 2f,
    )
}
drawRoundRect(
    color = ItunesSkin.MetalEdgeLo.copy(alpha = 0.7f),
    topLeft = Offset(left, top),
    size = Size(width, height),
    cornerRadius = CornerRadius(radius, radius),
    style = Stroke(width = 1f),
)
}

/**
 * One round chrome button, drawn as a face plus a glyph.
 *
 * [active] tints it selection-blue, which is how iTunes showed repeat and
 * shuffle engaged rather than greying them out.
 *
 * [glyph] is a path in a -1..1 space, scaled to the button, so callers do not
 * have to reason about button radii.
 */
fun DrawScope.itunesButton(
cx: Float,
cy: Float,
radius: Float,
active: Boolean,
glyph: (DrawScope.(scale: Float) -> Unit)?,
) {
// Drop shadow under the button, then the face.
drawCircle(
    color = Color.Black.copy(alpha = 0.28f),
    radius = radius,
    center = Offset(cx, cy + radius * 0.12f),
)
drawCircle(
    brush = Brush.radialGradient(
        colors = if (active) {
            listOf(ItunesSkin.AccentHi, ItunesSkin.Accent)
        } else {
            listOf(ItunesSkin.ChromeGloss, ItunesSkin.Chrome, ItunesSkin.MetalLo)
        },
        center = Offset(cx - radius * 0.35f, cy - radius * 0.4f),
        radius = radius * 1.7f,
    ),
    radius = radius,
    center = Offset(cx, cy),
)
drawCircle(
    color = ItunesSkin.ChromeEdge,
    radius = radius,
    center = Offset(cx, cy),
    style = Stroke(width = 1.2f),
)
// Specular highlight across the top third, the thing that makes it chrome.
drawCircle(
    brush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.75f),
        1f to Color.White.copy(alpha = 0f),
        startY = cy - radius,
        endY = cy - radius * 0.1f,
    ),
    radius = radius * 0.92f,
    center = Offset(cx, cy - radius * 0.28f),
)

if (glyph != null) {
    translate(cx, cy) {
        glyph(radius * 0.52f)
    }
}
}

/**
 * Triangles and bars, in a -1..1 space so they scale with the button.
 *
 * These are `DrawScope` extensions rather than plain functions because they
 * draw; [itunesButton] invokes them inside a translated DrawScope so callers
 * just write `glyphPlay(it)` without computing the button centre.
 */
/** Solid right-pointing triangle: play. */
fun DrawScope.glyphPlay(s: Float) {
    val p = Path().apply {
        moveTo(-s * 0.55f, -s * 0.8f)
        lineTo(s * 0.85f, 0f)
        lineTo(-s * 0.55f, s * 0.8f)
        close()
    }
    drawPath(p, ItunesSkin.ChromeInk)
}

/** Two bars: pause. */
fun DrawScope.glyphPause(s: Float) {
    val w = s * 0.38f
    drawRect(
        color = ItunesSkin.ChromeInk,
        topLeft = Offset(-s * 0.72f, -s * 0.78f),
        size = Size(w, s * 1.56f),
    )
    drawRect(
        color = ItunesSkin.ChromeInk,
        topLeft = Offset(s * 0.34f, -s * 0.78f),
        size = Size(w, s * 1.56f),
    )
}

/** Double triangle plus bar: previous / next. */
fun DrawScope.glyphSkipForward(s: Float) {
    for (i in 0..1) {
        val o = -s * 0.75f + i * s * 0.62f
        val p = Path().apply {
            moveTo(o, -s * 0.75f)
            lineTo(o + s * 0.62f, 0f)
            lineTo(o, s * 0.75f)
            close()
        }
        drawPath(p, ItunesSkin.ChromeInk)
    }
}

fun DrawScope.glyphSkipBack(s: Float) {
    for (i in 0..1) {
        val o = s * 0.75f - i * s * 0.62f
        val p = Path().apply {
            moveTo(o, -s * 0.75f)
            lineTo(o - s * 0.62f, 0f)
            lineTo(o, s * 0.75f)
            close()
        }
        drawPath(p, ItunesSkin.ChromeInk)
    }
}

/** Two chasing arrows: repeat. */
fun DrawScope.glyphRepeat(s: Float) {
    val w = s * 0.34f
    val h = s * 0.9f
    // Top bar, right-pointing hook.
    drawLine(
        ItunesSkin.ChromeInk, Offset(-s * 0.8f, -h * 0.35f), Offset(s * 0.6f, -h * 0.35f),
        strokeWidth = w,
    )
    drawLine(
        ItunesSkin.ChromeInk, Offset(-s * 0.8f, h * 0.35f), Offset(s * 0.6f, h * 0.35f),
        strokeWidth = w,
    )
    // Arrow heads at the right of the top and left of the bottom.
    val a = Path().apply {
        moveTo(s * 0.95f, -h * 0.35f)
        lineTo(s * 0.35f, -h * 0.72f)
        lineTo(s * 0.35f, h * 0.02f)
        close()
    }
    drawPath(a, ItunesSkin.ChromeInk)
    val b = Path().apply {
        moveTo(-s * 0.95f, h * 0.35f)
        lineTo(-s * 0.35f, h * 0.72f)
        lineTo(-s * 0.35f, -h * 0.02f)
        close()
    }
    drawPath(b, ItunesSkin.ChromeInk)
}

/** Single "1" bar for repeat-one, distinguished from plain repeat. */
fun DrawScope.glyphRepeatOne(s: Float) {
    // Qualified: a bare repeat(s) resolves to the stdlib's repeat(times)
    // first, which does not take a Float and fails to compile.
    this.repeat(s)
    val bar = Path().apply {
        moveTo(0f, -s * 0.16f)
        lineTo(0f, s * 0.16f)
    }
    drawPath(bar, ItunesSkin.ChromeGloss, style = Stroke(width = s * 0.28f))
    drawCircle(ItunesSkin.ChromeGloss, radius = s * 0.13f, center = Offset(0f, -s * 0.42f))
    drawCircle(ItunesSkin.ChromeGloss, radius = s * 0.13f, center = Offset(0f, s * 0.42f))
}

/** Crossing arrows: shuffle. */
fun DrawScope.glyphShuffle(s: Float) {
    val w = s * 0.3f
    drawLine(
        ItunesSkin.ChromeInk,
        Offset(-s * 0.8f, -h0(s)), Offset(s * 0.5f, h0(s)),
        strokeWidth = w,
    )
    drawLine(
        ItunesSkin.ChromeInk,
        Offset(-s * 0.8f, h0(s)), Offset(s * 0.5f, -h0(s)),
        strokeWidth = w,
    )
    val a = Path().apply {
        moveTo(s * 0.95f, h0(s) * 0.2f)
        lineTo(s * 0.3f, h0(s) * 1.5f)
        lineTo(s * 0.3f, -h0(s) * 0.6f)
        close()
    }
    drawPath(a, ItunesSkin.ChromeInk)
}

private fun h0(s: Float) = s * 0.45f
