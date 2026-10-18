package com.parallax.parallaxlauncher.ui.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Amber = Color(0xFFFF9500)
private val AmberDim = Color(0x99FF9500)
private val Steel = Color(0xFF26272B)
private val Ink = Color(0xFFE8E8ED)
private val Dim = Color(0xFF8A8A93)
private val Green = Color(0xFF34C759)

private fun mono(size: Int, color: Color = Amber, weight: FontWeight = FontWeight.Normal, ls: Int = 2) =
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = size.sp,
        color = color,
        fontWeight = weight,
        letterSpacing = ls.sp,
    )

/**
 * Now Playing.
 *
 * Drawn, not embedded -- the project rule is that marks are drawn rather than
 * pasted in, and album art is the one place where that rule has to bend to a
 * bitmap anyway, so the frame and bar graph are drawn and only the art is loaded.
 */
@Composable
fun NowPlayingPanel(
    nowPlaying: NowPlaying?,
    sourceLabel: String,
    onEmptyAction: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Color(0xFF0E0F11),
) {
    Column(modifier.fillMaxSize().background(background).padding(20.dp)) {
        Text(sourceLabel, style = mono(11, Dim, ls = 2))
        Spacer(Modifier.height(10.dp))

        if (nowPlaying == null) {
            EmptyState(onEmptyAction)
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Bars, because that is what a 2004 handset would have shown. Nothing
            // is actually decoded here: the point is the indicator, not the art.
            Canvas(Modifier.size(52.dp).background(Steel, RoundedCornerShape(6.dp))) {
                val bars = 7
                val gap = size.width / (bars * 2f)
                val bw = gap
                for (i in 0 until bars) {
                    val h = size.height * (0.28f + 0.52f * ((i % 3) / 2f))
                    drawRect(
                        color = if (nowPlaying.playing) Amber else AmberDim,
                        topLeft = Offset(
                            gap + i * (bw + gap),
                            (size.height - h) / 2f,
                        ),
                        size = Size(bw, h),
                    )
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    nowPlaying.title,
                    style = mono(15, Ink, ls = 0),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (nowPlaying.artist.isNotBlank()) {
                    Text(
                        nowPlaying.artist,
                        style = mono(11, AmberDim, ls = 0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                if (nowPlaying.playing) "PLAYING" else "PAUSED",
                style = mono(10, if (nowPlaying.playing) Green else Dim, ls = 1),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Progress bar, drawn so the ticks match the rest of the deck's line work.
        Canvas(Modifier.fillMaxWidth().height(10.dp)) {
            val y = size.height / 2f
            drawLine(Steel, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
            drawLine(
                Amber,
                Offset(0f, y),
                Offset(size.width * nowPlaying.progress(), y),
                strokeWidth = 3f,
            )
        }

        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nowPlaying.positionLabel(), style = mono(10, Dim, ls = 1))
            if (nowPlaying.durationMs > 0) Text(nowPlaying.durationLabel(), style = mono(10, Dim, ls = 1))
        }
    }
}

@Composable
private fun EmptyState(onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("NOTHING PLAYING", style = mono(15, Amber, FontWeight.Bold, 3))
        Spacer(Modifier.height(10.dp))
        Text(
            "Start music or video in any app and it appears here.",
            style = mono(11, Dim, ls = 1),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .background(Amber, RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text("BACK", style = mono(12, Color(0xFF0A0A0B), FontWeight.Bold, 3))
        }
    }
}