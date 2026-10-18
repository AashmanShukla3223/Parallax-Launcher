package com.parallax.parallaxlauncher.ui.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The song list, in the same iTunes chrome as the player.
 *
 * This is the "choose a song" half. The real V3i shipped with iTunes, where
 * the library and the player were the same window, so this deliberately shares
 * the brushed face and the LCD readout with [ItunesNowPlaying] rather than
 * looking like a different app that happens to share a launcher.
 *
 * A track is started by SELECT, which hands it to Parallax's own session — see
 * [ParallaxMediaService] for why the launcher plays rather than delegating.
 */
@Composable
fun ItunesLibrary(
    tracks: List<MediaItem>,
    selectedIndex: Int,
    hasPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { brushedFace(radius = size.minDimension * 0.045f) }

        Column(
            Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // The selection strip, iTunes-style: inverted row over the LCD.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    lcdWell(0f, 0f, size.width, size.height, radius = size.minDimension * 0.07f)
                }
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        if (tracks.isEmpty()) "NO MUSIC" else "SOURCE",
                        color = ItunesSkin.LcdInkDim,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.SansSerif,
                    )
                    Text(
                        text = when {
                            tracks.isEmpty() -> "Nothing indexed on this device"
                            hasPermission -> "${tracks.size} TRACKS"
                            else -> "Grant media access"
                        },
                        color = ItunesSkin.LcdInk,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            if (tracks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (hasPermission) {
                            "Add some music and it appears here."
                        } else {
                            "Parallax needs media access to see your library."
                        },
                        color = ItunesSkin.MetalEdgeLo,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                return@Column
            }

            val listState = rememberLazyListState()
            LaunchedEffect(selectedIndex) {
                // D-pad driven, so the highlighted row has to stay visible.
                listState.scrollToItem(selectedIndex.coerceIn(0, tracks.lastIndex))
            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                itemsIndexed(tracks) { index, item ->
                    val selected = index == selectedIndex
                    val bg = if (selected) {
                        Brush.horizontalGradient(listOf(ItunesSkin.Accent, ItunesSkin.AccentHi))
                    } else {
                        SolidColor(Color.Transparent)
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                    ) {
                        Text(
                            item.title,
                            color = if (selected) ItunesSkin.ChromeGloss else ItunesSkin.LcdInk,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                item.subtitle,
                                color = if (selected) {
                                    ItunesSkin.ChromeGloss.copy(alpha = 0.85f)
                                } else {
                                    ItunesSkin.MetalEdgeLo
                                },
                                fontSize = 9.sp,
                                fontFamily = FontFamily.SansSerif,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            item.durationLabel()?.let { d ->
                                Text(
                                    d,
                                    color = if (selected) {
                                        ItunesSkin.ChromeGloss.copy(alpha = 0.85f)
                                    } else {
                                        ItunesSkin.MetalEdgeLo
                                    },
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}