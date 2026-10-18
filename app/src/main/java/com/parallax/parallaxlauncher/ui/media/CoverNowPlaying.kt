package com.parallax.parallaxlauncher.ui.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Now Playing on a closed fold's cover display.
 *
 * ## Deliberately passive
 *
 * Nothing here accepts a touch, and there is **no on-screen exit**. The way out
 * is physical: unfold the device, or tap the V3i's hinge. A cover screen is a
 * few centimetres across and no player is meaningfully driven with a fingertip
 * on one; the real RAZR's external display was equally inert.
 *
 * There is a trap worth naming, because it looked like a missing feature rather
 * than a missing key. `*` was originally specified as the exit -- "like the web
 * button when locked". It cannot work: a closed flip surface has no keypad.
 * `*` lives on the lower shell, which a closed flip hides entirely, so on both
 * the Z Flip cover and the V3i's closed shell the key simply does not exist.
 * The exit is the fold and nothing else.
 *
 * ## Why this is not the V3i cover panel
 *
 * The V3i cover showed the clock, operator and message previews. This one shows
 * the track, because on a Z Flip the user has folded the device shut *because*
 * they are listening to something, and a clock is the one thing they already
 * have. Same iTunes chrome as the full player so it reads as the same app.
 */
@Composable
fun CoverNowPlaying(
    transport: Transport,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xFF07070A))) {
        Canvas(Modifier.fillMaxSize()) { brushedFace(radius = size.minDimension * 0.05f) }

        if (!transport.hasTrack) {
            Column(
                Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "iTunes",
                    color = ItunesSkin.LcdInk,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.padding(2.dp))
                Text(
                    if (transport.needsPermission) {
                        "Grant notification access"
                    } else {
                        "Nothing playing"
                    },
                    color = ItunesSkin.LcdInkDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                )
            }
            return@Box
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(7.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    lcdWell(0f, 0f, size.width, size.height, radius = size.minDimension * 0.06f)
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    Text(
                        transport.title,
                        color = ItunesSkin.LcdInk,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (transport.artist.isNotBlank()) {
                        Text(
                            transport.artist,
                            color = ItunesSkin.LcdInkDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        if (transport.playing) "PLAYING" else "PAUSED",
                        color = if (transport.playing) ItunesSkin.Accent else ItunesSkin.LcdInkDim,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.padding(3.dp))

            // A passive progress bar: no scrubber semantics, because nothing
            // here can be scrubbed.
            Text(
                "${Transport.clock(transport.positionMs)}  /  ${Transport.clock(transport.durationMs)}",
                color = ItunesSkin.LcdInk,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                val y = size.height / 2f
                val inset = size.height * 0.2f
                drawLine(
                    ItunesSkin.MetalEdgeLo,
                    Offset(inset, y),
                    Offset(size.width - inset, y),
                    strokeWidth = size.height * 0.22f,
                )
                drawLine(
                    ItunesSkin.Accent,
                    Offset(inset, y),
                    Offset(inset + (size.width - 2 * inset) * transport.progress(), y),
                    strokeWidth = size.height * 0.22f,
                )
            }
        }
    }
}