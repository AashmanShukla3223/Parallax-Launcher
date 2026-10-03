# Parallax Launcher - Style 2: Celestial / OrbitOS Implementation Guide

> **Design Persona:** Deep space orbital mechanics, generative physics, generative celestial charts.
> **Vibe:** Minimalist vector aesthetics, deep space obsidian (`#05060A`), starry glowing node constellations, physical mass dynamics, and gyroscope-driven gravitational field.

---

## 1. Aesthetic & Hardware Paradigms
- **Color Palette:**
  - Space Canvas: Void black/midnight navy (`#030508`), stellar dust gradients (`#0C101B`).
  - Orbit Rings & Gravitational Grids: Ultra-low opacity starlight (`#FFFFFF` with `0.08f` alpha).
  - Celestial Nodes (Apps): Luminous cyan (`#64D2FF`), solar gold (`#FFD60A`), nebula violet (`#BF5AF2`), spectral white (`#FFFFFF`).
- **Typography:**
  - Modern geometric sans-serif (e.g., Space Grotesk / Inter), thin weights, uppercase satellite nomenclature.
- **Physical Feel:**
  - Floating zero-G micro-gravity inertia.
  - Tilting the phone gently bends the gravity vector, causing apps to slide and settle dynamically against screen borders and orbital rings.

---

## 2. Component Architecture

```
OrbitOsScreen (Root Composable)
 ├── Background: DeepSpaceCanvas
 │    ├── Dynamic Starfield (Parallax micro-stars responsive to gyro drift)
 │    └── Gravitational Vector Rings (Concentric orbits centered on primary node or screen center)
 ├── PhysicsEngine: VerletSimulationLoop
 │    ├── Particle State (Position, PreviousPosition, Mass, Radius, Velocity, AppInfo)
 │    ├── Gravity Vector Injector (SensorHub pitch/roll -> Gx, Gy)
 │    ├── Collision & Repulsion (Spatial repulsion between nodes to prevent overlap)
 │    └── Screen Boundary Constraints (Elastic boundary bounce with damping)
 └── Foreground: NodeRenderer
      ├── Glow Halos (Radial gradients proportional to app launch frequency / mass)
      ├── App Vector Nodes (Circular node with icon or letter glyph)
      └── Orbit Lines / Constellation Connectors (Draws faint vectors between frequently co-launched apps)
```

---

## 3. Mathematical & Technical Specifications

### A. Verlet Integration Physics Engine
Instead of Euler integration (which can become unstable at high frame rates), use Verlet integration:

```kotlin
data class CelestialNode(
    val id: String,
    val appInfo: AppInfo,
    var x: Float,
    var y: Float,
    var oldX: Float,
    var oldY: Float,
    val mass: Float, // Derived from launch count: 1.0f + log(launchCount + 1)
    val radius: Float = 24.dp.toPx() * (1f + (mass - 1f) * 0.25f)
) {
    fun update(gravityX: Float, gravityY: Float, friction: Float = 0.985f, dt: Float = 1f) {
        val vx = (x - oldX) * friction + gravityX * dt * dt
        val vy = (y - oldY) * friction + gravityY * dt * dt
        oldX = x
        oldY = y
        x += vx
        y += vy
    }
}
```

### B. IMU Tilt Gravity Vector (SensorHub)
1. Sample accelerometer/gravity sensor:
   ```kotlin
   // Low-pass filter to reject hand tremors
   val alpha = 0.15f
   smoothedGx += alpha * (rawGx - smoothedGx)
   smoothedGy += alpha * (rawGy - smoothedGy)
   ```
2. Convert device tilt into physics acceleration:
   - Phone tilted right: `+Gx` pushes nodes right.
   - Phone tilted upright: `+Gy` pushes nodes down.

### C. Constraint & Collision Relaxation
- Run 2 to 3 constraint relaxation passes per frame:
  1. **Border Constraints:** Clamp `x` in `[radius, width - radius]` and `y` in `[radius, height - radius]`.
  2. **Inter-Node Collision & Repulsion:**
     ```kotlin
     val dx = nodeB.x - nodeA.x
     val dy = nodeB.y - nodeA.y
     val distSq = dx * dx + dy * dy
     val minDist = nodeA.radius + nodeB.radius + padding
     if (distSq < minDist * minDist && distSq > 0f) {
         val dist = sqrt(distSq)
         val overlap = 0.5f * (minDist - dist)
         val nx = dx / dist
         val ny = dy / dist
         nodeA.x -= nx * overlap
         nodeA.y -= ny * overlap
         nodeB.x += nx * overlap
         nodeB.y += ny * overlap
     }
     ```

### D. User Interaction
- **Tap:** Direct launch of the selected celestial body with a supernova expand animation.
- **Drag & Fling:** Dragging a node sets its position and gives it instant velocity on release.
- **Pinch-to-Zoom:** Scales the celestial map between Solar System (top apps) and Galaxy View (all installed apps).

---

## 4. Claude Implementation Checklist
- [ ] Create `ui/modes/celestial/OrbitOsScreen.kt`.
- [ ] Implement `ui/modes/celestial/physics/VerletSimulation.kt` with dynamic mass and boundary constraints.
- [ ] Integrate `SensorHub` gravity vector with `remember` simulation loop tied to `withFrameNanos`.
- [ ] Render high-performance vector starfield and glowing planetary app nodes on Compose `Canvas`.
- [ ] Implement node tap to launch app with haptic starlight pulse.
