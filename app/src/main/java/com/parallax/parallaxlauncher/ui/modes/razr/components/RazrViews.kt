package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.notifications.Headline
import com.parallax.parallaxlauncher.core.notifications.Intercepted
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIcon
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconGlyph
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconTints
import com.parallax.parallaxlauncher.ui.modes.razr.RazrMenuItem
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Bold humanist sans text, the closest safe stand-in for the stock Univers. */
@Composable
private fun T(
    text: String,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = weight,
        letterSpacing = (-0.1).sp,
        textAlign = align,
        maxLines = maxLines,
        modifier = modifier,
    )
}

/**
 * The status strip glyphs.
 *
 * Indicator set taken from page 46 of the manual: signal strength, GPRS, data,
 * roam, message, battery level and ring style — all drawn white on navy.
 */
@Composable
fun RazrStatusGlyphs(
    palette: RazrPalette,
    signalBars: Int,
    batteryPercent: Int,
    charging: Boolean,
    unreadMessages: Int,
    ringStyleName: String,
    modifier: Modifier = Modifier,
) {
    val ink = palette.statusInk
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
    ) {
        // Signal strength: ascending vertical bars.
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(0.5.dp),
            modifier = Modifier.height(6.dp),
        ) {
            (1..4).forEach { level ->
                Box(
                    Modifier
                        .width(1.2.dp)
                        .height((1.2f + level * 1.1f).dp)
                        .background(if (level <= signalBars) ink else ink.copy(alpha = 0.35f))
                )
            }
        }
        T("2G", ink, 8.7.sp, FontWeight.Bold)
        T("▧", ink, 8.7.sp)
        if (unreadMessages > 0) {
            T("✉", palette.badgeInk, 10.2.sp, FontWeight.Bold)
            T(unreadMessages.toString(), palette.badgeInk, 8.7.sp, FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        T(ringStyleName.take(3).uppercase(Locale.ROOT), ink, 8.sp, FontWeight.Bold)
        // Battery: vertical segments in a capsule.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .border(0.7.dp, ink, RoundedCornerShape(1.dp))
                    .padding(1.dp)
            ) {
                val filled = (batteryPercent.coerceIn(0, 100) / 34 + 1).coerceIn(1, 3)
                Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .width(1.3.dp)
                                .height(4.5.dp)
                                .background(
                                    when {
                                        i >= filled -> ink.copy(alpha = 0.3f)
                                        charging -> Color(0xFF7DFF9E)
                                        else -> ink
                                    }
                                )
                        )
                    }
                }
            }
            Box(Modifier.width(0.8.dp).height(2.5.dp).background(ink))
        }
    }
}

/**
 * Stand-by home screen.
 *
 * Layout follows the real handset as described in the user guide and confirmed
 * against reviews: status strip, then the operator name and date, then the row
 * of four feature icons bound to the navigation key, with the clock low and
 * large, and the soft-key bar along the very bottom.
 */
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
                        0f to Color.White.copy(alpha = 0.10f),
                        0.5f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.22f),
                    )
                )
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Operator and date.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("MOTOROLA", palette.titleInk, 10.2.sp, FontWeight.Bold)
                T(date.uppercase(Locale.ROOT), palette.titleInk, 10.2.sp, FontWeight.Bold, TextAlign.End)
            }

            // Clock, low and large as on the original.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom,
            ) {
                T(time, palette.titleInk, 49.3.sp, FontWeight.Bold)
                if (amPm.isNotBlank()) {
                    T(amPm, palette.titleInk, 13.sp, FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                }
            }

            // Four feature icons bound to the navigation key directions.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf(
                    RazrIcon.PHONEBOOK to RazrIconTints.Green,
                    RazrIcon.MESSAGES to RazrIconTints.Blue,
                    RazrIcon.CALLS to RazrIconTints.Orange,
                    RazrIcon.CAMERA to RazrIconTints.Slate,
                ).forEach { (icon, tint) ->
                    Box(
                        Modifier
                            .size(19.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RazrIconGlyph(icon, tint, Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

/**
 * Nine-icon menu matrix.
 *
 * Light field, colourful pictograms, and the selection rendered the way the
 * stock handsets did it: a rounded navy badge containing the icon with its
 * label in yellow underneath.
 */
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
            .background(palette.fieldAlt)
            .padding(3.dp),
    ) {
        items.chunked(3).forEachIndexed { rowIndex, row ->
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEachIndexed { colIndex, item ->
                    val index = rowIndex * 3 + colIndex
                    IconCell(palette, item, index == safe, Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
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
    val tint = when (item.icon) {
        RazrIcon.CALLS -> RazrIconTints.Orange
        RazrIcon.MESSAGES, RazrIcon.INBOX -> RazrIconTints.Blue
        RazrIcon.PHONEBOOK -> RazrIconTints.Green
        RazrIcon.WEB_ACCESS, RazrIcon.NETWORK -> RazrIconTints.Cyan
        RazrIcon.GAMES -> RazrIconTints.Purple
        RazrIcon.MULTIMEDIA, RazrIcon.PLAYLIST -> RazrIconTints.Blue
        RazrIcon.TOOLS -> RazrIconTints.Slate
        RazrIcon.SETTINGS -> RazrIconTints.Slate
        RazrIcon.CAMERA -> RazrIconTints.Red
        RazrIcon.ALARM, RazrIcon.CALENDAR -> RazrIconTints.Amber
        RazrIcon.FILES, RazrIcon.FOLDER -> RazrIconTints.Amber
        RazrIcon.SECURITY -> RazrIconTints.Red
        RazrIcon.CALCULATOR -> RazrIconTints.Slate
        RazrIcon.VOICEMAIL -> RazrIconTints.Purple
        RazrIcon.VIDEO -> RazrIconTints.Red
        RazrIcon.CHECKLIST -> RazrIconTints.Green
        RazrIcon.HELP -> RazrIconTints.Cyan
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(1.5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) palette.badgeFill else Color.Transparent)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RazrIconGlyph(
            item.icon,
            if (selected) Color.White else tint,
            Modifier.size(if (selected) 20.dp else 19.dp),
        )
        Spacer(Modifier.height(1.dp))
        T(
            text = item.title,
            color = if (selected) palette.badgeInk else palette.ink,
            size = 9.4.sp,
            weight = if (selected) FontWeight.Bold else FontWeight.Normal,
            align = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/**
 * Scrollable selection list.
 *
 * Near-white field, bold navy labels, a small `›` chevron on each row, and a
 * full-width blue selection bar carrying white text — the stock pattern.
 */
@Composable
fun <T> RazrListScreen(
    palette: RazrPalette,
    title: String,
    entries: List<T>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    emptyText: String = "Empty",
    marker: (T) -> String? = { null },
    primary: (T) -> String,
    secondary: (T) -> String? = { null },
) {
    val safe = selectedIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, title)
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                T(emptyText, palette.inkDim, 13.sp, FontWeight.Normal, TextAlign.Center)
            }
            return@Column
        }
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(entries) { index, item ->
                val selected = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val label = primary(item)
                    T(
                        text = label,
                        color = if (selected) palette.selectInk else palette.ink,
                        size = 14.5.sp,
                        weight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    secondary(item)?.let {
                        T(it, if (selected) palette.selectInk else palette.inkDim, 11.6.sp)
                    }
                    marker(item)?.let {
                        T(it, if (selected) palette.selectInk else palette.alert, 13.sp, FontWeight.Bold)
                    }
                    Spacer(Modifier.width(4.dp))
                    T("›", if (selected) palette.selectInk else palette.inkDim, 15.9.sp, FontWeight.Bold)
                }
            }
        }
    }
}

/** Dialing view: the entered number large, with the send hint underneath. */
@Composable
fun RazrDialingScreen(
    palette: RazrPalette,
    buffer: String,
    notice: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
            .padding(horizontal = 7.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        T("Enter number", palette.inkDim, 13.sp, FontWeight.Normal)
        T(
            buffer.ifEmpty { "_" },
            palette.ink,
            37.7.sp,
            FontWeight.Bold,
            TextAlign.Center,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T(
                notice ?: "Send to call",
                if (notice == null) palette.selectLow else palette.alert,
                13.sp,
                FontWeight.Bold,
                TextAlign.Center,
            )
            T("Clear erases the last digit", palette.inkDim, 11.6.sp)
        }
    }
}

/** Active call screen: number, timer, live cost and status pills. */
@Composable
fun RazrInCallScreen(
    palette: RazrPalette,
    number: String,
    timer: String,
    status: String,
    perSecondLabel: String,
    perMinuteLabel: String,
    secondsChargedLabel: String,
    minutesChargedLabel: String,
    totalLabel: String,
    muted: Boolean,
    onHold: Boolean,
    speaker: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow)))
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            T(status, palette.selectInk, 13.sp, FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).padding(horizontal = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            T(number, palette.ink, 17.4.sp, FontWeight.Bold, TextAlign.Center)
            T(timer, palette.selectLow, 37.7.sp, FontWeight.Bold, TextAlign.Center)
            Spacer(Modifier.height(3.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T(perSecondLabel, palette.inkDim, 10.2.sp)
                T(secondsChargedLabel, palette.ink, 10.2.sp, FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T(perMinuteLabel, palette.inkDim, 10.2.sp)
                T(minutesChargedLabel, palette.ink, 10.2.sp, FontWeight.Bold)
            }
            Spacer(Modifier.height(2.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.fieldAlt)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                T("Total", palette.ink, 11.6.sp, FontWeight.Bold)
                T(totalLabel, palette.selectLow, 13.sp, FontWeight.Bold)
            }
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Pill(palette, "Mute", muted)
                Pill(palette, "Hold", onHold)
                Pill(palette, "Spkr", speaker)
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun Pill(palette: RazrPalette, label: String, on: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (on) palette.selectTop else palette.fieldAlt)
            .border(0.7.dp, palette.inkDim, RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        T(
            label,
            if (on) palette.selectInk else palette.inkDim,
            10.2.sp,
            FontWeight.Bold,
        )
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
        modifier
            .fillMaxSize()
            .background(palette.field)
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        T("Incoming call", palette.alert, 17.4.sp, FontWeight.Bold, TextAlign.Center)
        T(number, palette.ink, 24.6.sp, FontWeight.Bold, TextAlign.Center)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("Send answers", palette.selectLow, 13.sp, FontWeight.Bold, TextAlign.Center)
            T("Power declines", palette.alert, 13.sp, FontWeight.Bold, TextAlign.Center)
        }
    }
}

/** Unlock prompt shown before handing off to the Android credential screen. */
@Composable
fun RazrUnlockScreen(
    palette: RazrPalette,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("MOTOROLA", palette.inkDim, 11.6.sp, FontWeight.Normal)
            T("RAZR V3i", palette.ink, 21.8.sp, FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("Phone locked", palette.alert, 15.9.sp, FontWeight.Bold, TextAlign.Center)
            T(
                message ?: "Press * or tap to unlock",
                palette.inkDim,
                11.6.sp,
                FontWeight.Normal,
                TextAlign.Center,
            )
        }
        T("Emergency calls only", palette.inkDim, 10.2.sp, FontWeight.Normal, TextAlign.Center)
    }
}

/** Ring-style picker. */
@Composable
fun RazrRingStyleScreen(
    palette: RazrPalette,
    names: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, names.lastIndex.coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Ring Styles")
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(names) { index, name ->
                val selected = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    T(
                        name,
                        if (selected) palette.selectInk else palette.ink,
                        14.5.sp,
                        if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    T("›", if (selected) palette.selectInk else palette.inkDim, 15.9.sp, FontWeight.Bold)
                }
            }
        }
    }
}

/** Four-finish theme picker. */
@Composable
fun RazrThemeScreen(
    palette: RazrPalette,
    names: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, names.lastIndex.coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Themes")
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(names) { index, name ->
                val selected = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.titleLow)
                            .border(0.7.dp, palette.inkDim, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(6.dp))
                    T(
                        name,
                        if (selected) palette.selectInk else palette.ink,
                        14.5.sp,
                        if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    T("›", if (selected) palette.selectInk else palette.inkDim, 15.9.sp, FontWeight.Bold)
                }
            }
        }
    }
}

/** Message inbox, with the manual's unread/read markers. */
@Composable
fun RazrInboxList(
    palette: RazrPalette,
    messages: List<Headline>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.ROOT) }
    val safe = selectedIndex.coerceIn(0, (messages.size - 1).coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Message Inbox")
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                T(
                    "No messages\n\nEnable notification access\nin Android settings",
                    palette.inkDim,
                    11.6.sp,
                    FontWeight.Normal,
                    TextAlign.Center,
                )
            }
            return@Column
        }
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(messages) { index, item ->
                val selected = index == safe
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 5.dp, vertical = 4.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (item.isMessage) RazrIconTints.Blue else RazrIconTints.Slate),
                            contentAlignment = Alignment.Center,
                        ) {
                            RazrIconGlyph(
                                if (item.isMessage) RazrIcon.MESSAGES else RazrIcon.HELP,
                                Color.White,
                                Modifier.size(8.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        T(
                            item.sender,
                            if (selected) palette.selectInk else palette.ink,
                            13.sp,
                            FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        T(
                            timeFmt.format(Date(item.postTime)),
                            if (selected) palette.selectInk else palette.inkDim,
                            10.2.sp,
                        )
                    }
                    if (item.text.isNotBlank()) {
                        T(
                            item.text,
                            if (selected) palette.selectInk else palette.inkDim,
                            11.6.sp,
                            maxLines = 2,
                            modifier = Modifier.padding(start = 15.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Outer-face panel shown while the flip is closed.
 *
 * This renders inside the true 96 x 80 cover display, so it has to be terse:
 * the wallpaper, a large clock, and a single-line alert summary. Anything
 * longer is unreadable at that size, so the message list lives on the inner
 * panel instead.
 */
@Composable
fun RazrCoverPanel(
    palette: RazrPalette,
    wallpaperRes: Int,
    time: String,
    amPm: String,
    date: String,
    alert: String?,
    unread: Int,
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
                        0f to Color.Black.copy(alpha = 0.15f),
                        1f to Color.Black.copy(alpha = 0.45f),
                    )
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(palette.statusLow.copy(alpha = 0.85f))
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                T("2G", Color.White, 8.7.sp, FontWeight.Bold)
                if (unread > 0) T("✉$unread", Color(0xFFFFD34D), 8.7.sp, FontWeight.Bold)
                T("▮", Color.White, 8.7.sp, FontWeight.Bold)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                T(time, Color.White, 27.6.sp, FontWeight.Bold)
                if (amPm.isNotBlank()) {
                    T(amPm, Color.White, 10.2.sp, FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp, start = 1.dp))
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            alert != null -> Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            missedCalls > 0 -> SolidColor(palette.alert)
                            else -> SolidColor(Color.Black.copy(alpha = 0.45f))
                        }
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp),
            ) {
                T(
                    alert
                        ?: if (missedCalls > 0) "$missedCalls MISSED CALLS"
                        else date.uppercase(Locale.ROOT),
                    if (alert != null || missedCalls > 0) Color.White else Color.White.copy(alpha = 0.85f),
                    9.4.sp,
                    FontWeight.Bold,
                    TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Cover-screen lock notice: tapping hands off to the Android lock screen. */
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
            .background(palette.field)
            .clickable(onClick = onUnlock)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("MOTOROLA", palette.inkDim, 11.6.sp, FontWeight.Normal)
            T("RAZR V3i", palette.ink, 21.8.sp, FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            T("PHONE LOCKED", palette.alert, 15.9.sp, FontWeight.Bold, TextAlign.Center)
            T("UNLOCK MODE REQUIRED", palette.ink, 11.6.sp, FontWeight.Bold, TextAlign.Center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("TAP HERE OR PRESS *", palette.selectLow, 11.6.sp, FontWeight.Bold, TextAlign.Center)
            T("SWIPE UP AND ENTER YOUR PIN", palette.inkDim, 10.2.sp, FontWeight.Normal, TextAlign.Center)
        }
    }
}

/** Messages sub-menu, showing the routing rule Mode 7 applies. */
@Composable
fun RazrMessageMenu(
    palette: RazrPalette,
    entries: List<Pair<RazrIcon, String>>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Messages")
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(entries) { index, (icon, label) ->
                val selected = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selected) palette.selectInk else palette.fieldAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        RazrIconGlyph(icon, if (selected) palette.selectLow else RazrIconTints.Blue, Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    T(
                        label,
                        if (selected) palette.selectInk else palette.ink,
                        14.5.sp,
                        if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    T("›", if (selected) palette.selectInk else palette.inkDim, 15.9.sp, FontWeight.Bold)
                }
            }
        }
        // Which routing rule is currently in force.
        Row(
            Modifier
                .fillMaxWidth()
                .background(palette.fieldAlt)
                .padding(horizontal = 5.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            T("In-app alert here", palette.selectLow, 9.4.sp, FontWeight.Bold)
            T("System alert elsewhere", palette.inkDim, 9.4.sp, FontWeight.Bold, TextAlign.End)
        }
    }
}

/** Calculator page, wired to live call-cost and battery telemetry. */
@Composable
fun RazrCalculatorPanel(
    palette: RazrPalette,
    rateText: String,
    costText: String,
    callSeconds: Int,
    batteryPct: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Calculator")
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("Rate", palette.inkDim, 13.sp); T(rateText, palette.ink, 13.sp, FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("Current call", palette.inkDim, 13.sp); T(costText, palette.ink, 13.sp, FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("Talk time", palette.inkDim, 13.sp)
                T(
                    String.format(Locale.ROOT, "%02d:%02d", callSeconds / 60, callSeconds % 60),
                    palette.ink,
                    13.sp,
                    FontWeight.Bold,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("Battery", palette.inkDim, 13.sp); T("$batteryPct%", palette.ink, 13.sp, FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            T("Tap digits to dial, centre to call", palette.inkDim, 11.6.sp)
        }
    }
}

/** Phone Status page, carrying the figures from the spec sheet. */
@Composable
fun RazrAboutScreen(palette: RazrPalette, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        RazrTitleBar(palette, "Phone Status")
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            listOf(
                "Model" to "RAZR V3i",
                "Size" to "98 x 53 x 13.9 mm",
                "Weight" to "100 g",
                "Display" to "2.2 in 176 x 220",
                "External" to "96 x 80 CSTN",
                "Memory" to "10 MB + microSD",
                "Camera" to "1.23 MP",
                "Network" to "Quad-band GSM 2G",
                "Bluetooth" to "1.2",
                "Connector" to "mini-USB 2.0",
                "Battery" to "710 mAh Li-Ion",
                "Talk time" to "3 h 30 min",
                "Standby" to "200 hours",
            ).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T(label, palette.inkDim, 11.6.sp)
                    T(value, palette.ink, 11.6.sp, FontWeight.Bold)
                }
            }
        }
    }
}

/** Compact banner used for call summaries and ring-style changes. */
@Composable
fun RazrToast(
    palette: RazrPalette,
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow)))
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        T(text, palette.selectInk, 11.6.sp, FontWeight.Bold, TextAlign.Center, maxLines = 3)
    }
}

/** Small empty-state slot used by screens with nothing to show. */
@Composable
fun RazrEmptyState(palette: RazrPalette, text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        T(text, palette.inkDim, 13.sp, FontWeight.Normal, TextAlign.Center)
    }
}

/** Convenience for a plain content field. */
@Composable
fun RazrField(palette: RazrPalette, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(palette.field)) { content() }
}
