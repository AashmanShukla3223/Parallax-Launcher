package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * Status indicator row described on page 46 of the V3i user guide: signal
 * strength, GPRS, data, roam, message, battery level and ring style all live
 * in a single strip across the top of the internal display.
 */
@Composable
fun RazrStatusBar(
    palette: RazrPalette,
    carrierName: String,
    signalBars: Int,
    batteryPercent: Int,
    charging: Boolean,
    /** Unread messages while the launcher owns the screen. */
    unreadMessages: Int,
    ringStyleGlyph: String,
    modifier: Modifier = Modifier,
) {
    val ink = palette.ink
    val inkDim = palette.inkDim

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.lcdBacklight)
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 1 Signal strength — vertical bars, per the manual.
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.7.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(7.dp)
            ) {
                (1..4).forEach { level ->
                    val h = (1.6f + level * 1.4f).dp
                    Box(
                        Modifier
                            .width(1.4.dp)
                            .height(h)
                            .background(if (level <= signalBars) ink else inkDim.copy(alpha = 0.4f))
                    )
                }
            }

            // 2 GPRS
            TinyTag("2G", ink)

            // 4 Roam
            TinyTag("R", inkDim)

            Spacer(Modifier.weight(1f))

            // 7 Message indicator — lit while the launcher holds unread alerts.
            if (unreadMessages > 0) {
                Text(
                    text = "✉",
                    color = palette.alert,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = unreadMessages.toString(),
                    color = palette.alert,
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Black,
                )
            }

            // 9 Ring style
            Text(text = ringStyleGlyph, color = ink, fontSize = 7.sp, fontWeight = FontWeight.Black)

            // 8 Battery level — vertical segments inside an outline.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .border(0.8.dp, ink, RoundedCornerShape(1.dp))
                        .padding(0.8.dp)
                ) {
                    val filled = (batteryPercent.coerceIn(0, 100) / 34 + 1).coerceIn(1, 3)
                    Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                        repeat(3) { i ->
                            Box(
                                Modifier
                                    .width(1.4.dp)
                                    .height(5.dp)
                                    .background(
                                        if (i < filled) {
                                            if (charging) Color(0xFF1E7B3C) else ink
                                        } else inkDim.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .height(3.dp)
                        .background(ink)
                )
            }
        }

        // Service provider banner line.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = carrierName.uppercase(),
                color = ink,
                fontFamily = FontFamily.Monospace,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.4.sp,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("❖", color = inkDim, fontSize = 6.sp)
                Text("▮", color = inkDim, fontSize = 6.sp)
            }
        }
    }
}

@Composable
private fun TinyTag(label: String, color: Color) {
    Text(
        text = label,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 5.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(1.dp))
            .border(0.6.dp, color.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
            .padding(horizontal = 1.dp)
    )
}

/** Soft key label bar drawn at the bottom of the internal display. */
@Composable
fun RazrSoftKeyBar(
    palette: RazrPalette,
    left: String,
    center: String,
    right: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.lcdBacklight)
            .border(0.7.dp, palette.inkDim.copy(alpha = 0.55f))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SoftLabel(palette, left, Modifier.weight(1f), TextAlign.Start)
        SoftLabel(palette, center, Modifier.weight(1.2f), TextAlign.Center)
        SoftLabel(palette, right, Modifier.weight(1f), TextAlign.End)
    }
}

@Composable
private fun SoftLabel(palette: RazrPalette, text: String, modifier: Modifier, align: TextAlign) {
    Box(modifier, contentAlignment = align.toAlignment()) {
        Text(
            text = text,
            color = palette.ink,
            fontFamily = FontFamily.Monospace,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private fun TextAlign.toAlignment(): Alignment = when (this) {
    TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
    TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
    else -> Alignment.Center
}