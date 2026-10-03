package com.parallax.parallaxlauncher.ui.modes.swiss.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.model.AppInfo
import androidx.compose.ui.graphics.Color
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette

/** Press inverts ink/paper before launching. No icons anywhere. */
@Composable
fun Story(
    app: AppInfo,
    fontSize: TextUnit,
    serif: Boolean,
    onPress: () -> Unit,
    onOpen: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalParallaxPalette.current
    var down by remember { mutableStateOf(false) }
    Box(
        modifier
            .background(if (down) Ink else Color.Transparent)
            .pointerInput(app) {
                detectTapGestures(
                    onPress = { down = true; onPress(); tryAwaitRelease(); down = false },
                    onTap = { onOpen(app) },
                )
            }
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Text(
            app.label.uppercase(),
            style = TextStyle(
                fontFamily = palette.font,
                fontWeight = if (serif) FontWeight.Black else FontWeight.SemiBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.95f,
                letterSpacing = if (serif) (-1).sp else 0.sp,
                color = if (down) Color(0xFFF4F1EA) else Ink,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Auto-scale headline size by character count so the lead fills the column. */
fun leadSize(label: String): TextUnit = when {
    label.length <= 6 -> 72.sp
    label.length <= 10 -> 54.sp
    label.length <= 16 -> 38.sp
    else -> 28.sp
}

@Composable
fun LeadStories(
    leads: List<AppInfo>,
    onPress: () -> Unit,
    onOpen: (AppInfo) -> Unit,
) {
    if (leads.isEmpty()) return
    val palette = LocalParallaxPalette.current
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            "LEAD STORY",
            style = TextStyle(fontFamily = palette.font, fontSize = 10.sp, color = palette.accent, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
        )
        Story(leads[0], leadSize(leads[0].label), true, onPress, onOpen, Modifier.fillMaxWidth())
        if (leads.size > 1) {
            Rule()
            Story(leads[1], leadSize(leads[1].label).let { (it.value * 0.6f).sp }, true, onPress, onOpen, Modifier.fillMaxWidth())
        }
    }
    Rule(thick = true)
}

/** Alphabetical classifieds: letter column + two-column stories separated by hairlines. */
@Composable
fun Classifieds(
    apps: List<AppInfo>,
    onPress: () -> Unit,
    onOpen: (AppInfo) -> Unit,
) {
    val palette = LocalParallaxPalette.current
    val groups = remember(apps) { apps.groupBy { it.label.firstOrNull()?.uppercaseChar() ?: '#' }.toSortedMap() }
    Column(Modifier.fillMaxWidth()) {
        groups.forEach { (letter, list) ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 6.dp)) {
                Text(
                    letter.toString(),
                    Modifier.width(44.dp),
                    style = TextStyle(fontFamily = palette.font, fontWeight = FontWeight.Black, fontSize = 40.sp, color = palette.accent),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    list.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            pair.forEachIndexed { i, app ->
                                if (i > 0) Box(Modifier.width(1.dp).height(28.dp).background(Ink))
                                Story(app, 16.sp, false, onPress, onOpen, Modifier.weight(1f).padding(start = if (i > 0) 8.dp else 0.dp))
                            }
                            if (pair.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
            Rule()
        }
    }
}
