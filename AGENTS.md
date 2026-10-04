# AGENTS.md

Android launcher (Jetpack Compose) with 7 switchable "mode" home screens. Single Gradle module `:app`, package `com.parallax.parallaxlauncher`.

## Build & verify

```bash
./gradlew :app:assembleDebug        # APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest    # compile smoke test only (see Testing)
./gradlew :app:compileDebugKotlin   # fastest way to see compile errors
```

Windows: use `.\gradlew.bat`. There is no `gradlew` shell script that works here.

### The JDK trap — read this before diagnosing anything

`JAVA_HOME` points at **Red Hat JDK 8**, and `java -version` on PATH reports 1.8. Neither is used. `gradle/gradle-daemon-jvm.properties` pins `toolchainVersion=25`, so Gradle auto-provisions **Eclipse Temurin JDK 25** into `~/.gradle/jdks/` and builds with that. Auto-download works because `settings.gradle.kts` applies the foojay-resolver plugin.

Consequence: never conclude "the JDK is too old" from `java -version`. Check `.\gradlew.bat -q javaToolchains` instead.

### Daemon is off, so builds are slow and interruptions corrupt the APK

`gradle.properties` sets `org.gradle.daemon=false`, `org.gradle.parallel=false`, `org.gradle.workers.max=2`, and enables the configuration cache. Every invocation forks a single-use daemon (expect ~30–90s, longer for Kotlin compile).

If a build is interrupted (server restart, timeout), the partially written `app-debug.apk` stays on disk. Installing it fails with `INSTALL_PARSE_FAILED_NOT_APK`, which looks like a corrupt build but is actually a truncated file. Delete and rebuild:

```bash
rm app/build/outputs/apk/debug/app-debug.apk && ./gradlew :app:assembleDebug
```

### Validate the APK before installing

A plain `assembleDebug` succeeding does not guarantee the artifact is installable. Check it:

```bash
# SDK path comes from local.properties (gitignored); build-tools live under it
aapt2  dump badging app/build/outputs/apk/debug/app-debug.apk   # parses manifest
apksigner verify app/build/outputs/apk/debug/app-debug.apk     # signature scheme
```

The `release` variant is **unsigned** (`app-release-unsigned.apk`) and cannot be installed — always use debug.

`local.properties` is gitignored and currently points at `sdk.dir=D\:\Games\Android\Sdk`. CI does not use it.

## Testing

There are **no real tests**. `app/src/test/.../ExampleUnitTest.kt` only asserts `2 + 2 == 4`. `testDebugUnitTest` is therefore a compile-level smoke test, not behavioural verification. There is no instrumentation test that runs in CI either.

Consequence: correctness must be verified by building and looking at the app. Don't claim a change is "tested" on the strength of a green `testDebugUnitTest`.

## CI

`.github/workflows/android.yml` — "Android CI", on push/PR to `main`/`master` + manual dispatch. ubuntu-latest, JDK 17 (Temurin), then `testDebugUnitTest` → `assembleDebug` → upload APK artifact.

Note the JDK mismatch with the local toolchain (17 in CI vs pinned 25 locally); it resolves via foojay auto-provisioning. If CI fails on toolchain resolution, that's the first thing to check.

## Architecture

`MainActivity` is a single `setContent` that dispatches on `settings.mode` (1–7) to one screen composable, overlaid with `MODE n` / `CFG` chips. Mode 7 is the Motorola RAZR V3i.

```
core/data        AppsRepository      PackageManager query, launch counts
core/settings    SettingsRepository  SharedPreferences("settings"), exposes StateFlow<Settings>
core/telemetry   TelemetryService     1 Hz battery / CPU / RAM / TrafficStats
core/sensor      SensorHub            gyro + accelerometer
core/haptics     HapticEngine         tick / click / thud
core/notifications NotificationFeed   process-wide bus (see below)
core/telecom     RazrInCallService + CallManager, TonePlayer, TelecomRepository
ui/modes/<n>/    one package per mode screen
ui/theme         palette + typography via CompositionLocal
```

### Process-wide singletons expose Compose state

`CallManager`, `NotificationFeed` and `ShutterEvents` are Kotlin `object`s holding `MutableStateFlow`/`mutableStateOf`. UI reads them with `collectAsState()`. They are global mutable state — mutating them from a service (not a composable) is the intended pattern here.

### Notification routing (Mode 7 only)

`ParallaxNotificationListener` decides per notification:

- launcher foreground **and** active mode == 7 → `cancelNotification(key)` + in-app banner ("1 NEW MESSAGE RECEIVED")
- anything else → posting app's own Android notification left completely untouched

`MainActivity` drives this via `NotificationFeed.setLauncherForeground(...)` in `onResume`/`onPause` and `setActiveMode(...)` in a `LaunchedEffect` keyed on `settings.mode`. If Mode 7 stops intercepting, check those two call sites first.

### Manifest-registered system integration

`MainActivity` is also a **home app** (`CATEGORY_HOME`) and a dialer (`ACTION_DIAL`). It registers:

- `ParallaxNotificationListener` (`BIND_NOTIFICATION_LISTENER_SERVICE`)
- `RazrInCallService` (`BIND_INCALL_SERVICE`)

None of these are granted at install time. Features silently no-op until the user grants:

| Feature | Requires |
|---|---|
| Notification interception | Notification access (manual jump to `ACTION_NOTIFICATION_LISTENER_SETTINGS`) |
| Camera (Mode 5) | `CAMERA` runtime grant |
| Calling / Mode 7 calls | `CALL_PHONE`, `ANSWER_PHONE_CALLS` |
| Being the launcher | `ROLE_HOME` |
| Place calls via `TelecomManager` | `ROLE_DIALER` |

There is deliberately **no `POST_NOTIFICATIONS` permission** — the app never posts notifications, it only intercepts. Don't add it without changing the routing design.

### AGSL shaders are API 33+ with Compose fallbacks

`ui/modes/cyberdeck/shaders/CrtShader.kt` and `ui/modes/swiss/shaders/PaperGrainShader.kt` use `RuntimeShader`, which needs API 33. `minSdk` is 27, so both guard on `Build.VERSION.SDK_INT >= TIRAMISU` and return `null` to fall back to plain Compose drawing. Keep that guard if you touch them.

### SharedPreferences files

| File | Owner | Holds |
|---|---|---|
| `parallax` | `MainActivity` | `onboarded` flag |
| `settings` | `SettingsRepository` | all `Settings` fields |
| `launch_counts` | `AppsRepository` | per-package launch counts |
| `telecom_numbers` | `TelecomRepository` | phonebook state |

**Gotcha:** `RazrInCallService.CallManager.startRinging` reads `SharedPreferences("settings")` and the `ringtoneUri` key **directly**, bypassing `SettingsRepository`. If you rename that key or that file in `SettingsRepository`, you must update the string literal in `RazrInCallService.kt` too — nothing will warn you.

## Mode 7 (RAZR V3i) specifics

Rebuilt against photographs of a real handset plus the supplied manual. Design facts that are easy to undo by accident:

- **Palette is split.** `RazrFinish` is the *enum* (Light/Dark/Blue/Rose Quartz). `RazrSkin` is the *display chrome data class*. `RazrPalette` combines a `RazrHardware` + `RazrSkin`. The names collide conceptually — don't rename one without the other.
- **The screen is a light UI.** Near-white content field, thin navy status strip, pale blue-grey title bar, medium-blue selection bars, bold *sans-serif* mixed-case text. It is not a dark monochrome LCD. Typography is `FontFamily.SansSerif` (the stock handsets used Univers, which is not redistributable).
- **The keypad has no key tiles.** One continuous brushed sheet with the electric-blue etch cut into it, forming three vertical channels — outer two wide, centre one narrow. The narrow centre channel creates the waisted silhouette. Legends mirror at the centre column: letters right of 2/5/8, left of 3/6/9.
- **Panels are pixel-exact**: internal 176×220 (4:5), external 96×80.
- **Icons are drawn on `Canvas`** in `RazrIcons.kt`. Do not substitute monospace/emoji dingbats — that was the single clearest "not a real phone" tell.
- Flip open/closed is a real state: closed removes the inner panel entirely and leaves only the lower shell carrying wallpaper, clock, notifications and message previews. Unlock delegates to the Android credential screen (`KeyguardManager.createConfirmDeviceCredentialIntent`) and only engages while the device itself is locked.

Reference material lives in `Razr V3i References/` (spec docx, 128-page manual PDF, keypad/display photos) and is committed. `RAZR_V3I_SPECS.md` summarises the spec sheet.

> **`isDeviceUnlocked()` does not resolve against this compile SDK.** Use `KeyguardManager.isKeyguardLocked()` instead. If you need the device-unlocked check, don't reach for the former.

### Current state: builds clean

`assembleDebug` and `testDebugUnitTest` both pass. Verified with `aapt2 dump badging` and `apksigner verify --verbose` (v2 scheme, 1 signer).

## Conventions

- Kotlin 2.2.10, Compose BOM, Material 3. `compileSdk`/`targetSdk` = 37, `minSdk` = 27, `compileOptions` = Java 11.
- Compose-only UI; no XML layouts. `buildFeatures.compose = true`, no `viewBinding`.
- AGP 9 `optimization { packageScope = ... }` in the release block — only shrink `androidx.**`, `kotlin.**`, `kotlinx.**`. Adding your own packages to that set changes release behaviour.
- `local.properties`, `.idea/caches`, `/build` are gitignored. `.idea/misc.xml` has been committed before and shows up as unrelated noise in diffs — ignore it.
- CI treats `main` and `master` as the integration branches; the repo is on `main`.