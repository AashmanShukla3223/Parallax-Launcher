# Parallax Launcher - Style 4: Cyberdeck / Subsystem 0 Implementation Guide

> **Design Persona:** Military tactical terminal, Gibsonian cyberspace deck, retro-futuristic telemetry console.
> **Vibe:** Phosphor green/amber monochrome or cold cyber-cyan, CRT curvature, horizontal scanlines, chromatic aberration, real-time hardware telemetry graphs, and vector targeting reticles.

---

## 1. Aesthetic & Hardware Paradigms
- **Color Palette:**
  - CRT Substrate: Deep terminal black (`#020403`).
  - Terminal Phosphors: High-voltage terminal green (`#00FF66`, `#00CC44`) or Amber Deck (`#FFB000`).
  - Alert Warnings: Saturated laser red (`#FF003C`) and tactical yellow (`#FFE500`).
- **Typography:**
  - Strict monospaced console glyphs, VT220 style, bracketed status codes (`[OK]`, `[SYS_INIT]`, `[SIG_LOST]`).
- **Tactility & Audio-Visuals:**
  - Subtle CRT screen flicker and jitter.
  - Low-frequency mechanical relays and digital click feedback for selections.
  - Real-time live system stats from Linux `/proc/stat`, `ActivityManager`, and `TrafficStats`.

---

## 2. Component Architecture

```
CyberdeckScreen (Root Composable)
 ├── BackgroundLayer: CRTShaderHost
 │    ├── AGSL Scanlines & Phosphor Glow Shader
 │    ├── Barrel Distortion (Simulating curved glass cathode ray tube)
 │    └── RGB Chromatic Aberration
 ├── UpperDeck: HardwareTelemetryHUD
 │    ├── Real-time CPU Activity Graph (Rolling oscilloscope waveform)
 │    ├── RAM Allocation Matrix (Used vs Total visualized as segmented memory bank)
 │    └── Network Rx/Tx Telemetry (Live data transfer rates via TrafficStats)
 ├── MidDeck: AppTacticalIndex
 │    ├── Vector Targeting Reticle (Locks onto hovered or focused app)
 │    ├── Hexadecimal Memory Offsets (Apps listed with simulated/real hash memory addresses)
 │    └── Command Line Filter Input (Direct console prompt search)
 └── LowerDeck: SubsystemStatusConsole
      └── Continuous scrolling system log stream (Battery mV, temperature, sensor tick log)
```

---

## 3. Mathematical & Technical Specifications

### A. AGSL CRT Scanline & Barrel Distortion Shader
AGSL fragment shader applied to the entire screen container:

```kotlin
@Language("AGSL")
const val CRT_TERMINAL_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    uniform shader uContent;
    
    // Barrel distortion for curved screen
    float2 curve(float2 uv) {
        uv = (uv - 0.5) * 2.0;
        uv *= 1.1;
        uv.x *= 1.0 + pow((abs(uv.y) / 5.0), 2.0);
        uv.y *= 1.0 + pow((abs(uv.x) / 4.0), 2.0);
        uv = (uv / 2.0) + 0.5;
        return uv;
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / uResolution;
        float2 curvedUv = curve(uv);
        
        // Discard out of bounds of curved screen
        if (curvedUv.x < 0.0 || curvedUv.x > 1.0 || curvedUv.y < 0.0 || curvedUv.y > 1.0) {
            return half4(0.0, 0.0, 0.0, 1.0);
        }
        
        // Sample content with slight chromatic aberration
        float r = uContent.eval((curvedUv - float2(0.002, 0.0)) * uResolution).r;
        float g = uContent.eval(curvedUv * uResolution).g;
        float b = uContent.eval((curvedUv + float2(0.002, 0.0)) * uResolution).b;
        
        // Scanlines
        float scanline = sin(curvedUv.y * uResolution.y * 1.5) * 0.08;
        half3 color = half3(r, g, b) - scanline;
        
        // Vignette
        float vignette = curvedUv.x * curvedUv.y * (1.0 - curvedUv.x) * (1.0 - curvedUv.y);
        color *= clamp(pow(16.0 * vignette, 0.25), 0.0, 1.0);
        
        return half4(color, 1.0);
    }
"""
```

### B. Hardware Telemetry Collectors
1. **Network KB/s Polling:**
   ```kotlin
   var lastRx = TrafficStats.getTotalRxBytes()
   var lastTime = SystemClock.elapsedRealtime()
   // In 1-second coroutine loop:
   val currentRx = TrafficStats.getTotalRxBytes()
   val currentTime = SystemClock.elapsedRealtime()
   val speedKbps = ((currentRx - lastRx) * 1000f) / (currentTime - lastTime) / 1024f
   ```
2. **CPU Oscilloscope Canvas:**
   - Maintain a circular buffer of the last 60 CPU/Bandwidth samples.
   - Draw an anti-aliased green phosphor waveform with glow using `DrawScope.drawPath` and `BlendMode.Screen`.

---

## 4. Claude Implementation Checklist
- [ ] Create `ui/modes/cyberdeck/CyberdeckScreen.kt`.
- [ ] Implement `ui/modes/cyberdeck/shaders/CrtShader.kt` with AGSL barrel distortion & scanlines.
- [ ] Build `ui/modes/cyberdeck/components/OscilloscopeGraph.kt` for rolling hardware telemetry.
- [ ] Implement tactical vector reticle tracking when navigating apps.
- [ ] Connect `TelemetryService` (RAM, CPU, `TrafficStats`, Battery) to the HUD gauges.
