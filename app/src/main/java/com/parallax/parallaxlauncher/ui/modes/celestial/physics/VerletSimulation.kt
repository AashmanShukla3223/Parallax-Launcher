package com.parallax.parallaxlauncher.ui.modes.celestial.physics

import com.parallax.parallaxlauncher.core.model.AppInfo
import kotlin.math.ln
import kotlin.math.sqrt

class CelestialNode(
    val app: AppInfo,
    var x: Float,
    var y: Float,
    launchCount: Int,
    baseRadius: Float,
) {
    var oldX = x
    var oldY = y
    var mass = 1f
    var radius = baseRadius
    var pinned = false

    init { setWeight(launchCount, baseRadius) }

    /** Frequency of use -> mass; mass -> radius. */
    fun setWeight(launchCount: Int, baseRadius: Float) {
        mass = 1f + ln(launchCount + 1f)
        radius = baseRadius * (1f + (mass - 1f) * 0.25f)
    }

    fun integrate(gx: Float, gy: Float, friction: Float) {
        if (pinned) return
        val vx = (x - oldX) * friction + gx
        val vy = (y - oldY) * friction + gy
        oldX = x; oldY = y
        x += vx; y += vy
    }
}

class VerletSimulation {
    var nodes: List<CelestialNode> = emptyList()
    var width = 0f
    var height = 0f
    var gravity = 0.55f

    /** [gx], [gy] are normalized tilt (~ -1..1). Zero-G drift keeps nodes alive when flat. */
    fun step(gx: Float, gy: Float, time: Float) {
        if (width <= 0f) return
        val g = gravity
        val ax = gx * g
        val ay = gy * g
        for ((i, n) in nodes.withIndex()) {
            // Gentle per-node drift so the field never fully settles.
            val dx = kotlin.math.sin(time * 0.3f + i * 1.7f) * 0.02f
            val dy = kotlin.math.cos(time * 0.25f + i * 2.3f) * 0.02f
            n.integrate(ax + dx, ay + dy, 0.985f)
        }
        repeat(3) {
            collide()
            bounds()
        }
    }

    private fun collide() {
        val ns = nodes
        for (i in ns.indices) for (j in i + 1 until ns.size) {
            val a = ns[i]; val b = ns[j]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val d2 = dx * dx + dy * dy
            val min = a.radius + b.radius + 4f
            if (d2 >= min * min || d2 == 0f) continue
            val d = sqrt(d2)
            val overlap = min - d
            val nx = dx / d; val ny = dy / d
            val wa = if (a.pinned) 0f else 1f / a.mass
            val wb = if (b.pinned) 0f else 1f / b.mass
            val sum = wa + wb
            if (sum == 0f) continue
            a.x -= nx * overlap * wa / sum; a.y -= ny * overlap * wa / sum
            b.x += nx * overlap * wb / sum; b.y += ny * overlap * wb / sum
        }
    }

    private fun bounds() {
        for (n in nodes) {
            if (n.pinned) continue
            val r = n.radius
            // Wall reflection: implicit velocity v = x - oldX becomes -0.6v.
            val vx = n.x - n.oldX
            val vy = n.y - n.oldY
            if (n.x < r || n.x > width - r) {
                n.x = n.x.coerceIn(r, (width - r).coerceAtLeast(r))
                n.oldX = n.x + vx * 0.6f
            }
            if (n.y < r || n.y > height - r) {
                n.y = n.y.coerceIn(r, (height - r).coerceAtLeast(r))
                n.oldY = n.y + vy * 0.6f
            }
        }
    }

    fun hit(px: Float, py: Float): CelestialNode? =
        nodes.filter {
            val dx = it.x - px; val dy = it.y - py
            dx * dx + dy * dy <= (it.radius * 1.2f) * (it.radius * 1.2f)
        }.minByOrNull { (it.x - px) * (it.x - px) + (it.y - py) * (it.y - py) }
}
