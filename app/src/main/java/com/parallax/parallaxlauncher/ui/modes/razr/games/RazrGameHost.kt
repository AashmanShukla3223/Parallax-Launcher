package com.parallax.parallaxlauncher.ui.modes.razr.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIcon
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconGlyph
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrSoftKeyBar
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrTitleBar
import kotlinx.coroutines.delay

/** Stable storage key per game slot. */
fun gameId(index: Int): String = when (index) {
    0 -> "snake"
    1 -> "breaker"
    else -> "frog"
}

/**
 * Game picker, styled like the rest of the V3i menus rather than as a foreign
 * widget dropped into the handset.
 */
@Composable
fun RazrGamesMenu(
    palette: RazrPalette,
    games: List<() -> RazrGame>,
    scores: RazrScores,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val safe = selectedIndex.coerceIn(0, (games.size - 1).coerceAtLeast(0))
    Column(modifier.fillMaxSize().background(palette.field)) {
        RazrTitleBar(palette, "Games & Apps")
        Column(
            Modifier.weight(1f).padding(horizontal = 4.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            games.forEachIndexed { index, factory ->
                val g = remember(index) { factory() }
                val selected = index == safe
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(palette.selectTop, palette.selectLow)
                                )
                            } else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                        )
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .width(16.dp)
                            .height(16.dp)
                            .background(if (selected) palette.selectInk else palette.fieldAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        RazrIconGlyph(
                            when (index) {
                                0 -> RazrIcon.GAMES
                                1 -> RazrIcon.FILES
                                else -> RazrIcon.PLAYLIST
                            },
                            if (selected) palette.selectLow else g.accent,
                            Modifier.width(12.dp).height(12.dp),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            g.title,
                            color = if (selected) palette.selectInk else palette.ink,
                            fontSize = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                        )
                        Text(
                            g.blurb,
                            color = if (selected) palette.selectInk else palette.inkDim,
                            fontSize = 7.sp,
                            maxLines = 1,
                        )
                    }
                    Text(
                        "BEST ${scores.best(gameId(index))}",
                        color = if (selected) palette.selectInk else palette.inkDim,
                        fontSize = 7.sp,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "›",
                        color = if (selected) palette.selectInk else palette.inkDim,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("5 PLAY", color = palette.selectLow, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                Text("0 APPS", color = palette.inkDim, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Plays one game.
 *
 * The tick is a coroutine loop rather than Compose animation, which keeps the
 * deliberately low cadence of the original MIDlets and keeps simulation state
 * out of the recomposition path. Each tick bumps [tick], which re-runs the draw.
 */
@Composable
fun RazrGameHost(
    palette: RazrPalette,
    game: RazrGame,
    scores: RazrScores,
    onExit: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tick by remember { mutableIntStateOf(0) }
    var showOver by remember { mutableStateOf(false) }
    val id = remember(game) { game.title }

    // Ops are collected by Render into this list, then drawn. Keeping them in a
    // remembered list avoids allocating per frame.
    val ops = remember { mutableListOf<DrawOp>() }
    val painter = remember { RazrPainter { ops += it } }

    LaunchedEffect(game) {
        game.reset()
        showOver = false
        while (true) {
            delay(RAZR_TICK_MS)
            game.update(RAZR_TICK_MS)
            if (game.isOver && !showOver) {
                scores.submit(id, game.score)
                showOver = true
            }
            tick++
        }
    }

    Column(modifier.fillMaxSize().background(palette.field)) {
        RazrTitleBar(palette, game.title)

        Row(
            Modifier
                .fillMaxWidth()
                .background(palette.fieldAlt)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("SCORE ${game.score}", color = palette.ink, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text("BEST ${scores.best(id)}", color = palette.inkDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }

        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            // Integer cell size keeps the low-resolution look crisp instead of
            // fractional cells that would blur.
            val cell = (minOf(maxWidth / Grid.W, maxHeight / Grid.H)).let {
                if (it.value < 1f) 1.dp else it
            }
            val boardW = cell * Grid.W
            val boardH = cell * Grid.H

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Canvas(Modifier.width(boardW).height(boardH)) {
                    val cs = this.size.width / Grid.W
                    fun fx(v: Int) = v * cs
                    fun fy(v: Int) = v * cs

                    // Re-run the game's draw into the op list each frame.
                    ops.clear()
                    game.Render(painter)

                    // Shapes first, then any cell text on top.
                    ops.forEach { op ->
                        when (op) {
                            is DrawOp.Cell -> drawRect(
                                op.color, Offset(fx(op.x), fy(op.y)), Size(cs, cs),
                            )
                            is DrawOp.Rect -> drawRect(
                                op.color, Offset(fx(op.x), fy(op.y)), Size(op.w * cs, op.h * cs),
                            )
                            is DrawOp.Frame -> drawRect(
                                op.color,
                                Offset(fx(op.x), fy(op.y)),
                                Size(op.w * cs, op.h * cs),
                                style = Stroke(cs * 0.9f),
                            )
                            is DrawOp.Disc -> drawCircle(
                                op.color, (op.r * cs).coerceAtLeast(cs * 0.5f),
                                Offset(op.cx * cs, op.cy * cs),
                            )
                            is DrawOp.Text -> Unit
                        }
                    }
                    ops.filterIsInstance<DrawOp.Text>().forEach { op ->
                        var cx = fx(op.x)
                        op.s.forEach { ch ->
                            if (ch != ' ') {
                                drawRect(
                                    op.color.copy(alpha = 0.92f),
                                    Offset(cx, fy(op.y)),
                                    Size(cs * 0.78f, cs * 0.78f),
                                )
                            }
                            cx += cs
                        }
                    }
                }

                if (showOver) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GAME OVER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("SCORE ${game.score}", color = Color.White, fontSize = 9.sp)
                            Text(
                                if (game.score >= scores.best(id)) "NEW BEST" else "BEST ${scores.best(id)}",
                                color = game.accent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "5 RETRY    0 EXIT",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 8.sp,
                            )
                        }
                    }
                }
            }
        }

        RazrSoftKeyBar(palette, "EXIT", "5 RETRY", "0 MENU")
    }
}