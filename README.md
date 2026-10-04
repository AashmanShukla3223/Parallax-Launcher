# Parallax Launcher

An enthusiast Android launcher built with **Jetpack Compose**, featuring 5 distinct, tactile instrument panel modes. Each mode is a completely different aesthetic and mental model for interacting with your apps, hardware telemetry, sensors, and camera.

---

## 5 Instrument Panels

### 01 · INDUSTRIAL RIG
- **Knurled Rotary Dial**: High-inertia dial with rotational physics (`atan2` angle tracking) mapped to app index detents.
- **Mechanical Arm Toggle**: Two-position safety interlock switch with physical spring resistance.
- **Tactile LAUNCH**: Gated execution button that only activates when safety is armed.
- **Amber OLED Subsystem**: Real-time hardware telemetry including microvolt battery voltage, CPU frequency, RAM utilization, and system temperature.

### 02 · CELESTIAL ORBITOS
- **Planetary Physics Simulation**: Custom Verlet particle physics where apps orbit as gravitational celestial bodies.
- **Accelerometer Drift**: Planetary orbits and multi-layer starfields react live to physical phone tilt.
- **Mass Scaling**: Mass and gravitational pull scale dynamically based on launch frequency.

### 03 · SWISS BROADSHEET
- **Editorial Typography**: Typographic newsprint aesthetic with dynamic headline scaling.
- **AGSL Paper Grain**: Custom Tiramisu RuntimeShader simulating paper grain texture and vignette (with precomputed speckle fallback on older Android versions).
- **AP/Reuters Live Wire**: NotificationListener integration translating notifications into editorial wire tickers.

### 04 · CYBERDECK HUD
- **Tactical Telemetry**: Real-time oscilloscope waveform graphs plotting CPU load and network throughput.
- **Memory Banks**: Real-time segmented RAM visualizer.
- **AGSL CRT Shader**: Hardware-accelerated chromatic aberration, scanlines, barrel distortion, and phosphor bloom (with lightweight scanline fallback on Android < 13).
- **Hex Reticle & CLI Filter**: Command-line prompt filtering apps by hex address and package tags.

### 05 · CINECAM RANGEFINDER
- **Viewfinder HUD**: CameraX live sensor feed overlaid with exposure and focus metrics (f/1.4, 1/250s, ISO 400).
- **Volume Shutter Binding**: Dual-stage physical volume rocker shutter (half-press locks AF/exposure, release launches app).
- **Live Luminance Histogram**: Subsampled real-time 64-bin exposure histogram.
- **Artificial Horizon**: Real-time gyroscopic roll leveling with zero-detent haptic feedback.

---

## Customization System

- **Global Accent Colors**: Amber, Cyan, Emerald, Crimson, Solar Gold, Clean White.
- **Background Schemes**: Obsidian, Charcoal, Deep Space, Newsprint Paper.
- **Dynamic Typography**: Monospace, Sans-Serif, Serif, System default font families propagated across all screens.
- **Per-Style Tunables**: Detent angles, gravity constants, max orbit nodes, aspect ratios (2.39:1 / 3:2), framing grids, and CRT toggles.

---

### 07 · MOTOROLA RAZR V3i CLAMSHELL
- **Working Flip Hinge**: Tap the hinge to open or close the clamshell. Closing it hands the display over to the 96 x 80 external CSTN panel; opening it restores the 2.2" 176 x 220 internal TFT.
- **Pixel-Perfect Panels**: Both screens render at their true aspect ratio with a drawn pixel lattice, so type reads like a 2005-era LCD rather than modern text.
- **Laser-Etched Keypad**: Electric-blue groove network traced across the deck exactly as on the handset, plus the moulded four-way rocker, centre-select, green send and red power keys, and the real 12-key layout.
- **Authentic Status Bar**: Signal bars, GPRS, roam, message, ring-style and battery indicators, per page 46 of the V3i user guide.
- **4-Digit Unlock Code**: Default `1234` as documented in the manual, entered on the keypad.
- **Ring Styles**: Loud / Soft / Vibrate / Vibe & Ring / Vibe then Ring / Silent, cycled with the rocker like the original.
- **Four Quartz Finishes**: Dark, Silver, Blue and Rose — recolours the whole chassis, keypad and display bezel.
- **Real Telephony**: `InCallService` + `TelecomManager`, live talk-time timer and per-minute tariff billing.

---

## Notification Routing (Mode 7)

Alerts are routed differently depending on who owns the screen:

| Situation | Behaviour |
|---|---|
| Mode 7 is the foreground app | The posting app's notification is taken down and re-rendered in-app as a 2G-styled **`1 NEW MESSAGE RECEIVED`** banner (or `N NEW MESSAGES RECEIVED`), with the sender and body. The message indicator stays lit in the status bar until the inbox is opened. |
| Another app is in front | Nothing is intercepted — the posting app's **normal Android notification** posts and displays as usual. |
| Any other Parallax mode | Untouched, same as above. |

Tapping the hinge to close the flip mirrors this on the cover display, which shows `N NEW MSG` or `X MISSED CALLS` exactly as the manual describes.

Implementation lives in `core/notifications/NotificationFeed.kt`. `MainActivity` publishes foreground/background transitions via `setLauncherForeground(...)` and the active mode via `setActiveMode(...)`; `ParallaxNotificationListener` consults `NotificationFeed.intercepting` before deciding whether to call `cancelNotification(...)`.

Grant notification access via **CFG → OPEN MESSAGE NOTIFICATION ACCESS**.

---

## Architecture & Tech Stack

- **Framework**: Jetpack Compose & Material 3
- **Language**: Kotlin 2.x
- **Target OS**: `minSdk 27` (Android 8.1 Oreo) to `targetSdk 37`
- **Sensors & Telemetry**: `SensorManager` (gyroscope & accelerometer), `BatteryManager`, `TrafficStats`, `ActivityManager`
- **Optics**: CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Graphics & Shaders**: Android Graphics Shading Language (AGSL) `RuntimeShader` with backward-compatible Compose renderers

---

## Building

```bash
# Clone the repository
git clone https://github.com/AashmanShukla3223/Parallax-Launcher.git
cd Parallax-Launcher

# Build debug APK with Gradle
./gradlew assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## CI / Automated Testing

Automated builds and unit tests run on every push and pull request via [GitHub Actions](.github/workflows/android.yml). Tested on Ubuntu with JDK 17.
