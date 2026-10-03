# Parallax Launcher - Style 3: Swiss Editorial / Brutalist Broadsheet Implementation Guide

> **Design Persona:** International Typographic Style (Josef Müller-Brockmann) meets Brutalist Broadsheet Newspaper.
> **Vibe:** High contrast, strict modular grid, zero standard app icons, massive kinetic display typography, newsprint paper grain, and notification-driven live headlines.

---

## 1. Aesthetic & Hardware Paradigms
- **Color Palette:**
  - Newsprint Mode: Cream/unbleached paper (`#F4F1EA`), pure printer ink black (`#111111`), vermilion red editorial accents (`#E63946`).
  - Dark Broadsheet Mode: Deep obsidian (`#101010`), stark off-white (`#EEEEEE`), electric editorial yellow (`#FFE600`).
- **Typography:**
  - High-impact Serif & Neo-grotesque Sans pairing:
    - Headlines: Bold compressed serif / slab (Playfair / Bodoni / Georgia style).
    - Grid & Metadata: Tight, architectural Swiss neo-grotesque sans (Helvetica / Inter / Archivo).
- **Iconoclastic Philosophy:**
  - Strictly **NO app icons**. Apps are treated as editorial entities, sections, or kinetic headlines formatted with typographic hierarchy.

---

## 2. Component Architecture

```
SwissBroadsheetScreen (Root Composable)
 ├── Background: NewsprintPaperCanvas
 │    ├── Procedural AGSL Paper Grain / Halftone Rosette Shader
 │    └── Strict Column Grid Lines (1px hairline rules separating sections)
 ├── Masthead: NewspaperHeader
 │    ├── Publication Name: "THE DAILY PARALLAX" or "CHRONICLE"
 │    ├── Dateline: Real-time kinetic date, weather/battery edition number (e.g., "EDITION NO. 4120 mV")
 │    └── Top Headline Ticker: Active notification intercepted or urgent breaking alert
 ├── ContentDeck: ModularGridContent
 │    ├── LeadStory (Top 1-2 most frequently used apps styled as front-page lead articles)
 │    ├── ColumnistSection (Horizontal or vertical 3-column typography index of categories/apps)
 │    └── ClassifiedsSection (Alphabetical / search index styled as newspaper classifieds ads)
 └── Footer: PressStatusStrip
      └── Print run metadata, system telemetry formatted as printer press diagnostics
```

---

## 3. Mathematical & Technical Specifications

### A. Procedural AGSL Paper Grain Shader
On Android 13+ (API 33+), use `RuntimeShader` with AGSL to inject tactile tactile newsprint texture without bitmap asset overhead:

```kotlin
@Language("AGSL")
const val PAPER_GRAIN_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    
    // Hash function for procedural noise
    float hash(float2 p) {
        p = fract(p * float2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
    }
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / uResolution;
        float noise = hash(fragCoord + float2(uTime * 0.001, 0.0));
        
        // Base newsprint tint (#F4F1EA)
        half3 paper = half3(0.957, 0.945, 0.918);
        
        // Subtle micro-fiber variation
        paper -= (noise * 0.035);
        
        return half4(paper, 1.0);
    }
"""
```
*(Fallback for API < 33: Multiplied subtle Compose `Canvas` noise pass).*

### B. Kinetic Typography Hierarchy
1. **Dynamic Type Sizing:**
   - Lead app is sized dynamically based on character count to fill the column width (`hyphenation` and auto-scaling font size).
   - Secondary apps are arranged in a multi-column asymmetric grid.
2. **Notification Interception to Headlines:**
   - Read active notifications (via `NotificationListenerService`).
   - Format: `"[SENDER] SAYS: [MESSAGE TITLE]"` rendered as a sensational front-page banner. Tapping the headline opens the notifying app directly.

### C. Kinetic Scrolling & Haptics
- As the broadsheet scrolls, hairline separator rules snap with delicate micro-haptics (`HapticFeedbackType.TextHandleMove`).
- Touch down on an article inverts the section (black-on-cream flips to cream-on-black) before launching.

---

## 4. Claude Implementation Checklist
- [ ] Create `ui/modes/swiss/SwissBroadsheetScreen.kt`.
- [ ] Implement `ui/modes/swiss/shaders/PaperGrainShader.kt` with AGSL `RuntimeShader` (and fallback for older APIs).
- [ ] Create `ui/modes/swiss/components/Masthead.kt` with dynamic edition numbers and battery metadata.
- [ ] Build the dynamic editorial grid in `ui/modes/swiss/components/EditorialGrid.kt`.
- [ ] Integrate notification interception or recents into the breaking news banner.
