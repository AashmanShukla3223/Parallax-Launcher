package com.parallax.parallaxlauncher.ui.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Now Playing, drawn as iTunes 4/5 on Windows.
 *
 * Sits in Mode 7 because the V3i shipped with iTunes — see [ItunesSkin] for why
 * this palette is the one exception to the handset's own.
 *
 * ## Controls
 *
 * The panel itself is display-first. Everything is reachable from the deck:
 *
 * | Key            | Action              |
 * |----------------|---------------------|
 * | LEFT / RIGHT   | seek -5s / +5s      |
 * | SELECT (centre)| play / pause        |
 * | UP / DOWN      | previous / next     |
 * | REPEAT (soft L)| cycle off/all/one   |
 * | SHUFFLE (soft R)| toggle shuffle      |
 *
 * The deck is used rather than on-screen taps because the panel's touch
 * surface is currently dead — the subpixel Canvas that draws it is the last
 * child of [RazrPixelScreen] and covers everything beneath. The buttons are
 * still drawn and still carry click handlers, so they start working the day
 * that bug is fixed without another change here.
 */
@Composable
fun ItunesNowPlaying(
    transport: Transport,
    modifier: Modifier = Modifier,
    onScrubFraction: (Float) -> Unit = {},
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
) {
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { brushedFace(radius = size.minDimension * 0.045f) }

        if (!transport.hasSession) {
            EmptyLcd(transport.needsPermission)
            return@Box
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Lcd(
                modifier = Modifier.weight(1f),
                transport = transport,
                onScrubFraction = onScrubFraction,
            )
            TransportRow(
                transport = transport,
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onCycleRepeat = onCycleRepeat,
                onToggleShuffle = onToggleShuffle,
            )
        }
    }
}

/**
 * The sunken readout: track, artist, album, clock and scrubber.
 *
 * Takes a [modifier] rather than calling `Modifier.weight` itself. `weight` is
 * a member of `ColumnScope`, available only lexically inside a Column's content
 * lambda — a separate composable called from one does not inherit it. The
 * caller, which *is* inside the Column, passes the weighted modifier in.
 */
@Composable
private fun Lcd(
    modifier: Modifier,
    transport: Transport,
    onScrubFraction: (Float) -> Unit,
) {
    Box(
        modifier.fillMaxWidth()
    ) {
        Canvas(Modifier.fillMaxSize()) {
            lcdWell(0f, 0f, size.width, size.height, radius = size.minDimension * 0.07f)
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Text(
                transport.title,
                color = ItunesSkin.LcdInk,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(transport.artist, transport.album)
                    .filter { it.isNotBlank() }
                    .joinToString(" — ")
                    .ifBlank { "Unknown artist" },
                color = ItunesSkin.LcdInkDim,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.weight(1f))

            // Elapsed on the left, *remaining* on the right. iTunes showed the
            // time left, not the total, and putting the total there made the
            // readout feel wrong even though the numbers were right.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LcdText(Transport.clock(transport.positionMs))
                LcdText("-" + Transport.clock(transport.remainingMs()))
            }
            Spacer(Modifier.height(3.dp))
            Scrubber(transport.progress(), onScrubFraction)
        }
    }
}

@Composable
private fun LcdText(text: String) {
    Text(
        text,
        color = ItunesSkin.LcdInk,
        fontSize = 10.sp,
        // Monospaced so the clock does not jitter sideways twice a second as
        // the digits change width.
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )
}

/**
 * The scrubber.
 *
 * Tapping or dragging maps x to a fraction and reports it; the caller seeks.
 * Track and knob are drawn, not composables, so the geometry stays in one place.
 */
@Composable
private fun Scrubber(progress: Float, onScrubFraction: (Float) -> Unit) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(11.dp)
            .pointerInput(Unit) {
                detectTapGestures { o -> onScrubFraction((o.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {},
                    onDragCancel = {},
                ) { change, _ ->
                    onScrubFraction((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
    ) {
        val y = size.height / 2f
        val inset = size.height * 0.22f
        // Groove.
        drawLine(
            ItunesSkin.LcdEdge.copy(alpha = 0.55f),
            Offset(inset, y),
            Offset(size.width - inset, y),
            strokeWidth = size.height * 0.24f,
        )
        // Played portion, in iTunes blue so it reads at a glance.
        drawLine(
            ItunesSkin.Accent,
            Offset(inset, y),
            Offset(inset + (size.width - 2 * inset) * progress, y),
            strokeWidth = size.height * 0.24f,
        )
        // Knob.
        drawCircle(
            ItunesSkin.MetalHi,
            radius = size.height * 0.44f,
            center = Offset(inset + (size.width - 2 * inset) * progress, y),
        )
        drawCircle(
            ItunesSkin.MetalEdgeLo,
            radius = size.height * 0.44f,
            center = Offset(inset + (size.width - 2 * inset) * progress, y),
            style = Stroke(width = 1f),
        )
    }
}

/** Round chrome transport row. */
@Composable
private fun TransportRow(
    transport: Transport,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(Modifier.size(34.dp), active = transport.shuffle, onClick = onToggleShuffle) {
            glyphShuffle(it)
        }
        Button(Modifier.size(34.dp), active = false, onClick = onPrevious) {
            glyphSkipBack(it)
        }
        Button(Modifier.size(38.dp), active = false, onClick = onPlayPause) {
            if (transport.playing) glyphPause(it) else glyphPlay(it)
        }
        Button(Modifier.size(34.dp), active = false, onClick = onNext) {
            glyphSkipForward(it)
        }
        Button(Modifier.size(34.dp), active = transport.repeat != RepeatMode.OFF, onClick = onCycleRepeat) {
            if (transport.repeat == RepeatMode.ONE) {
                glyphRepeatOne(it)
            } else {
                glyphRepeat(it)
            }
        }
    }
}

/**
 * One button: chrome face, glyph, and a click target.
 *
 * The glyph lambda runs inside a translated DrawScope so it can work in a
 * -1..1 space rather than recomputing the button centre.
 */
@Composable
private fun Button(
    modifier: Modifier,
    active: Boolean,
    onClick: () -> Unit,
    glyph: androidx.compose.ui.graphics.drawscope.DrawScope.(Float) -> Unit,
) {
    Box(
        modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            itunesButton(
                cx = size.width / 2f,
                cy = size.height / 2f,
                radius = size.minDimension / 2f,
                active = active,
                glyph = glyph,
            )
        }
    }
}

/** Nothing playing, or permission missing — both rendered inside the LCD. */
@Composable
private fun EmptyLcd(needsPermission: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(7.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                lcdWell(0f, 0f, size.width, size.height, radius = size.minDimension * 0.07f)
            }
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (needsPermission) "NOTIFICATION ACCESS" else "iTunes",
                    color = ItunesSkin.LcdInk,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (needsPermission) {
                        "Grant notification access so Parallax can see what is playing."
                    } else {
                        "Play something and it shows up here."
                    },
                    color = ItunesSkin.LcdInkDim,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                )
            }
        }
    }
}