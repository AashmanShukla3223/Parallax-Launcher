package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.notifications.Headline
import com.parallax.parallaxlauncher.core.notifications.Intercepted
import com.parallax.parallaxlauncher.core.telecom.SecretCodes
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIcon
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconGlyph
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconTints
import com.parallax.parallaxlauncher.ui.modes.razr.RazrMenuItem
import com.parallax.parallaxlauncher.ui.modes.razr.RazrOperators
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bold humanist sans text, the closest safe stand-in for the stock Univers.
 *
 * [family] overrides the face for the rare places that need it — engineering
 * codes are monospaced so the `*` and `#` line up column-wise down the list.
 * The negative letter spacing is a tightening for the sans face and is dropped
 * for monospace, where uniform advance is the whole point.
 */
@Composable
private fun T(
    text: String,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    family: FontFamily? = null,
) {
    Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = weight,
        // Default rather than SansSerif when unset: these screens have always
        // inherited the platform face via LocalTextStyle, and pinning a family
        // here would quietly restyle every one of them.
        fontFamily = family ?: FontFamily.Default,
        letterSpacing = if (family == null) (-0.1).sp else TextUnit.Unspecified,
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
    operatorName: String = RazrOperators.UNKNOWN_OPERATOR,
    noSim: Boolean = false,
    noService: Boolean = false,
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
        // The stock strip prints the network operator, not the technology.
        // On a 2G handset that is the legacy network name, so "Jio" subscribers
        // read "Reliance Communications" here.
        // Without a SIM the strip stops naming an operator, because there is no
        // network to name, and says the one thing still true: emergency calls
        // only. The ring-style caption is dropped to make room, since a ringer
        // profile is meaningless when there is nowhere to ring.
        if (noSim) {
            T("NO SIM - EMERG. CALLS ONLY", palette.alert, 8.sp, FontWeight.Bold, maxLines = 1)
        } else {
            // The operator is named even with no service: the handset still
            // knows whose SIM it is holding. What it cannot do is connect, so
            // the bars are empty and the gap is spelled out rather than left
            // to be misread as weak signal.
            T(operatorName.take(9).uppercase(Locale.ROOT), ink, 8.7.sp, FontWeight.Bold)
            if (noService) T("NO SVC", palette.alert, 8.sp, FontWeight.Bold, maxLines = 1)
            else T("▧", ink, 8.7.sp)
        }
        if (unreadMessages > 0) {
            T("✉", palette.badgeInk, 10.2.sp, FontWeight.Bold)
            T(unreadMessages.toString(), palette.badgeInk, 8.7.sp, FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        if (!noSim && !noService) {
            T(ringStyleName.take(3).uppercase(Locale.ROOT), ink, 8.sp, FontWeight.Bold)
        }
        // Battery: continuous 1-100% fill, red at or below 20%, green + bolt
        // while charging. Replaces the old three-segment cap, which could only
        // ever show full / half / nearly-empty.
        RazrBatteryGlyph(
            percent = batteryPercent,
            charging = charging,
            ink = ink,
        )
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
            // Date only.
            //
            // The operator used to sit here too, on the left of the date. It
            // now lives in the status strip, which is where a handset reports
            // carrier and SIM state, so repeating it here was redundant -- and
            // with no SIM it printed a carrier name right next to a "no SIM"
            // strip, which read as a contradiction.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
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
 * The main menu: the apps actually installed on the device.
 *
 * The stock V3i's nine fixed icons describe a 2005 handset, not the phone this
 * launcher is running on. This grid shows every launchable app with its real
 * icon instead, three to a row like the original menu, and scrolls when there
 * are more than fit. Selection is drawn exactly as [RazrIconGrid] draws it -- a
 * navy badge with the label underneath -- so the screen still belongs to the
 * handset.
 */
@Composable
fun RazrAppGrid(
    palette: RazrPalette,
    apps: List<AppInfo>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty()) {
        Box(
            modifier.fillMaxSize().background(palette.field),
            contentAlignment = Alignment.Center,
        ) {
            T(
                text = "NO APPS FOUND",
                color = palette.inkDim,
                size = 13.sp,
                align = TextAlign.Center,
            )
        }
        return
    }

    val safe = selectedIndex.coerceIn(0, apps.lastIndex)
    val state = rememberLazyGridState()

    // Keep the highlight on screen without scrolling on every key press: move
    // only when the selection leaves the visible window, and bring it to the
    // nearest edge rather than jumping it to the top.
    LaunchedEffect(safe) {
        val visible = state.layoutInfo.visibleItemsInfo
        if (visible.isEmpty()) {
            // First composition (or returning to the menu): the grid has not
            // been measured yet, so there is no visible window to reason about.
            // Put the remembered selection on screen before anything is drawn.
            if (safe > 0) state.scrollToItem(safe)
            return@LaunchedEffect
        }
        val first = visible.first().index
        val last = visible.last().index
        when {
            safe < first -> state.animateScrollToItem(safe)
            safe > last -> {
                // Whole rows only, so the newly visible row is not cut in half.
                val target = ((safe - (last - first + 1) + 1).coerceAtLeast(0) / 3) * 3
                state.animateScrollToItem(target)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = state,
        modifier = modifier
            .fillMaxSize()
            .background(palette.fieldAlt)
            .padding(3.dp),
    ) {
        gridItemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
            AppCell(palette, app, index == safe)
        }
    }
}

@Composable
private fun AppCell(
    palette: RazrPalette,
    app: AppInfo,
    selected: Boolean,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(1.5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) palette.badgeFill else Color.Transparent)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(app, Modifier.size(if (selected) 27.dp else 24.dp))
        Spacer(Modifier.height(1.dp))
        T(
            text = app.label,
            color = if (selected) palette.badgeInk else palette.ink,
            size = 8.8.sp,
            weight = if (selected) FontWeight.Bold else FontWeight.Normal,
            align = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/**
 * Loads the app's real launcher icon off the main thread.
 *
 * Icons are resolved per visible cell rather than for the whole app list: a
 * launcher only needs the tiles the user can actually see, and scrolling then
 * costs nothing for apps that never appear. A generic folder glyph stands in
 * while the icon is loading (or if the package disappears mid-load).
 */
@Composable
private fun AppIcon(app: AppInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, app.packageName) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager.getApplicationIcon(app.packageName)
                    .toBitmap(128, 128)
                    .asImageBitmap()
            }.getOrNull()
        }
    }
    icon?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    } ?: Box(modifier, contentAlignment = Alignment.Center) {
        RazrIconGlyph(RazrIcon.FOLDER, RazrIconTints.Amber, Modifier.fillMaxSize(0.8f))
    }
}

/**
 * Contact action sheet.
 *
 * The stock handset does not call straight from the phonebook: SELECT opens a
 * short menu of what you can do with the highlighted contact. Mirroring that
 * is what makes Call / Message / Copy reachable at all on a three-soft-key
 * panel, and it keeps the destructive-ish actions one extra press away.
 */
@Composable
fun <T> RazrActionSheet(
    palette: RazrPalette,
    heading: String,
    options: List<T>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    label: (T) -> String,
) {
    val safe = selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            T(
                heading.take(22),
                palette.ink,
                12.6.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 1,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.fieldAlt))
        Column(Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 3.dp)) {
            options.forEachIndexed { index, item ->
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    T(
                        label(item),
                        if (selected) palette.selectInk else palette.ink,
                        14.5.sp,
                        if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    T(">", if (selected) palette.selectInk else palette.inkDim, 15.9.sp, FontWeight.Bold)
                }
            }
        }
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
    canAnswer: Boolean = true,
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
            if (canAnswer) {
                T("Send answers", palette.selectLow, 13.sp, FontWeight.Bold, TextAlign.Center)
                T("Power declines", palette.alert, 13.sp, FontWeight.Bold, TextAlign.Center)
            } else {
                // Answering is placing a call, and without a SIM the radio
                // cannot carry one. Saying so beats offering a key that fails.
                T("No SIM card available", palette.alert, 13.sp, FontWeight.Bold, TextAlign.Center)
                T("Cannot answer this call", palette.inkDim, 13.sp, FontWeight.Bold, TextAlign.Center)
                T("Power ends it", palette.alert, 13.sp, FontWeight.Bold, TextAlign.Center)
            }
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
    operatorName: String = RazrOperators.UNKNOWN_OPERATOR,
    noSim: Boolean = false,
    noService: Boolean = false,
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
        T(
            if (noSim) "NO SIM" else operatorName.take(9).uppercase(Locale.ROOT),
            if (noSim) Color(0xFFFF6B5E) else Color.White,
            8.7.sp,
            FontWeight.Bold,
            maxLines = 1,
        )
                if (noService) {
                    T("NO SVC", Color(0xFFFF6B5E), 8.7.sp, FontWeight.Bold, maxLines = 1)
                } else if (unread > 0) {
                    T("✉$unread", Color(0xFFFFD34D), 8.7.sp, FontWeight.Bold)
                }
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
                        // "1 MISSED CALLS" was the wording on the cover, which
                        // is exactly the sort of thing that gives a skin away.
                        // A 96x80 panel only has room for the singular form
                        // anyway, so it has to be short as well as correct.
                        ?: if (missedCalls > 0) {
                            if (missedCalls == 1) "1 MISSED CALL" else "$missedCalls MISSED CALLS"
                        } else {
                            date.uppercase(Locale.ROOT)
                        },
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

/**
 * Incoming call on the closed flip.
 *
 * The 96 x 80 cover has room for exactly one useful thing while ringing, and on
 * the real handset that is the number. The internal panel says "Incoming call"
 * and then the number; the cover leads with the number, because that is the only
 * thing needed to decide whether to pick up. The answer/decline line is kept to
 * the minimum, since the green and red keys are what actually answer and
 * reject and the screen would be unusable without telling you so.
 */
@Composable
fun RazrCoverIncomingCall(
    palette: RazrPalette,
    number: String,
    canAnswer: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(palette.field),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(palette.alert)
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                T("INCOMING CALL", Color.White, 8.7.sp, FontWeight.Bold)
            }

            T(
                number.ifBlank { "UNKNOWN" },
                palette.ink,
                15.9.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            // Answering is placing a call. With no SIM the radio cannot carry
            // one, so the key is not offered as though it would work.
            T(
                if (canAnswer) "SEND answers  POWER declines" else "NO SIM - CANNOT ANSWER",
                if (canAnswer) palette.inkDim else palette.alert,
                8.7.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Live call on the closed flip.
 *
 * With the flip shut there is no room for the timer or the cost breakdown the
 * internal panel carries, so the cover shows what you can actually change: the
 * number, and the three line states that the keys toggle. Speaker, hold and
 * mute are all still live here, which is why they are shown rather than hidden.
 */
@Composable
fun RazrCoverInCall(
    palette: RazrPalette,
    number: String,
    timer: String,
    muted: Boolean,
    onHold: Boolean,
    speaker: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(palette.field),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow)))
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                T("IN CALL", palette.selectInk, 8.7.sp, FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T(
                    number.ifBlank { "-" },
                    palette.ink,
                    13.sp,
                    FontWeight.Bold,
                    TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                T(timer, palette.selectLow, 17.4.sp, FontWeight.Bold, TextAlign.Center, maxLines = 1)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CoverPill(palette, "SPKR", speaker, Modifier.weight(1f))
                CoverPill(palette, "HOLD", onHold, Modifier.weight(1f))
                CoverPill(palette, "MUTE", muted, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CoverPill(
    palette: RazrPalette,
    label: String,
    on: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(1.5.dp))
            .background(if (on) palette.selectTop else palette.fieldAlt)
            .border(0.6.dp, palette.inkDim, RoundedCornerShape(1.5.dp))
            .padding(vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        T(
            label,
            if (on) palette.selectInk else palette.inkDim,
            8.7.sp,
            FontWeight.Bold,
            TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * No SIM: emergency calls only.
 *
 * Shown instead of placing the call. Without a SIM the radio cannot carry an
 * ordinary call at all, so refusing at the UI is more honest than letting the
 * platform fail the dial and leave the caller with a silent failure. Emergency
 * numbers still go through, because they must.
 */
@Composable
fun RazrNoSimNotice(
    palette: RazrPalette,
    number: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
            .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        T("RAZR V3i", palette.inkDim, 11.6.sp, FontWeight.Bold, TextAlign.Center)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            // Circled i, drawn rather than typed: the panel glyph fonts are not
            // guaranteed to carry an information symbol.
            Canvas(Modifier.size(22.dp)) {
                val r = size.minDimension / 2f
                drawCircle(
                    color = palette.selectLow,
                    radius = r - size.minDimension * 0.06f,
                    style = Stroke(width = size.minDimension * 0.10f),
                )
                drawLine(
                    color = palette.selectLow,
                    start = Offset(size.width / 2f, size.height * 0.44f),
                    end = Offset(size.width / 2f, size.height * 0.70f),
                    strokeWidth = size.minDimension * 0.11f,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = palette.selectLow,
                    radius = size.minDimension * 0.07f,
                    center = Offset(size.width / 2f, size.height * 0.30f),
                )
            }
            T(
                number,
                palette.inkDim,
                13.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 1,
            )
            T(
                "NO SIM CARD\nAVAILABLE",
                palette.ink,
                17.4.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 2,
            )
            T(
                "Emergency calls only\n(911 / 112)",
                palette.selectLow,
                11.6.sp,
                FontWeight.Bold,
                TextAlign.Center,
                maxLines = 2,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("TRY AGAIN LATER", palette.alert, 13.sp, FontWeight.Bold, TextAlign.Center)
            T("BACK", palette.inkDim, 10.2.sp, FontWeight.Bold, TextAlign.Center)
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

/**
 * Browseable list of OEM engineering codes.
 *
 * Parallax cannot dispatch these itself: sending the broadcast needs
 * `MODIFY_PHONE_STATE`, a signature permission no launcher can hold. So this is
 * a reference, not a remote control — SELECT copies a code into the dial
 * buffer and SEND hands it to a real dialer, which is the component that does
 * hold the permission. See [SecretCodes] for the full explanation.
 */
@Composable
fun RazrSecretCodeList(
    palette: RazrPalette,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val entries = SecretCodes.reference
    val safe = selectedIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
    // This list is walked with a D-pad, not a finger, so it has to keep the
    // highlighted row on screen. Without this the column keeps the scroll
    // position from last time and re-entering shows entries 4+ while the
    // selection is still on entry 1 -- the screen and the state disagree.
    val listState = rememberLazyListState()
    LaunchedEffect(safe) {
        listState.scrollToItem(safe)
    }
    Column(
        modifier
            .fillMaxSize()
            .background(palette.field)
    ) {
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            itemsIndexed(entries) { index, entry ->
                val selected = index == safe
                val ink = if (selected) palette.selectInk else palette.ink
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow))
                            } else SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Monospace so the * and # line up column-wise down the
                        // list; the digits are meaningless in a proportional face.
                        T(
                            entry.code,
                            ink,
                            16.sp,
                            FontWeight.Bold,
                            family = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                        )
                        T(
                            entry.brands,
                            if (selected) palette.selectInk else palette.inkDim,
                            9.4.sp,
                            FontWeight.Bold,
                            TextAlign.End,
                        )
                    }
                    T(
                        entry.label,
                        when {
                            entry.blocked -> palette.alert
                            selected -> palette.selectInk
                            else -> palette.inkDim
                        },
                        11.sp,
                        if (selected || entry.blocked) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(palette.fieldAlt)
                .padding(horizontal = 5.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            T("SELECT COPIES CODE", palette.selectLow, 9.4.sp, FontWeight.Bold)
            T("SEND HANDS TO DIALER", palette.inkDim, 9.4.sp, FontWeight.Bold, TextAlign.End)
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
