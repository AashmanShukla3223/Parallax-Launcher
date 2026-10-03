# Parallax Launcher - Handoff

Read `PROJECT_BLUEPRINT.md` for the full spec. Where this file and the blueprint disagree, this file reflects the real project.

## Real project values (blueprint differs)
- Package / namespace: `com.parallax.parallaxlauncher`
- minSdk 27, targetSdk 37, compileSdk 37
- Stack: Kotlin, Jetpack Compose, Material 3. No extra dependencies added.
- Source: `app/src/main/java/com/parallax/parallaxlauncher/` (`MainActivity.kt` is still the default Greeting template, plus `ui/theme/`)

## Done so far
- Added a `<queries>` block (MAIN/LAUNCHER intent) to `app/src/main/AndroidManifest.xml` so the app can list installed apps (Android 11+).

## Not done yet
- HOME intent-filter in the manifest (`android.intent.action.MAIN` + `CATEGORY_HOME` + `CATEGORY_DEFAULT`). Without it the app cannot be chosen as the home screen.
- `AppsRepository` (PackageManager query).
- Mode 1, Industrial Rig (Compose):
  - Knurled rotary dial with `atan2` drag and per-app detents (CLOCK_TICK haptic).
  - Mechanical arm toggle (LONG_PRESS haptic) that gates a LAUNCH key.
  - Amber OLED panel: selected app, clock, battery mV (sticky `ACTION_BATTERY_CHANGED`), CPU MHz (sysfs read with graceful fallback).
- Modes 2-5 and the mode switcher.

## Machine notes
- `local.properties` is machine-specific (SDK path). Delete it or let your tooling regenerate it.
- Chromebook (4 GB RAM, 2 GB to Linux): do not run Android Studio. Keep Gradle memory low, e.g. `org.gradle.jvmargs=-Xmx1g` in `gradle.properties`, or build the APK on the Windows PC and copy `app-debug.apk` over.
- Building from the CLI needs a JDK and the Android command-line SDK, then `./gradlew assembleDebug`.

## Laya MCP (optional)
- `laya_mcp_server.py` (in this folder) is a local MCP server around the `laya` pip package (`python -m pip install laya mcp`; uses `mcp` v2 `MCPServer`).
- Tools: `laya_score`, `laya_choice`, `laya_noul`, `laya_critic`, `laya_audit_tool_call`. No fake fallback: failures return errors.
- Caveat: tested here, Laya barely separates safe from dangerous actions (P(safe) 0.23 for deleting `C:\Windows` vs 0.23 for writing a Kotlin file) and warns its confidence is uncalibrated. Treat it as a weak signal, not a safety gate.
- Needs PyTorch; may be heavy on 2 GB RAM.

## Cleanup
- `optimize_network.ps1` was never run and is unlikely to help. Delete it.
- A TypeSafe API key was pasted in an earlier chat. Revoke it.
