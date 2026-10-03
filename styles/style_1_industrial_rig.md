# Parallax Launcher - Style 1: Industrial Rig Implementation Guide

> **Design Persona:** Dieter Rams / Teenage Engineering / Braun instrumentation.
> **Vibe:** Matte dark anodized aluminum, orange/amber segmented OLED/VFD, tactile knurled rotary dials, heavy mechanical safety toggles, mechanical relays, and tactile haptic feedback.

---

## 1. Aesthetic & Hardware Paradigms
- **Color Palette:**
  - Background: Deep matte charcoal / bead-blasted dark aluminum (`#121314` / `#1A1B1E`).
  - Text & Accents: High-contrast safety amber/orange (`#FF9500`, `#FF5500`), muted warm white (`#E5E5EA`), technical cyan (`#00E5FF`).
  - Hardware Metals: Knurled metal shadows (`#0A0A0B`), specular metallic highlights (`#3A3D42`).
- **Typography:**
  - Monospaced technical typography (e.g., `FontFamily.Monospace`), DIN-like industrial lettering, uppercase labeling with wide letter-spacing (`tracking = 0.15.sp`).
- **Tactility:**
  - Every interaction has physical resistance, rotational inertia, detents, and mechanical arming mechanisms.

---

## 2. Component Architecture

```
IndustrialRigScreen (Root Composable)
 ├── TopSection: AmberOledPanel
 │    ├── Clock & Date (HH:mm:ss.SS)
 │    ├── Telemetry Readouts (Battery mV, Charging state, Memory/CPU stat)
 │    └── Active App Card (Large mono app title, package ID, launch count)
 ├── MidSection: MechanicalControlDeck
 │    ├── ArmingSafetyToggle (Arm/Disarm mechanical flip switch with guard)
 │    └── LaunchButton (Big heavy industrial momentary switch, enabled only when armed)
 └── BottomSection: KnurledRotaryDial
      ├── DialFace (Circular canvas with knurled tick marks, angle indicator)
      ├── Atan2 Gesture Recognizer (Tracks drag angle + angular momentum)
      └── Detent Haptic Driver (Fires tick vibration whenever angle crosses index threshold)
```

---

## 3. Mathematical & Technical Specifications

### A. Knurled Rotary Dial (`atan2` Polar Math)
1. **Touch Angle Tracking:**
   ```kotlin
   val center = Offset(size.width / 2f, size.height / 2f)
   val touchVector = change.position - center
   val rawAngleRad = kotlin.math.atan2(touchVector.y, touchVector.x)
   val rawAngleDeg = Math.toDegrees(rawAngleRad.toDouble()).toFloat()
   ```
2. **Delta & Continuous Rotation:**
   - Compute angular delta between consecutive pointer positions:
     ```kotlin
     var delta = currentAngle - previousAngle
     if (delta > 180f) delta -= 360f
     else if (delta < -180f) delta += 360f
     totalRotationDegrees += delta
     ```
3. **App Index Snapping & Detents:**
   - Degrees per app slot: `val step = 360f / max(apps.size, 1)` or fixed angular detent (e.g., `15°` per app).
   - Whenever `totalRotationDegrees / step` crosses an integer boundary:
     - Update selected `AppInfo`.
     - Trigger `HapticFeedbackType.TextHandleMove` or `VibrationEffect.createPredefined(EFFECT_TICK)`.
4. **Momentum & Decay:**
   - Track angular velocity with `VelocityTracker`.
   - On drag release, apply exponential decay animation (`Animatable.animateTo` with friction) to settle on the nearest detent.

### B. Mechanical Safety Arm Toggle
- Two-stage launch authorization:
  1. The user flips up the armed guard / toggles the switch to `ARMED`.
  2. The OLED screen flashes `ARMED / READY`.
  3. The `LAUNCH` button becomes active.
  4. Haptic: `VibrationEffect.createPredefined(EFFECT_HEAVY_CLICK)`.
  5. Tapping `LAUNCH` starts the selected intent and resets arming state.

### C. Amber OLED Panel
- Rendered with an amber glow border and scanline/subpixel dot matrix look.
- Live sticky battery broadcast listener:
  - Voltage: `intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)` (e.g., `4120 mV`).
  - Level: `intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)` %.
  - Temperature: `intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f` °C.

---

## 4. Claude Implementation Checklist
- [ ] Create `ui/modes/industrial/IndustrialRigScreen.kt`.
- [ ] Create `ui/modes/industrial/components/KnurledRotaryDial.kt` using `Canvas` and `pointerInput`.
- [ ] Create `ui/modes/industrial/components/AmberOledPanel.kt` with monospace telemetry.
- [ ] Create `ui/modes/industrial/components/SafetyArmToggle.kt` with custom spring-loaded toggle graphics.
- [ ] Wire to `AppsRepository` for real app list cycling and intent launching.
- [ ] Ensure `HapticEngine` triggers distinct physical clicks for dial detents vs. toggle flips.
