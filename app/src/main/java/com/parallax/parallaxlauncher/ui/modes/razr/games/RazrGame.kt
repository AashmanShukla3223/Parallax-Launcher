package com.parallax.parallaxlauncher.ui.modes.razr.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * The logical playfield.
 *
 * The real handset had a 176x220 colour TFT and its Java MIDlets drew into a
 * small pixel grid at a low tick rate. Games here therefore work in logical
 * cells rather than Compose dp, and are scaled up on render, so the chunky
 * original look is preserved instead of crisp vector shapes.
 */
object Grid {
    /** Logical pixels wide (matches the panel width). */
    const val W = 88

    /** Logical pixels tall, leaving room for a two-line HUD. */
    const val H = 104

    /** Play area only, below the HUD. */
    const val FIELD_TOP = 10

    const val FIELD_W = W
    const val FIELD_H = H - FIELD_TOP
}

/** Direction from the four-way rocker. */
enum class Nav { UP, DOWN, LEFT, RIGHT, CENTRE }

/**
 * A RAZR game.
 *
 * Implementations are plain classes held in a `remember`, driven by a single
 * coroutine tick from [RazrGameHost] rather than Compose animation, which keeps
 * the deliberately low, stuttery cadence of the original MIDlets.
 */
interface RazrGame {
    /** Title shown in the game's own menu row. */
    val title: String

    /** One-line blurb shown under the title. */
    val blurb: String

    /** Tint used for this game's icon and HUD accent. */
    val accent: Color

    /** Reset to a fresh game. */
    fun reset()

    /** Advance one tick. [tickMs] is the elapsed wall time since the last tick. */
    fun update(tickMs: Long)

    /**
     * Called when the game is ticked. Return the score so the host can persist
     * high scores without each game touching storage.
     */
    val score: Int

    /** Game-over flag; the host shows the summary and waits for input. */
    val isOver: Boolean

    /**
     * Draw the playfield into the cell-space [RazrPainter].
     *
     * Deliberately NOT `@Composable`: the host collects these ops from inside a
     * Canvas draw pass, so a game must not read composition state.
     */
    fun Render(p: RazrPainter)

    /** Numeric or symbol key press. */
    fun onKey(ch: Char) {}

    /** Rocker direction. */
    fun onNav(nav: Nav) {}
}

/**
 * Cell-space drawing API.
 *
 * Games never see Compose primitives or dp — only integer cell coordinates, so
 * the low-resolution look is structural rather than a filter applied later.
 */
class RazrPainter(private val onDraw: (DrawOp) -> Unit) {
    fun cell(x: Int, y: Int, color: Color) =
        onDraw(DrawOp.Cell(x, y, color))

    fun rect(x: Int, y: Int, w: Int, h: Int, color: Color) =
        onDraw(DrawOp.Rect(x, y, w, h, color))

    fun frame(x: Int, y: Int, w: Int, h: Int, color: Color) =
        onDraw(DrawOp.Frame(x, y, w, h, color))

    fun text(x: Int, y: Int, s: String, color: Color) =
        onDraw(DrawOp.Text(x, y, s, color))

    fun disc(cx: Float, cy: Float, r: Float, color: Color) =
        onDraw(DrawOp.Disc(cx, cy, r, color))
}

sealed interface DrawOp {
    data class Cell(val x: Int, val y: Int, val color: Color) : DrawOp
    data class Rect(val x: Int, val y: Int, val w: Int, val h: Int, val color: Color) : DrawOp
    data class Frame(val x: Int, val y: Int, val w: Int, val h: Int, val color: Color) : DrawOp
    data class Text(val x: Int, val y: Int, val s: String, val color: Color) : DrawOp
    data class Disc(val cx: Float, val cy: Float, val r: Float, val color: Color) : DrawOp
}

/**
 * High-score storage.
 *
 * The original saved records to internal memory; here they go to
 * SharedPreferences alongside the rest of the app's state.
 */
class RazrScores(context: android.content.Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("razr_games", android.content.Context.MODE_PRIVATE)

    fun best(id: String): Int = prefs.getInt("best_$id", 0)

    fun submit(id: String, score: Int) {
        if (score > best(id)) {
            prefs.edit().putInt("best_$id", score).apply()
        }
    }
}

/** Fixed-step tick rate. The MIDlets ran at roughly this cadence. */
const val RAZR_TICK_MS = 70L

/** Remember a game instance keyed by its stable id. */
@Composable
fun rememberRazrGame(id: String, factory: () -> RazrGame): RazrGame =
    remember(id) { factory() }