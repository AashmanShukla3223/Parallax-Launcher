# Parallax Launcher (Android)
> An enthusiast multi-engine home screen featuring 5 distinct aesthetic and hardware paradigms.

---

## 1. Project Overview
* **Name:** Parallax (Parallax Launcher)
* **Target Package:** `com.enthusiast.parallax`
* **Stack:** Kotlin, Jetpack Compose, Material 3, Kotlin Coroutines & Flow
* **Target SDK:** Android 14/15 (`compileSdk 35`, `minSdk 26`)
* **Environment:** Android SDK at `D:\Games\Android\Sdk`

---

## 2. Core Architecture
```
                         ┌───────────────────────────────────────────────┐
                         │               Parallax Core                   │
                         │  • AppsRepository (PackageManager query)      │
                         │  • TelemetryService (RAM, Battery, Bandwidth) │
                         │  • SensorHub (Gyro, Accelerometer)            │
                         │  • HapticEngine (Custom Vibrations)           │
                         └──────────────────────┬────────────────────────┘
                                                │ StateFlow
                                                ▼
                         ┌───────────────────────────────────────────────┐
                         │             Active Mode Switcher              │
                         │            (Tactile "Mode Dial")              │
                         └──┬──────────┬──────────┬──────────┬───────────┘
                            │          │          │          │           │
     ┌──────────────────────┘          │          │          │           └──────────────────────┐
     ▼                                 ▼          ▼          ▼                                  ▼
┌──────────────────┐           ┌──────────────┐ ┌──────────┐ ┌───────────────┐          ┌───────────────┐
│ 1. Industrial Rig│           │ 2. Celestial │ │ 3. Swiss │ │ 4. Cyberdeck  │          │ 5. CineCam    │
│    (Dials/OLED)  │           │    OrbitOS   │ │ Broad-   │ │    HUD        │          │    Rangefinder│
│                  │           │  (2D Physics)│ │ sheet    │ │  (Telemetry)  │          │    (Camera)   │
└──────────────────┘           └──────────────┘ └──────────┘ └───────────────┘          └───────────────┘
```

---

## 3. The 5 Interchangeable Modes
1. **Industrial Rig (Braun / Teenage Engineering):**
   - Knurled rotary dials with polar angle momentum (`atan2`).
   - Mechanical toggle switches with heavy haptic thuds.
   - Amber OLED/VFD status screen (battery mV, CPU clock, audio visualizer).
2. **Celestial / OrbitOS (2D Physics & Dynamic Mass):**
   - Floating app nodes with zero-G drift.
   - Real-time 2D Verlet integration physics.
   - Phone tilt gravity vectoring via low-pass filtered IMU (gyro/accelerometer).
   - Frequency-of-use dynamically weights node mass.
3. **Swiss Editorial / Brutalist Broadsheet (Kinetic Typography):**
   - Procedural poster layout (no app icons, bold typography).
   - Live notification interception mapped into newspaper headlines.
   - AGSL runtime paper grain/newsprint shader.
4. **Cyberdeck / Subsystem 0 (Tactical Telemetry HUD):**
   - Live hardware telemetry (`/proc/stat` CPU, RAM, `TrafficStats` network KB/s).
   - AGSL CRT scanline, phosphor glow, and chromatic aberration fragment shaders.
   - Vector targeting reticle and rolling oscilloscope graphs.
5. **CineCam / Rangefinder (Leica / Fuji Camera OS):**
   - Live camera preview background in cinema letterbox (2.39:1 / 3:2).
   - Real-time RGB/luminance histogram calculated from camera YUV stream.
   - Gyro-driven aircraft artificial horizon spirit level.
   - Hardware volume rocker remapped to dual-stage shutter trigger.

---

## 4. Master Learning Objectives (Styles 1 – 5)
* **System Plumbing:** Android Home Intent (`CATEGORY_HOME`), package querying, wallpaper transparency.
* **Math & Physics:** Polar coordinate rotary math, Verlet physics integration, gyro complementary filter.
* **Graphics & Shaders:** AGSL runtime fragment shaders (CRT, grain, bloom), Compose GPU Canvas vector rendering.
* **Low-Level Telemetry:** BatteryManager, TrafficStats, ActivityManager, SensorManager.
* **CameraX & Vision:** Live preview pipelines, YUV ImageAnalysis, real-time luminance histograms.
