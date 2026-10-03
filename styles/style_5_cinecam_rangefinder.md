# Parallax Launcher - Style 5: CineCam / Rangefinder Implementation Guide

> **Design Persona:** Leica M-series / Fujifilm X-Pro optical rangefinder meets cinema camera monitor (ARRI / RED).
> **Vibe:** Live optical camera background with letterbox framing (2.39:1 / 3:2), real-time RGB/luminance histogram, artificial horizon spirit level, frameline markers, and dual-stage shutter app launcher.

---

## 1. Aesthetic & Hardware Paradigms
- **Color Palette:**
  - Viewfinder Overlay: Stealth matte camera body black (`#0D0E10`) with cinema letterbox cutouts.
  - OSD & Reticles: High-visibility optical yellow (`#FFEE00`), horizon green (`#00FF88`), framing white (`#FFFFFF`).
  - Histogram Channels: Red (`#FF3B30`), Green (`#34C759`), Blue (`#007AFF`), Luminance (`#EBEBF5`).
- **Typography:**
  - Technical camera engraving font (sharp neo-grotesque or condensed DIN), aperture/shutter speed notation (`f/1.4`, `1/250s`, `ISO 400`, `24 FPS`, `T2.0`).
- **Tactility & Hardware Mapping:**
  - Remap device volume rocker (via `onKeyDown` / `onKeyUp`) as a dual-stage shutter button:
    - Half-press / volume hold: Locks focus reticle and freezes frame.
    - Full-press / volume release: Snaps shutter and launches armed app.

---

## 2. Component Architecture

```
CineCamScreen (Root Composable)
 ├── Background: CameraXPreviewContainer
 │    ├── PreviewView (Live camera sensor feed)
 │    └── LetterboxMask (Cinema aspect ratio crop: 2.39:1 Anamorphic or 3:2 Full Frame)
 ├── OSDHUDLayer: RangefinderHUD
 │    ├── GyroSpiritLevel (Artificial horizon indicator driven by SensorHub pitch/roll)
 │    ├── Framelines & Rule-of-Thirds Grid
 │    ├── Real-Time RGB/Luminance Histogram (Calculated from ImageAnalysis YUV buffer)
 │    └── Camera Diagnostics Bar (Battery voltage formatted as camera battery %, storage capacity)
 └── AppRangefinderDeck: FocusRingSelector
      ├── FocusDistanceTrack (Apps arranged along a camera focus distance scale: 0.7m, 1m, 1.5m, 3m, ∞)
      └── ShutterTriggerMechanism (Rotary ISO/Shutter dial or dual-stage shutter release)
```

---

## 3. Mathematical & Technical Specifications

### A. CameraX Pipeline & Live Histogram Analysis
1. Configure `CameraX` with `ImageAnalysis` (using `STRATEGY_KEEP_ONLY_LATEST` to avoid lag):
   ```kotlin
   val imageAnalysis = ImageAnalysis.Builder()
       .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
       .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
       .build()
   ```
2. Compute Luminance Histogram:
   - Sample Y-plane buffer (stride subsampled by 4 or 8 for zero-frame-drop performance):
   ```kotlin
   val histogram = IntArray(256)
   val yBuffer = image.planes[0].buffer
   val step = 8 // Subsample for efficiency
   for (i in 0 until yBuffer.remaining() step step) {
       val y = yBuffer.get(i).toInt() and 0xFF
       histogram[y]++
   }
   ```
3. Render histogram as a translucent vector graph in the lower corner.

### B. Gyro Artificial Horizon Spirit Level
1. From `SensorHub` pitch and roll:
   - Roll angle $\theta$ rotates the horizon line:
     ```kotlin
     rotate(degrees = -rollAngle, pivot = center) {
         drawLine(
             color = if (abs(rollAngle) < 0.5f) Color(0xFF00FF88) else Color(0xCCFFFFFF),
             start = Offset(center.x - 120.dp.toPx(), center.y + pitchOffset),
             end = Offset(center.x + 120.dp.toPx(), center.y + pitchOffset),
             strokeWidth = 2.dp.toPx()
         )
     }
     ```
   - When $\theta \approx 0^\circ$ (within $\pm 0.5^\circ$), snap to level with a subtle haptic detent and turn crosshair green.

### C. Lens Focus Ring App Selector
- Apps are indexed along a physical focus ring scale (e.g., `0.3m`, `0.5m`, `0.7m`, `1.2m`, `3m`, `∞`).
- Turning the focus ring shifts the selected app into sharp focus while blurring/scaling neighboring items.

---

## 4. Claude Implementation Checklist
- [ ] Add CameraX dependencies (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) if camera mode is active.
- [ ] Create `ui/modes/cinecam/CineCamScreen.kt`.
- [ ] Implement `ui/modes/cinecam/components/RangefinderOverlay.kt` with letterbox bars and framelines.
- [ ] Implement `ui/modes/cinecam/components/ArtificialHorizon.kt` linked to `SensorHub`.
- [ ] Implement `ui/modes/cinecam/components/LuminanceHistogram.kt` with optimized subsampling.
- [ ] Build the focus ring app carousel and shutter trigger launcher.
