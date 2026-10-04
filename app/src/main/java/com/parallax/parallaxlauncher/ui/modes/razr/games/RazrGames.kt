package com.parallax.parallaxlauncher.ui.modes.razr.games

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * SNAKE '99 — keypad-native snake.
 *
 * Steered by the rocker *or* the number pad, because the real handset's games
 * accepted both: 2/4/6/8 and the arrows both turn, 5 pauses, 0 restarts.
 */
class SnakeGame : RazrGame {
    override val title = "SNAKE '99"
    override val blurb = "2 4 6 8 or ROCKER"
    override val accent = Color(0xFF3FA34D)

    private val body = ArrayDeque<Pair<Int, Int>>()
    private var dirX = 1
    private var dirY = 0
    private var queuedX = 1
    private var queuedY = 0
    private var foodX = 0
    private var foodY = 0
    private var growing = 0
    private var stepTimer = 0
    private var paused = false

    override var score = 0
        private set
    override var isOver = false
        private set

    override fun reset() {
        body.clear()
        val midY = Grid.FIELD_TOP + Grid.FIELD_H / 2
        for (i in 0 until 4) body.addLast(4 - i to midY)
        dirX = 1; dirY = 0; queuedX = 1; queuedY = 0
        score = 0
        growing = 0
        stepTimer = 0
        paused = false
        isOver = false
        placeFood()
    }

    private fun placeFood() {
        var tries = 0
        do {
            foodX = Random.nextInt(1, Grid.FIELD_W - 1)
            foodY = Random.nextInt(Grid.FIELD_TOP + 1, Grid.FIELD_TOP + Grid.FIELD_H - 1)
            tries++
        } while (body.contains(foodX to foodY) && tries < 200)
    }

    override fun update(tickMs: Long) {
        if (isOver || paused) return
        // Speeds up as the snake grows.
        if (stepTimer > (150 - score * 3).coerceAtLeast(55)) {
            stepTimer = 0
            step()
        } else {
            stepTimer += tickMs.toInt()
        }
    }

    private fun step() {
        dirX = queuedX; dirY = queuedY
        val head = body.first()
        val nx = head.first + dirX
        val ny = head.second + dirY

        val hitWall = nx <= 0 || nx >= Grid.FIELD_W - 1 ||
                ny <= Grid.FIELD_TOP || ny >= Grid.FIELD_TOP + Grid.FIELD_H
        val hitSelf = body.dropLast(1).any { it.first == nx && it.second == ny }
        if (hitWall || hitSelf) {
            isOver = true
            return
        }

        body.addFirst(nx to ny)
        if (nx == foodX && ny == foodY) {
            score += 10
            growing += 2
            placeFood()
        } else if (growing > 0) {
            growing--
        } else {
            body.removeLast()
        }
    }

    override fun onKey(ch: Char) {
        when (ch) {
            '2' -> turn(0, -1)
            '4' -> turn(-1, 0)
            '6' -> turn(1, 0)
            '8' -> turn(0, 1)
            '5' -> { paused = !paused }
            '0' -> reset()
        }
    }

    override fun onNav(nav: Nav) {
        when (nav) {
            Nav.UP -> turn(0, -1)
            Nav.DOWN -> turn(0, 1)
            Nav.LEFT -> turn(-1, 0)
            Nav.RIGHT -> turn(1, 0)
            Nav.CENTRE -> { paused = !paused }
        }
    }

    private fun turn(x: Int, y: Int) {
        // Reject instant 180s so the body cannot drive into itself.
        if (x == -dirX && y == -dirY) return
        queuedX = x
        queuedY = y
    }

        override fun Render(p: RazrPainter) {
        // Walls.
        p.frame(0, Grid.FIELD_TOP, Grid.FIELD_W, Grid.FIELD_H, accent)
        // Food.
        p.cell(foodX, foodY, Color(0xFFE24A3C))
        // Body, head brightest.
        body.forEachIndexed { i, seg ->
            val fade = (1f - i.toFloat() / (body.size + 4f)).coerceIn(0.30f, 1f)
            p.cell(seg.first, seg.second, accent.copy(alpha = fade))
        }
        if (paused && !isOver) p.text(30, Grid.FIELD_TOP + 40, "PAUSED", Color(0xFFFFFFFF))
    }
}

/**
 * BLOCK BREAKER — the keypad-native V3i arcade staple.
 *
 * Paddle on 4/6, launch with 5, brick layouts tighten each level.
 */
class BlockBreakerGame : RazrGame {
    override val title = "BLOCK BREAKER"
    override val blurb = "4 6 MOVE - 5 SERVE"
    override val accent = Color(0xFF2E74C8)

    private val COLS = 11
    private val ROWS = 7
    private val brickW = 7
    private val brickH = 3

    private var grid = Array(ROWS) { BooleanArray(COLS) }
    private var paddleX = (Grid.FIELD_W - 16) / 2
    private var ballX = 0f
    private var ballY = 0f
    private var velX = 0f
    private var velY = 0f
    private var level = 1
    private var lives = 3
    private var serving = true

    override var score = 0
        private set
    override var isOver = false
        private set

    override fun reset() {
        level = 1
        lives = 3
        score = 0
        isOver = false
        newLevel()
    }

    private fun newLevel() {
        grid = Array(ROWS) { r -> BooleanArray(COLS) { c ->
            val row = r
            // Opening pattern widens as levels rise.
            val gap = (COLS - 5 - level).coerceAtLeast(1)
            val start = (COLS - gap) / 2
            !(c in start until start + gap)
        } }
        paddleX = (Grid.FIELD_W - 16) / 2
        ballX = Grid.FIELD_W / 2f
        ballY = (Grid.FIELD_TOP + Grid.FIELD_H - 8).toFloat()
        velX = 0f
        velY = 0f
        serving = true
    }

    private val paddleY get() = Grid.FIELD_TOP + Grid.FIELD_H - 5
    private val ceiling get() = Grid.FIELD_TOP

    override fun update(tickMs: Long) {
        if (isOver) return
        if (serving) return

        ballX += velX
        ballY += velY

        if (ballX <= 1f) { ballX = 1f; velX = -velX }
        if (ballX >= Grid.FIELD_W - 1f) { ballX = Grid.FIELD_W - 1f; velX = -velX }
        if (ballY <= ceiling) { ballY = ceiling.toFloat(); velY = -velY }

        // Paddle bounce.
        if (ballY >= paddleY - 1 && ballY <= paddleY + 1 &&
            ballX >= paddleX && ballX <= paddleX + 16
        ) {
            ballY = (paddleY - 1).toFloat()
            velY = -velY
            // Off-centre hits angle the ball.
            val hit = (ballX - (paddleX + 8)) / 8f
            velX = hit * 1.6f
        }

        // Bottom = lose a ball.
        if (ballY >= Grid.FIELD_TOP + Grid.FIELD_H) {
            lives--
            if (lives <= 0) { isOver = true; return }
            serving = true
            ballX = Grid.FIELD_W / 2f
            ballY = (paddleY - 1).toFloat()
            velX = 0f; velY = 0f
            return
        }

        // Bricks.
        val col = (ballX / brickW).toInt()
        val row = ((ballY - ceiling) / brickH).toInt()
        if (row in 0 until ROWS && col in 0 until COLS && grid[row][col]) {
            grid[row][col] = false
            score += 5 * level
            velY = -velY
            // Nudge off the brick edge to avoid tunnelling.
            if (ballX - col * brickW < brickW / 2f) velX = -velX else velX = velX
            if (grid.all { r -> r.all { !it } }) { level++; newLevel() }
        }
    }

    private fun serve() {
        serving = false
        velY = -1.4f
        velX = Random.nextFloat() * 1.2f - 0.6f
    }

    override fun onKey(ch: Char) {
        when (ch) {
            '4' -> paddleX = (paddleX - 3).coerceAtLeast(0)
            '6' -> paddleX = (paddleX + 3).coerceAtMost(Grid.FIELD_W - 16)
            '5' -> if (serving) serve()
            '0' -> reset()
        }
    }

    override fun onNav(nav: Nav) {
        when (nav) {
            Nav.LEFT -> onKey('4')
            Nav.RIGHT -> onKey('6')
            Nav.CENTRE -> if (serving) serve()
            else -> Unit
        }
    }

        override fun Render(p: RazrPainter) {
        p.frame(0, Grid.FIELD_TOP, Grid.FIELD_W, Grid.FIELD_H, accent)
        grid.forEachIndexed { r, row ->
            row.forEachIndexed { c, alive ->
                if (alive) {
                    val hue = ((r * COLS + c + level) % 5)
                    p.rect(
                        c * brickW + 1, Grid.FIELD_TOP + 1 + r * brickH,
                        brickW - 1, brickH - 1,
                        BRICKS[hue],
                    )
                }
            }
        }
        p.rect(paddleX, paddleY, 16, 2, accent)
        p.disc(ballX, ballY, 1.6f, Color(0xFFFFFFFF))
        if (serving && !isOver) {
            p.text(28, Grid.FIELD_TOP + 45, "PRESS 5", Color(0xFFFFFFFF))
        }
    }

    private companion object {
        val BRICKS = listOf(
            Color(0xFFC0392B), Color(0xFFDE6B22), Color(0xFFE0A020),
            Color(0xFF3FA34D), Color(0xFF2E74C8),
        )
    }
}

/**
 * FROG JUMP — lifted from the V3i's own Java game line.
 *
 * Hop a fixed distance on 5, steer lanes on the rocker, platforms scroll down
 * toward the water.
 */
class FrogJumpGame : RazrGame {
    override val title = "FROG JUMP"
    override val blurb = "5 HOP - ROCKER STEER"
    override val accent = Color(0xFFDE6B22)

    private val LANE_H = 12
    private val FROG_X = 40

    /** One platform per lane, scrolling toward the water. */
    private val platforms = ArrayList<Platform>()

    private var frogLane = 5
    private var frogOffset = 6f
    private var hopping = false
    private var hopFrom = 6f
    private var hopTo = 6f
    private var waterLane = 10
    private var ticks = 0

    private class Platform(var lane: Int, var offset: Float, val width: Int)

    override var score = 0
        private set
    override var isOver = false
        private set

    override fun reset() {
        platforms.clear()
        for (i in 6 downTo 0) {
            platforms.add(Platform(i, Random.nextInt(0, LANE_H - 10).toFloat(), Random.nextInt(9, 16)))
        }
        frogLane = 5
        frogOffset = 6f
        hopping = false
        score = 0
        ticks = 0
        waterLane = 10
        isOver = false
    }

    private fun laneY(lane: Int) = Grid.FIELD_TOP + lane * LANE_H + 2

    override fun update(tickMs: Long) {
        if (isOver) return

        if (hopping) {
            frogOffset += (hopTo - hopFrom) * 0.35f
            if (kotlin.math.abs(frogOffset - hopTo) < 0.4f) {
                frogOffset = hopTo
                hopping = false
                onLanded()
            }
            return
        }

        // Scroll everything down one tick.
        ticks++
        if (ticks % 6 == 0) {
            platforms.forEach { it.offset += 1f }
            frogOffset += 1f
        }

        // Fell in the water?
        if (frogLane >= waterLane - 1) { isOver = true; return }

        // Carried off the bottom of a platform.
        val under = platforms.filter { it.lane == frogLane }
        if (under.none { frogOffset >= it.offset && frogOffset <= it.offset + it.width }) {
            isOver = true
            return
        }

        // Cull spent platforms and recycle them at the top.
        val iterator = platforms.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.offset > LANE_H) iterator.remove()
        }
        if (platforms.none { it.lane == 0 }) {
            platforms.add(Platform(0, Random.nextInt(0, 4).toFloat(), Random.nextInt(9, 16)))
        }
    }

    private fun onLanded() {
        val best = platforms.filter { it.lane == frogLane }
            .minByOrNull { kotlin.math.abs(it.offset - frogOffset) }
        if (best != null && frogLane > 0) {
            frogLane--
            frogOffset = best.offset + best.width / 2f
            score += 10
        }
    }

    private fun hop() {
        if (hopping) return
        val target = platforms.firstOrNull { it.lane == frogLane + 1 && frogOffset >= it.offset - 4 && frogOffset <= it.offset + it.width + 4 }
        if (target != null) {
            hopFrom = frogOffset
            hopTo = (target.offset + target.width / 2f).coerceIn(2f, LANE_H - 3f)
            hopping = true
        } else {
            frogOffset += LANE_H - 4f
            hopping = true
            hopFrom = frogOffset - (LANE_H - 4f)
            hopTo = frogOffset + (LANE_H - 4f)
        }
    }

    override fun onKey(ch: Char) {
        when (ch) {
            '2', '5' -> hop()
            '4' -> frogOffset = (frogOffset - 2f).coerceAtLeast(1f)
            '6' -> frogOffset = (frogOffset + 2f).coerceAtMost(LANE_H - 2f)
            '0' -> reset()
        }
    }

    override fun onNav(nav: Nav) {
        when (nav) {
            Nav.UP, Nav.CENTRE -> hop()
            Nav.LEFT -> onKey('4')
            Nav.RIGHT -> onKey('6')
            Nav.DOWN -> Unit
        }
    }

        override fun Render(p: RazrPainter) {
        // Lanes.
        for (lane in 0..9) {
            val y = laneY(lane)
            p.rect(0, y, Grid.FIELD_W, LANE_H - 2, if (lane % 2 == 0) Color(0xFF243B24) else Color(0xFF1B2E1B))
        }
        // Water.
        p.rect(0, laneY(waterLane), Grid.FIELD_W, Grid.FIELD_TOP + Grid.FIELD_H - laneY(waterLane), Color(0xFF2E74C8))

        // Platforms.
        platforms.forEach { p2 ->
            val y = laneY(p2.lane) + p2.offset.toInt()
            p.rect(p2.offset.toInt(), y, p2.width, 2, Color(0xFF9AA6B2))
        }

        // Frog.
        val fy = laneY(frogLane) + frogOffset.toInt()
        p.rect(FROG_X, fy, 6, 4, accent)
        p.cell(FROG_X + 1, fy, Color(0xFFFFFFFF))
        p.cell(FROG_X + 4, fy, Color(0xFFFFFFFF))
    }
}

/** The bundled RAZR-exclusive line-up. */
val RAZR_GAMES: List<() -> RazrGame> = listOf(
    { SnakeGame() },
    { BlockBreakerGame() },
    { FrogJumpGame() },
)