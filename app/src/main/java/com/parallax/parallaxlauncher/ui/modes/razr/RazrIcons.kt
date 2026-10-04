package com.parallax.parallaxlauncher.ui.modes.razr

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * Vector pictograms for the RAZR menu.
 *
 * The stock handsets used small, colourful, detailed icons. Substituting
 * monospace dingbats (✉ ☰ ◍ ♪ ⚙) was the single clearest tell that this was
 * not a real phone, so every icon is now drawn from paths.
 */
enum class RazrIcon {
    CALLS,
    MESSAGES,
    PHONEBOOK,
    WEB_ACCESS,
    GAMES,
    MULTIMEDIA,
    TOOLS,
    SETTINGS,
    CAMERA,
    VOICEMAIL,
    FOLDER,
    VIDEO,
    PLAYLIST,
    CALENDAR,
    CALCULATOR,
    ALARM,
    HELP,
    FILES,
    SECURITY,
    NETWORK,
    INBOX,
    CHECKLIST,
}

/**
 * Draws [icon] inside the current draw scope, scaled to the given size.
 *
 * Each icon is painted in the flat, slightly-shaded style of the original:
 * a saturated body, a darker edge, and a small white highlight.
 */
@Composable
fun RazrIconGlyph(
    icon: RazrIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) { drawIcon(icon, tint) }
}

private fun DrawScope.drawIcon(icon: RazrIcon, tint: Color) {
    val s = size.minDimension
    fun u(f: Float) = s * f
    val dark = tint.darken(0.34f)
    val light = tint.lighten(0.30f)

    when (icon) {
        RazrIcon.MESSAGES -> {
            // Envelope: body, flap, white letter.
            val b = Rect(u(0.06f), u(0.20f), u(0.94f), u(0.80f))
            drawRoundRect(tint, b.topLeft, b.size, CornerRadius(u(0.05f)))
            drawRoundRect(dark, b.topLeft, Size(b.size.width, u(0.07f)), CornerRadius(u(0.03f)))
            val flap = Path().apply {
                moveTo(u(0.08f), u(0.24f))
                lineTo(u(0.50f), u(0.60f))
                lineTo(u(0.92f), u(0.24f))
            }
            drawPath(flap, light.copy(alpha = 0.85f), style = Stroke(width = u(0.07f)))
            drawLine(light, Offset(u(0.10f), u(0.78f)), Offset(u(0.40f), u(0.52f)), u(0.045f))
            drawLine(light, Offset(u(0.90f), u(0.78f)), Offset(u(0.60f), u(0.52f)), u(0.045f))
        }

        RazrIcon.CALLS -> {
            // Handset.
            rotate(-38f) {
                val p = Path().apply {
                    moveTo(u(0.30f), u(0.10f))
                    cubicTo(u(0.18f), u(0.12f), u(0.12f), u(0.24f), u(0.18f), u(0.40f))
                    cubicTo(u(0.26f), u(0.62f), u(0.44f), u(0.84f), u(0.66f), u(0.88f))
                    cubicTo(u(0.82f), u(0.90f), u(0.92f), u(0.78f), u(0.90f), u(0.64f))
                    lineTo(u(0.74f), u(0.46f))
                    cubicTo(u(0.70f), u(0.41f), u(0.64f), u(0.43f), u(0.61f), u(0.48f))
                    cubicTo(u(0.55f), u(0.58f), u(0.45f), u(0.48f), u(0.42f), u(0.36f))
                    cubicTo(u(0.40f), u(0.31f), u(0.43f), u(0.26f), u(0.38f), u(0.22f))
                    close()
                }
                drawPath(p, tint)
            }
        }

        RazrIcon.PHONEBOOK, RazrIcon.INBOX -> {
            // Contact card with a portrait.
            drawRoundRect(tint, Offset(u(0.08f), u(0.14f)), Size(u(0.84f), u(0.72f)), CornerRadius(u(0.06f)))
            drawRoundRect(light, Offset(u(0.08f), u(0.14f)), Size(u(0.84f), u(0.20f)), CornerRadius(u(0.06f)))
            drawCircle(dark, u(0.32f), Offset(u(0.34f), u(0.52f)))
            val body = Path().apply {
                moveTo(u(0.16f), u(0.80f))
                cubicTo(u(0.16f), u(0.62f), u(0.52f), u(0.62f), u(0.52f), u(0.80f))
                close()
            }
            drawPath(body, dark)
            listOf(0.62f, 0.72f, 0.82f).forEach { y ->
                drawRoundRect(
                    light.copy(alpha = 0.9f),
                    Offset(u(0.58f), u(y)), Size(u(0.26f), u(0.05f)), CornerRadius(u(0.02f)),
                )
            }
        }

        RazrIcon.WEB_ACCESS -> {
            // Globe.
            drawCircle(tint, u(0.50f), Offset(u(0.50f), u(0.50f)), u(0.40f))
            drawCircle(dark, u(0.50f), Offset(u(0.50f), u(0.50f)), u(0.40f), style = Stroke(u(0.035f)))
            drawOval(
                light.copy(alpha = 0.75f),
                Offset(u(0.30f), u(0.10f)), Size(u(0.40f), u(0.80f)),
                style = Stroke(u(0.030f)),
            )
            drawLine(light.copy(alpha = 0.75f), Offset(u(0.12f), u(0.50f)), Offset(u(0.88f), u(0.50f)), u(0.030f))
            drawLine(light.copy(alpha = 0.55f), Offset(u(0.18f), u(0.32f)), Offset(u(0.82f), u(0.32f)), u(0.022f))
            drawLine(light.copy(alpha = 0.55f), Offset(u(0.18f), u(0.68f)), Offset(u(0.82f), u(0.68f)), u(0.022f))
        }

        RazrIcon.GAMES -> {
            // Game controller.
            drawRoundRect(tint, Offset(u(0.05f), u(0.32f)), Size(u(0.90f), u(0.38f)), CornerRadius(u(0.18f)))
            drawCircle(light, u(0.05f), Offset(u(0.74f), u(0.46f)))
            drawCircle(light, u(0.05f), Offset(u(0.86f), u(0.58f)))
            drawLine(dark, Offset(u(0.20f), u(0.44f)), Offset(u(0.32f), u(0.44f)), u(0.055f))
            drawLine(dark, Offset(u(0.26f), u(0.38f)), Offset(u(0.26f), u(0.50f)), u(0.055f))
        }

        RazrIcon.MULTIMEDIA -> {
            // Eighth note.
            drawRoundRect(dark, Offset(u(0.54f), u(0.10f)), Size(u(0.07f), u(0.52f)), CornerRadius(u(0.03f)))
            val flag = Path().apply {
                moveTo(u(0.61f), u(0.12f))
                cubicTo(u(0.78f), u(0.16f), u(0.88f), u(0.10f), u(0.86f), u(0.28f))
                cubicTo(u(0.84f), u(0.42f), u(0.72f), u(0.40f), u(0.61f), u(0.36f))
                close()
            }
            drawPath(flag, tint)
            drawOval(tint, Offset(u(0.36f), u(0.58f)), Size(u(0.26f), u(0.20f)))
        }

        RazrIcon.VIDEO, RazrIcon.PLAYLIST -> {
            // Film frame with a play triangle.
            drawRoundRect(dark, Offset(u(0.05f), u(0.20f)), Size(u(0.90f), u(0.60f)), CornerRadius(u(0.05f)))
            drawRoundRect(tint, Offset(u(0.14f), u(0.27f)), Size(u(0.72f), u(0.46f)), CornerRadius(u(0.03f)))
            var x = u(0.07f)
            while (x < u(0.90f)) {
                drawRoundRect(light, Offset(x, u(0.22f)), Size(u(0.05f), u(0.06f)), CornerRadius(u(0.01f)))
                drawRoundRect(light, Offset(x, u(0.72f)), Size(u(0.05f), u(0.06f)), CornerRadius(u(0.01f)))
                x += u(0.11f)
            }
            val tri = Path().apply {
                moveTo(u(0.42f), u(0.36f))
                lineTo(u(0.64f), u(0.50f))
                lineTo(u(0.42f), u(0.64f))
                close()
            }
            drawPath(tri, light)
        }

        RazrIcon.CAMERA -> {
            // Camera body with a raised viewfinder hump.
            drawRoundRect(dark, Offset(u(0.28f), u(0.12f)), Size(u(0.26f), u(0.10f)), CornerRadius(u(0.03f)))
            drawRoundRect(tint, Offset(u(0.06f), u(0.20f)), Size(u(0.88f), u(0.62f)), CornerRadius(u(0.07f)))
            drawCircle(dark, u(0.26f), Offset(u(0.50f), u(0.52f)))
            drawCircle(light.copy(alpha = 0.65f), u(0.20f), Offset(u(0.50f), u(0.52f)))
            drawCircle(light, u(0.05f), Offset(u(0.80f), u(0.32f)))
        }

        RazrIcon.TOOLS -> {
            // Wrench crossed with a screwdriver.
            rotate(-40f) {
                drawRoundRect(dark, Offset(u(0.44f), u(0.20f)), Size(u(0.12f), u(0.66f)), CornerRadius(u(0.05f)))
                drawCircle(light, u(0.10f), Offset(u(0.50f), u(0.16f)))
                drawCircle(tint, u(0.16f), Offset(u(0.14f), u(0.80f)))
            }
        }

        RazrIcon.SETTINGS -> {
            // Gear.
            val teeth = 8
            for (i in 0 until teeth) {
                rotate(i * (360f / teeth), pivot = Offset(u(0.5f), u(0.5f))) {
                    drawRoundRect(tint, Offset(u(0.43f), u(0.04f)), Size(u(0.14f), u(0.22f)), CornerRadius(u(0.03f)))
                }
            }
            drawCircle(tint, u(0.32f), Offset(u(0.50f), u(0.50f)))
            drawCircle(light, u(0.15f), Offset(u(0.50f), u(0.50f)))
        }

        RazrIcon.FOLDER -> {
            // Manila folder.
            val tab = Path().apply {
                moveTo(u(0.06f), u(0.26f))
                lineTo(u(0.42f), u(0.26f))
                lineTo(u(0.50f), u(0.36f))
                lineTo(u(0.94f), u(0.36f))
                lineTo(u(0.94f), u(0.80f))
                lineTo(u(0.06f), u(0.80f))
                close()
            }
            drawPath(tab, tint)
            drawRoundRect(light.copy(alpha = 0.7f), Offset(u(0.06f), u(0.30f)), Size(u(0.88f), u(0.08f)))
        }

        RazrIcon.FILES -> {
            // Stacked documents.
            listOf(0.18f to 0.62f, 0.28f to 0.44f).forEachIndexed { i, (x, w) ->
                drawRoundRect(
                    if (i == 0) dark else tint,
                    Offset(u(x), u(0.16f + i * 0.10f)),
                    Size(u(w), u(0.52f)),
                    CornerRadius(u(0.04f)),
                )
            }
            drawRoundRect(light, Offset(u(0.40f), u(0.42f)), Size(u(0.30f), u(0.06f)), CornerRadius(u(0.02f)))
        }

        RazrIcon.CALCULATOR -> {
            drawRoundRect(dark, Offset(u(0.18f), u(0.06f)), Size(u(0.64f), u(0.88f)), CornerRadius(u(0.07f)))
            drawRoundRect(light, Offset(u(0.24f), u(0.12f)), Size(u(0.52f), u(0.20f)), CornerRadius(u(0.03f)))
            for (r in 0..2) for (c in 0..2) {
                drawRoundRect(
                    tint,
                    Offset(u(0.25f + c * 0.18f), u(0.38f + r * 0.17f)),
                    Size(u(0.13f), u(0.12f)),
                    CornerRadius(u(0.02f)),
                )
            }
        }

        RazrIcon.CALENDAR, RazrIcon.ALARM -> {
            drawRoundRect(tint, Offset(u(0.10f), u(0.18f)), Size(u(0.80f), u(0.70f)), CornerRadius(u(0.06f)))
            drawRoundRect(dark, Offset(u(0.10f), u(0.18f)), Size(u(0.80f), u(0.18f)), CornerRadius(u(0.06f)))
            drawRoundRect(light, Offset(u(0.24f), u(0.06f)), Size(u(0.07f), u(0.16f)), CornerRadius(u(0.03f)))
            drawRoundRect(light, Offset(u(0.69f), u(0.06f)), Size(u(0.07f), u(0.16f)), CornerRadius(u(0.03f)))
            (0..2).forEach { r ->
                (0..3).forEach { c ->
                    drawRoundRect(
                        dark.copy(alpha = 0.35f),
                        Offset(u(0.18f + c * 0.18f), u(0.44f + r * 0.14f)),
                        Size(u(0.12f), u(0.09f)),
                        CornerRadius(u(0.02f)),
                    )
                }
            }
        }

        RazrIcon.CHECKLIST -> {
            drawRoundRect(light, Offset(u(0.12f), u(0.10f)), Size(u(0.76f), u(0.80f)), CornerRadius(u(0.05f)))
            drawRoundRect(dark, Offset(u(0.12f), u(0.10f)), Size(u(0.76f), u(0.16f)), CornerRadius(u(0.05f)))
            (0..2).forEach { r ->
                val y = u(0.34f + r * 0.18f)
                drawLine(tint, Offset(u(0.20f), y + u(0.05f)), Offset(u(0.28f), y + u(0.12f)), u(0.05f))
                drawLine(tint, Offset(u(0.28f), y + u(0.12f)), Offset(u(0.42f), y - u(0.04f)), u(0.05f))
                drawRoundRect(dark.copy(alpha = 0.4f), Offset(u(0.48f), y + u(0.03f)), Size(u(0.32f), u(0.06f)), CornerRadius(u(0.03f)))
            }
        }

        RazrIcon.VOICEMAIL -> {
            // Tape reel.
            drawCircle(tint, u(0.30f), Offset(u(0.50f), u(0.50f)))
            drawCircle(dark, u(0.12f), Offset(u(0.50f), u(0.50f)))
            drawCircle(light, u(0.30f), Offset(u(0.86f), u(0.34f)))
            drawLine(dark, Offset(u(0.66f), u(0.36f)), Offset(u(0.20f), u(0.62f)), u(0.05f))
        }

        RazrIcon.NETWORK -> {
            // Signal mast with arcs.
            drawRoundRect(dark, Offset(u(0.44f), u(0.36f)), Size(u(0.12f), u(0.56f)), CornerRadius(u(0.04f)))
            drawCircle(tint, u(0.09f), Offset(u(0.50f), u(0.34f)))
            listOf(0.22f, 0.34f, 0.46f).forEachIndexed { i, r ->
                drawArc(
                    tint.copy(alpha = 0.85f - i * 0.16f),
                    startAngle = 200f, sweepAngle = 140f, useCenter = false,
                    topLeft = Offset(u(0.50f - r), u(0.34f - r)),
                    size = Size(u(r * 2f), u(r * 2f)),
                    style = Stroke(width = u(0.05f)),
                )
            }
        }

        RazrIcon.SECURITY -> {
            // Padlock shackle.
            drawRoundRect(tint, Offset(u(0.24f), u(0.44f)), Size(u(0.52f), u(0.44f)), CornerRadius(u(0.06f)))
            drawArc(
                tint, startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(u(0.34f), u(0.14f)), size = Size(u(0.32f), u(0.40f)),
                style = Stroke(width = u(0.10f)),
            )
            drawCircle(light, u(0.05f), Offset(u(0.50f), u(0.64f)))
            drawRoundRect(light, Offset(u(0.46f), u(0.64f)), Size(u(0.08f), u(0.16f)), CornerRadius(u(0.03f)))
        }

        RazrIcon.HELP -> {
            drawCircle(tint, u(0.44f), Offset(u(0.50f), u(0.50f)))
            drawCircle(light.copy(alpha = 0.5f), u(0.34f), Offset(u(0.38f), u(0.36f)))
            val q = Path().apply {
                moveTo(u(0.36f), u(0.40f))
                cubicTo(u(0.36f), u(0.24f), u(0.66f), u(0.24f), u(0.66f), u(0.42f))
                cubicTo(u(0.66f), u(0.56f), u(0.50f), u(0.56f), u(0.50f), u(0.66f))
            }
            drawPath(q, light, style = Stroke(width = u(0.09f)))
            drawCircle(light, u(0.055f), Offset(u(0.50f), u(0.78f)))
        }
    }
}

private fun Color.darken(amount: Float) = Color(
    red = (red * (1f - amount)).coerceIn(0f, 1f),
    green = (green * (1f - amount)).coerceIn(0f, 1f),
    blue = (blue * (1f - amount)).coerceIn(0f, 1f),
    alpha = alpha,
)

private fun Color.lighten(amount: Float) = Color(
    red = (red + (1f - red) * amount).coerceIn(0f, 1f),
    green = (green + (1f - green) * amount).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * amount).coerceIn(0f, 1f),
    alpha = alpha,
)

/** Accent tints used by the stock icon set. */
object RazrIconTints {
    val Blue = Color(0xFF2E74C8)
    val Cyan = Color(0xFF2AA7C4)
    val Green = Color(0xFF3FA34D)
    val Amber = Color(0xFFE0A020)
    val Orange = Color(0xFFDE6B22)
    val Red = Color(0xFFC93A2E)
    val Purple = Color(0xFF7A5AA8)
    val Slate = Color(0xFF5A6472)
}