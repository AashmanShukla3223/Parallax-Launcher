package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.notifications.Headline
import com.parallax.parallaxlauncher.core.notifications.Intercepted
import com.parallax.parallaxlauncher.ui.modes.razr.RazrMenuItem
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Wallpaper-backed home screen: clock, date, wallpaper credit, soft-key hints. */
@Composable
fun RazrHomeScreen(
    palette: RazrPalette,
    wallpaperRes: Int,
    time: String,
    amPm: String,
    date: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(wallpaperRes),
            contentDescription = "RAZR wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.30f),
                        0.42f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.35f),
                    )
                )
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: wallpaper name + menu indicator, per the manual's home screen.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InkText("WALLPAPER", palette, 6.sp, FontWeight.Normal)
                InkText("M", palette, 8.sp, FontWeight.Black)
            }

            // Centre: clock.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    InkText(time, palette, 26.sp, FontWeight.Black)
                    Spacer(Modifier.size(3.dp))
                    InkText(amPm, palette, 8.sp, FontWeight.Bold)
                }
                InkText(date.uppercase(Locale.ROOT), palette, 7.sp, FontWeight.Bold)
            }

            InkText("MENU", palette, 6.sp, FontWeight.Bold)
        }
    }
}

/** Grid of menu icons with the highlighted row and the centre-select caption. */
@Composable
fun RazrIconGrid(
    palette: RazrPalette,
    items: List<RazrMenuItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(palette.ink)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            InkText(items[safe].title.uppercase(Locale.ROOT), palette, 8.sp, FontWeight.Black, palette.lcdBacklight)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (row in items.indices step 3) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (col in 0 until 3) {
                        val idx = row + col
                        if (idx >= items.size) {
                            Spacer(Modifier.weight(1f))
                            continue
                        }
                        IconCell(palette, items[idx], selected = idx == safe, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconCell(
    palette: RazrPalette,
    item: RazrMenuItem,
    selected: Boolean,
    modifier: Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (selected) palette.ink.copy(alpha = 0.14f) else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) palette.ink else Color.Transparent,
                shape = RoundedCornerShape(2.dp),
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InkText(item.glyph, palette, 13.sp, FontWeight.Bold, if (selected) palette.ink else palette.inkDim)
        InkText(
            item.title,
            palette,
            5.5.sp,
            if (selected) FontWeight.Bold else FontWeight.Normal,
            if (selected) palette.ink else palette.inkDim,
            TextAlign.Center,
        )
    }
}

/** Scrollable selection list used by apps, calls, inbox, messages, ringtones. */
@Composable
fun <T> RazrListScreen(
    palette: RazrPalette,
    title: String,
    entries: List<T>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    emptyText: String = "EMPTY",
    primary: @Composable (T) -> String,
    /** Optional unread/read marker column. */
    marker: (T) -> String? = { null },
    secondary: @Composable (T) -> String? = { null },
) {
    val safe = selectedIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(palette.ink)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            InkText(title.uppercase(Locale.ROOT), palette, 7.5.sp, FontWeight.Black, palette.lcdBacklight)
        }
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                InkText(emptyText, palette, 6.5.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
            }
            return@Column
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(entries) { index, item ->
                val sel = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (sel) palette.ink.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InkText(
                        if (sel) "►" else " ",
                        palette,
                        6.sp,
                        FontWeight.Black,
                        if (sel) palette.ink else palette.inkDim,
                    )
                    InkText(
                        marker(item) ?: "",
                        palette,
                        6.sp,
                        FontWeight.Black,
                        palette.alert,
                    )
                    Column(Modifier.weight(1f)) {
                        InkText(primary(item), palette, 7.sp, if (sel) FontWeight.Bold else FontWeight.Normal)
                        secondary(item)?.let { InkText(it, palette, 5.5.sp, FontWeight.Normal, palette.inkDim) }
                    }
                }
            }
        }
    }
}

/** Dialing view: big entered digits plus the number pad legend. */
@Composable
fun RazrDialingScreen(
    palette: RazrPalette,
    buffer: String,
    notice: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        InkText("ENTER NUMBER", palette, 6.5.sp, FontWeight.Bold, palette.inkDim)
        InkText(buffer.ifEmpty { "_" }, palette, 17.sp, FontWeight.Black, palette.ink, TextAlign.Center)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText(
                notice ?: "SEND TO CALL",
                palette,
                6.5.sp,
                FontWeight.Bold,
                if (notice == null) palette.accent else palette.alert,
                TextAlign.Center,
            )
            InkText("CLEAR REMOVES LAST DIGIT", palette, 5.5.sp, FontWeight.Normal, palette.inkDim)
        }
    }
}

/** Active call screen: number, timer, live cost, and status pills. */
@Composable
fun RazrInCallScreen(
    palette: RazrPalette,
    number: String,
    timer: String,
    status: String,
    rate: String,
    cost: String,
    muted: Boolean,
    onHold: Boolean,
    speaker: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(palette.ink),
            contentAlignment = Alignment.Center,
        ) {
            InkText(status, palette, 7.sp, FontWeight.Black, palette.lcdBacklight)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText(number, palette, 9.sp, FontWeight.Bold, palette.ink, TextAlign.Center)
            InkText(timer, palette, 22.sp, FontWeight.Black, palette.ink)
            InkText("RATE $rate   COST $cost", palette, 6.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Pill(palette, "MUTE", muted)
            Pill(palette, "HOLD", onHold)
            Pill(palette, "SPKR", speaker)
        }
    }
}

@Composable
private fun Pill(palette: RazrPalette, label: String, on: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (on) palette.alert else Color.Transparent)
            .border(0.7.dp, if (on) palette.alert else palette.inkDim, RoundedCornerShape(2.dp))
            .padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
        InkText(label, palette, 5.5.sp, FontWeight.Bold, if (on) palette.lcdBacklight else palette.inkDim)
    }
}

/** Incoming-call screen. */
@Composable
fun RazrIncomingCallScreen(
    palette: RazrPalette,
    number: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        InkText("INCOMING CALL", palette, 8.sp, FontWeight.Black, palette.alert, TextAlign.Center)
        InkText(number, palette, 13.sp, FontWeight.Black, palette.ink, TextAlign.Center)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText("SEND ANSWERS", palette, 6.5.sp, FontWeight.Bold, palette.accent, TextAlign.Center)
            InkText("POWER DECLINES", palette, 6.5.sp, FontWeight.Bold, palette.alert, TextAlign.Center)
        }
    }
}

/** Numeric keypad entry for the 4-digit unlock code. */
@Composable
fun RazrUnlockScreen(
    palette: RazrPalette,
    entry: String,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText("MOTOROLA", palette, 6.sp, FontWeight.Normal, palette.inkDim)
            InkText("RAZR V3i", palette, 11.sp, FontWeight.Black, palette.ink)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText("ENTER UNLOCK CODE", palette, 7.sp, FontWeight.Bold, palette.inkDim)
            InkText(
                entry.padEnd(4, '_'),
                palette,
                18.sp,
                FontWeight.Black,
                palette.ink,
                TextAlign.Center,
            )
            InkText(
                error ?: "ENTER 4 DIGITS ON KEYPAD",
                palette,
                6.sp,
                FontWeight.Bold,
                if (error == null) palette.inkDim else palette.alert,
                TextAlign.Center,
            )
        }
        InkText("EMERGENCY CALLS ONLY", palette, 5.5.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
    }
}

/** Cover-display clock, used whenever the flip is closed. */
@Composable
fun RazrCoverHome(
    palette: RazrPalette,
    time: String,
    date: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InkText("2G", palette, 5.sp, FontWeight.Bold)
            InkText("▮", palette, 5.sp, FontWeight.Bold)
        }
        InkText(time, palette, 14.sp, FontWeight.Black, palette.ink, TextAlign.Center)
        InkText(date.uppercase(Locale.ROOT), palette, 5.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
    }
}

/** Cover-display event view: shows missed calls / new messages per the manual. */
@Composable
fun RazrCoverEvent(
    palette: RazrPalette,
    headline: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        InkText(headline.uppercase(Locale.ROOT), palette, 6.5.sp, FontWeight.Black, palette.alert, TextAlign.Center)
        InkText(detail.take(22), palette, 5.5.sp, FontWeight.Normal, palette.ink, TextAlign.Center)
    }
}

/**
 * The outer-face panel shown while the flip is closed. With the inner panel
 * removed, this lower shell carries everything still worth seeing: the
 * wallpaper, the clock, and any notification or message waiting for the user.
 */
@Composable
fun RazrCoverPanel(
    palette: RazrPalette,
    wallpaperRes: Int,
    time: String,
    date: String,
    intercepted: Intercepted?,
    unread: Int,
    messages: List<Headline>,
    missedCalls: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(wallpaperRes),
            contentDescription = "RAZR wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.35f),
                        0.45f to Color.Black.copy(alpha = 0.15f),
                        1f to Color.Black.copy(alpha = 0.45f),
                    )
                )
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Clock + date.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                InkText(time, palette, 30.sp, FontWeight.Black, Color.White, TextAlign.Center)
                InkText(date.uppercase(Locale.ROOT), palette, 8.sp, FontWeight.Bold, palette.lcdBacklight, TextAlign.Center)
            }

            // Live notification.
            intercepted?.let { current ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.bezel)
                        .border(1.dp, palette.groove, RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 4.dp)
                ) {
                    val isMessage = current.headline.isMessage
                    Text(
                        text = when {
                            isMessage && current.count == 1 -> "1 NEW MESSAGE RECEIVED"
                            isMessage -> "${current.count} NEW MESSAGES RECEIVED"
                            current.count == 1 -> "1 NEW NOTIFICATION"
                            else -> "${current.count} NEW NOTIFICATIONS"
                        },
                        color = palette.grooveGlow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                    )
                    InkText(current.headline.sender, palette, 7.sp, FontWeight.Bold, palette.lcdBacklight)
                    if (current.headline.text.isNotBlank()) {
                        InkText(current.headline.text, palette, 6.sp, FontWeight.Normal, palette.lcdBacklight.copy(alpha = 0.8f))
                    }
                }
            }

            // Message inbox preview.
            if (messages.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    InkText(
                        "MESSAGES",
                        palette,
                        7.sp,
                        FontWeight.Black,
                        palette.lcdBacklight,
                    )
                    messages.take(4).forEach { msg ->
                        Row(Modifier.fillMaxWidth()) {
                            InkText(
                                if (msg.isMessage) "✉" else "◉",
                                palette,
                                7.sp,
                                FontWeight.Black,
                                palette.grooveGlow,
                            )
                            Column(Modifier.weight(1f)) {
                                InkText(msg.sender, palette, 6.5.sp, FontWeight.Bold, Color.White)
                                InkText(msg.text.take(38), palette, 5.8.sp, FontWeight.Normal, Color.White.copy(alpha = 0.75f))
                            }
                        }
                    }
                    if (unread > 0) {
                        InkText(
                            "$unread UNREAD",
                            palette,
                            6.sp,
                            FontWeight.Black,
                            palette.alert,
                        )
                    }
                }
            }

            if (missedCalls > 0) {
                InkText(
                    "$missedCalls MISSED CALLS",
                    palette,
                    7.sp,
                    FontWeight.Black,
                    palette.alert,
                    TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))
            InkText(
                "TAP TO OPEN FLIP",
                palette,
                7.sp,
                FontWeight.Bold,
                Color.White.copy(alpha = 0.8f),
                TextAlign.Center,
            )
        }
    }
}

/** Cover-screen lock notice: opening the flip or tapping hands off to Android. */
@Composable
fun RazrCoverLocked(
    palette: RazrPalette,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(3.dp))
            .background(palette.bezel)
            .border(1.dp, palette.groove, RoundedCornerShape(3.dp))
            .clickable(onClick = onUnlock)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText("MOTOROLA", palette, 7.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
            InkText("RAZR V3i", palette, 13.sp, FontWeight.Black, palette.lcdBacklight, TextAlign.Center)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            InkText("PHONE LOCKED", palette, 9.sp, FontWeight.Black, palette.alert, TextAlign.Center)
            InkText(
                "UNLOCK MODE REQUIRED",
                palette,
                7.sp,
                FontWeight.Bold,
                palette.lcdBacklight,
                TextAlign.Center,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InkText(
                "TAP HERE OR PRESS * TO OPEN",
                palette,
                7.sp,
                FontWeight.Black,
                palette.grooveGlow,
                TextAlign.Center,
            )
            InkText(
                "SWIPE UP AND ENTER YOUR PIN",
                palette,
                6.sp,
                FontWeight.Normal,
                palette.lcdBacklight.copy(alpha = 0.8f),
                TextAlign.Center,
            )
        }
    }
}

/** Ring-style picker (Settings > Ring Styles > Style). */
@Composable
fun RazrRingStyleScreen(
    palette: RazrPalette,
    styles: List<Pair<String, String>>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, styles.lastIndex.coerceAtLeast(0))
    Column(
        modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        InkText("RING STYLES", palette, 8.sp, FontWeight.Black, palette.ink, TextAlign.Center)
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            styles.forEachIndexed { index, (glyph, name) ->
                val sel = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (sel) palette.ink.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InkText(if (sel) "►" else " ", palette, 6.sp, FontWeight.Black)
                    InkText(glyph, palette, 8.sp, FontWeight.Bold, palette.inkDim)
                    Spacer(Modifier.size(4.dp))
                    InkText(name.uppercase(Locale.ROOT), palette, 7.sp, if (sel) FontWeight.Black else FontWeight.Normal)
                }
            }
        }
        InkText("STYLE DETAIL SELECTS ALERTS", palette, 5.5.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
    }
}

/** Four-skin theme picker. */
@Composable
fun RazrThemeScreen(
    palette: RazrPalette,
    names: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, names.lastIndex.coerceAtLeast(0))
    Column(
        modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        InkText("THEMES", palette, 8.sp, FontWeight.Black, palette.ink, TextAlign.Center)
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            names.forEachIndexed { index, name ->
                val sel = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (sel) palette.ink.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InkText(if (sel) "►" else " ", palette, 6.sp, FontWeight.Black)
                    InkText("▦", palette, 8.sp, FontWeight.Bold, palette.inkDim)
                    Spacer(Modifier.size(4.dp))
                    InkText(name.uppercase(Locale.ROOT), palette, 7.sp, if (sel) FontWeight.Black else FontWeight.Normal)
                }
            }
        }
        InkText("THEME SETS SKIN AND SOUNDS", palette, 5.5.sp, FontWeight.Normal, palette.inkDim, TextAlign.Center)
    }
}

/** Call-times breakdown with lifetime totals and the master-reset action. */
@Composable
fun RazrCallTimesScreen(
    palette: RazrPalette,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(palette.ink),
            contentAlignment = Alignment.Center,
        ) {
            InkText("CALL TIMES", palette, 7.5.sp, FontWeight.Black, palette.lcdBacklight)
        }
        rows.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InkText(label.uppercase(Locale.ROOT), palette, 6.sp, FontWeight.Normal, palette.inkDim)
                InkText(value, palette, 6.sp, FontWeight.Bold, palette.ink)
            }
        }
        Spacer(Modifier.height(2.dp))
        InkText("MASTER RESET CLEARS TIMERS", palette, 5.5.sp, FontWeight.Normal, palette.inkDim)
    }
}

/** Message inbox preview rows (mirrors the manual's read/unread markers). */
@Composable
fun RazrInboxList(
    palette: RazrPalette,
    messages: List<Headline>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.ROOT) }
    val safe = selectedIndex.coerceIn(0, (messages.size - 1).coerceAtLeast(0))
    Column(modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 3.dp)) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(palette.ink),
            contentAlignment = Alignment.Center,
        ) {
            InkText("MESSAGE INBOX", palette, 7.5.sp, FontWeight.Black, palette.lcdBacklight)
        }
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                InkText(
                    "NO MESSAGES\n\nENABLE NOTIFICATION ACCESS\nIN ANDROID SETTINGS",
                    palette,
                    6.sp,
                    FontWeight.Normal,
                    palette.inkDim,
                    TextAlign.Center,
                )
            }
            return@Column
        }
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(messages) { index, item ->
                val sel = index == safe
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (sel) palette.ink.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InkText(if (sel) "► " else "  ", palette, 6.sp, FontWeight.Black)
                        InkText(
                            item.sender,
                            palette,
                            6.5.sp,
                            if (sel) FontWeight.Bold else FontWeight.Normal,
                            palette.ink,
                            modifier = Modifier.weight(1f),
                        )
                        InkText(timeFmt.format(Date(item.postTime)), palette, 5.5.sp, FontWeight.Normal, palette.inkDim)
                    }
                    InkText(item.text.take(46), palette, 5.8.sp, FontWeight.Normal, palette.inkDim)
                }
            }
        }
    }
}

/** Simple message composer preview showing the live character counter. */
@Composable
fun RazrComposeScreen(
    palette: RazrPalette,
    body: String,
    recipient: String,
    mode: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InkText("TO", palette, 6.sp, FontWeight.Bold, palette.inkDim)
            InkText(recipient.ifEmpty { "[New Number]" }, palette, 6.sp, FontWeight.Normal, palette.ink)
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .border(0.7.dp, palette.inkDim, RoundedCornerShape(2.dp))
                .padding(3.dp)
        ) {
            InkText(
                body.ifEmpty { "Tap keys to type. # switches entry mode." },
                palette,
                6.sp,
                FontWeight.Normal,
                if (body.isEmpty()) palette.inkDim else palette.ink,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InkText(mode, palette, 5.5.sp, FontWeight.Bold, palette.accent)
            InkText("${body.length}/450", palette, 5.5.sp, FontWeight.Bold, palette.inkDim)
        }
    }
}

@Composable
private fun InkText(
    text: String,
    palette: RazrPalette,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    color: Color? = null,
    align: TextAlign? = null,
    modifier: Modifier? = null,
) {
    Text(
        text = text,
        color = color ?: palette.ink,
        fontFamily = FontFamily.Monospace,
        fontSize = size,
        fontWeight = weight,
        textAlign = align ?: TextAlign.Start,
        maxLines = 4,
        modifier = modifier ?: Modifier,
    )
}

/** About screen carrying the physical dimensions from the spec sheet. */
@Composable
fun RazrAboutScreen(palette: RazrPalette, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)).background(palette.ink),
            contentAlignment = Alignment.Center,
        ) {
            InkText("PHONE STATUS", palette, 7.5.sp, FontWeight.Black, palette.lcdBacklight)
        }
        listOf(
            "MODEL      RAZR V3i",
            "SIZE       98 x 53 x 13.9 mm",
            "WEIGHT     100 g",
            "DISPLAY    2.2 in 176 x 220",
            "EXTERNAL   96 x 80 CSTN",
            "MEMORY     10 MB  + microSD",
            "CAMERA     1.23 MP",
            "NETWORK    QUAD-BAND GSM 2G",
            "BLUETOOTH  1.2",
            "CONNECTOR  MINI-USB 2.0",
            "BATTERY    710 mAh LI-ION",
            "TALK       3 H 30 MIN",
            "STANDBY    200 HOURS",
        ).forEach {
            InkText(it, palette, 6.sp, FontWeight.Normal, palette.ink)
        }
    }
}