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

`apksigner verify` prints nothing on success; check the exit code (`$?`). An unsigned release prints a complaint and exits non-zero.

The `release` variant is signed when signing material is configured (see Release signing below) and is otherwise an `app-release-unsigned.apk` that cannot be installed. For day-to-day device installs use `assembleDebug`.

`local.properties` is gitignored and currently points at `sdk.dir=D\:\Games\Android\Sdk`. CI does not use it.

## Testing

`testDebugUnitTest` is a compile-level smoke test only: `app/src/test/.../ExampleUnitTest.kt` asserts `2 + 2 == 4`. A green one proves nothing about behaviour.

There *is* an instrumentation test that runs in CI — `app/src/androidTest/.../ModeSmokeTest.kt` walks all seven modes on an emulator and asserts each activity reaches RESUMED/STARTED and does not finish. It is still not behavioural verification: Mode 7 gets a lifecycle assertion only (it requests `ROLE_DIALER` on compose, so Espresso cannot drive it), and nothing captures a screenshot.

Consequence: correctness must be verified by building and looking at the app. Don't claim a change is "tested" on the strength of a green `testDebugUnitTest` or a green emulator job.

## CI

`.github/workflows/android.yml` — "Android CI", on push/PR to `main`/`master` + manual dispatch. Three jobs, all `ubuntu-latest`, JDK 17 (Temurin):

| Job | Does |
|---|---|
| `build` | `testDebugUnitTest` → `assembleDebug` → uploads `parallax-launcher-debug` |
| `release` | restore keystore from secret → `assembleRelease` → verify signature → uploads `parallax-launcher-release` |
| `emulator` | `connectedDebugAndroidTest` via `reactivecircus/android-emulator-runner` |

Note the JDK mismatch with the local toolchain (17 in CI vs pinned 25 locally); it resolves via foojay auto-provisioning. If CI fails on toolchain resolution, that's the first thing to check. The `emulator` job needs KVM, which the dev machines do not expose, so it has to run on the runner.

Artifacts expire 90 days after the run. Anything you intend to publish has to be downloaded before then.

### A workflow file is not compiled by anything you can run

**Never gate a whole job on `secrets`.** A job-level `if: ${{ secrets.X != '' }}` makes Actions reject the *entire workflow file*: the run is created, no jobs start, and it reports "This run likely failed because of a workflow file issue". Commit `a4728f0` did this and killed CI; the workflow parsed fine in Python with every key schema-valid, so local checks passed while the pipeline was dead. The `secrets` context is not available in `jobs.<id>.if` at all.

Gate the individual **steps** instead — the current `release` job reads the secret in one unconditional "Detect signing key" step and gates the five signing steps on `steps.signing.outputs.available`. A fork has no secrets, so that step emits `::notice::` and the job passes without producing a release APK.

**Verified, on both branches.** A run with the secret present executes all five signing steps and uploads the APK. A run forced down the skip path left `Detect signing key` green, marked all five signing steps `skipped`, passed the job green, and produced only `parallax-launcher-debug` — no unsigned release artifact leaked. Do not test this by deleting `ANDROID_KEYSTORE_BASE64`: the keystore exists only on the original dev machine, so the secret could not be restored.

Corollary: **verify workflow changes by watching a real run**, never by parsing the YAML locally.

## License

**GPL-3.0** (`LICENSE`, with `NOTICE` for the third-party material). Consequence for anyone working here: **do not report a missing licence** — it was added deliberately. Code released from this repo is GPL-3.0 permanently, so do not copy in code whose licence is incompatible, and do not relicense existing files.

`Razr V3i References/` is **not** covered by the GPL. Those are Motorola's files, committed for reproducibility, and they will be inherited by forks. Do not edit them, and do not treat them as project-owned.

## Release signing

The release key is RSA4096, 10000 days, alias `parallax`, `CN=Parallax Launcher, OU=Parallax, O=Parallax, C=IN`. Its certificate SHA-256 digest is:

```
1972fac3889a398199aa503fef7dd5c4970548608b45bbae8dbf5bf4c2babd82
```

That digest is the load-bearing number. Android only offers an update over an installed app if the signing certificate matches, so **anything published under it is permanent** — a new key means new installs only, and existing users cannot upgrade.

**Back the keystore and its passwords up off this machine.** `parallax-release.jks` and `keystore.properties` are both gitignored and exist nowhere else. Losing them means the app can never be updated by anyone.

`app/build.gradle.kts` takes signing material from `keystore.properties` at the repo root if present (how a developer machine signs), otherwise from `ANDROID_*` environment variables (which CI populates from secrets). With neither it leaves the release unsigned and logs a warning naming both sources.

Four repository secrets:

| Secret | Contents |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64 of `parallax-release.jks` |
| `ANDROID_STORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `parallax` |
| `ANDROID_KEY_PASSWORD` | key password |

Never commit the key or the passwords, and never paste the password into a prompt, a commit, or a log.

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
| Mode 7 Phonebook (DOWN on standby) | `READ_CONTACTS` runtime grant |
| Being the launcher | `ROLE_HOME` |
| Place calls via `TelecomManager` | `ROLE_DIALER` |

Reading the SIM operator name for Mode 7 deliberately does **not** add `READ_PHONE_STATE`; it is a best-effort read in `simOperator2G` that degrades to `MOTOROLA`. Do not add that permission for a status-strip label.

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
- **The keypad has no key tiles.** One continuous brushed sheet with the electric-blue etch cut into it. In the numeric block there are **no vertical walls at all** — one continuous groove crosses each row boundary, stepping down into a narrow centre channel and back out again. That pinch is what gives the deck its waisted silhouette. Legends mirror at the centre column: letters right of 2/5/8, left of 3/6/9. Do not reintroduce per-key rectangles; drawing a vertical line down each channel edge plus a horizontal line per row is exactly what made it read as a grid of tiles. Channel geometry lives in one `Channels` object shared by the paint and the tap targets.
- **Panels are pixel-exact**: internal 176×220 (4:5), external 96×80.
- **Icons are drawn on `Canvas`** in `RazrIcons.kt`. Do not substitute monospace/emoji dingbats — that was the single clearest "not a real phone" tell.
- Flip open/closed is a real state: closed removes the inner panel entirely and leaves only the lower shell carrying wallpaper, clock, notifications and message previews. Unlock delegates to the Android credential screen (`KeyguardManager.createConfirmDeviceCredentialIntent`) and only engages while the device itself is locked.

Reference material lives in `Razr V3i References/` (spec docx, 128-page manual PDF, keypad/display photos) and is committed. `RAZR_V3I_SPECS.md` summarises the spec sheet.

> **`isDeviceUnlocked()` does not resolve against this compile SDK.** Use `KeyguardManager.isKeyguardLocked()` instead. If you need the device-unlocked check, don't reach for the former.

### Current state

`main` builds and all three CI jobs are green. v1.0.0 is published from tag `v1.0.0`, carrying the CI-built `app-release.apk`.

**Keypad** — fixed. The numeric block is drawn as five serpentine grooves with no vertical channel walls, so it reads as one etched sheet rather than a grid of tiles. Verified by screenshot on a real device against `Razr V3i References/keypad close up.jpeg`.

**Calls** — fixed. `placeCall()` used to set the in-call view unconditionally, even when `TelecomManager.placeCall` threw. Worse, `placeCall` can *succeed* while the call is refused downstream: `RazrInCallService` never attaches, `CallManager.call` stays null, and the deck sat on a fabricated `CONNECTED` screen indefinitely. There is now a watchdog that falls back to standby, and `callStatus` reports `CALLING...` rather than `CONNECTED` when there is no call behind it.

**Emergency numbers** — audited against Wikipedia plus a second source for every suspicious row; 20 corrections applied (Turkmenistan's obsolete Soviet codes collapsing to the bare digits 1–4 was the worst, plus Niue/Tokelau international numbers transcribed as local codes, Thailand missing 1669, and ten missing countries). Turkey, Bangladesh, Qatar, Saudi and UAE per-service codes were deliberately left alone — plausible but unconfirmed against a national source.

- **The middle soft key is a legend, not a touch target — and the whole panel is.** The soft-key bar under the display draws `CLEAR / SEND / CANCEL`, but nothing inside the upper half accepts touches: tapping the words does nothing, and adding `clickable` to `RazrSoftKeyBar` does not fix it (verified on device). The deck below is the only input surface — flank brackets, rocker, digits. The hinge sits *outside* `RazrUpperShell` and does respond, which is the clue: the blocker is inside `RazrPixelScreen`, whose subpixel `Canvas` is the last child and covers the whole panel. Root cause is not yet pinned down; do not add handlers to `RazrSoftKeyBar` and assume they work.
- **Mode 7 has no unlock code of its own.** `*` opens `createConfirmDeviceCredentialIntent` so the user types their real phone PIN. The old 4-digit `settings.razrUnlockCode` and the in-app `PHONE LOCKED` panel are both gone — a second code to forget on a phone that already has a lock screen was strictly worse, and with nothing to enter the panel could only mislead when `isKeyguardLocked()` reported a false positive. `settings.razrUnlockCode` still exists in `Settings` and is still persisted, but nothing reads it; do not reintroduce a consumer without restoring the panel too.
- **All tone playback goes on `STREAM_DTMF`, never `STREAM_MUSIC`.** `TonePlayer` used `STREAM_MUSIC`, which is the stream every media app fights over. A `PLAYING` media session from another app captures it and this app goes silent with nothing it can observe — Samsung's SoundPicker did exactly that on a Galaxy S24 Ultra, reporting `PLAYING(3)` with `position=0`. The synthesised `AudioTrack` fallback is on `USAGE_VOICE_COMMUNICATION` for the same reason.
- **Do not read `remote_submix` as a fault.** `STREAM_MUSIC: Devices: remote_submix(8000)` is this device's *normal* default sink on an S24 Ultra, with audio working fine. I misdiagnosed that routing label as a hijack and wrote a fix and a note around it. The real evidence was the parked SoundPicker session. Check for a `PLAYING` session with `position=0`, not for the sink name.
- **The deck is the input surface, so hit targets must be measured, not eyeballed.** Deck hit targets are fractions of the deck box, which on a 1080x2340 screen spans roughly y 1360–2082. A tap 10px outside a target silently does nothing or hits a neighbour, which reads as "the key is broken". Derive coordinates from a full-resolution screenshot before concluding a control is dead.

**Still outstanding:**

- **SIM insertion/removal has never been seen working.** The status strip correctly reports `NO SIM` / `NO SVC` on a genuinely SIM-less device, but a physical swap is the only way to verify the transition. Probed: `cmd phone` exposes no SIM-state manipulation (only ims/uce/cc/gba/src/restart-modem) and there is no emulator transport for `adb emu gsm`, so there is no adb route at all.
- **Play Store signing/track** — not started. The APK is signed directly with the project key. This needs a real developer account and a review process, so it is not repo work.
- **CI is intermittently flaky** in GitHub's OIDC step, before any build. — **Resolved, and it was never intermittent.** That failure was in `testlab.yml`, not `android.yml`, and the cause was four unset repository variables (`GCP_WIF_PROVIDER`, `GCP_TESTLAB_SERVICE_ACCOUNT`, `GCP_PROJECT_ID`, `GCP_RESULTS_BUCKET`) — the one-time GCP setup in `docs/test-lab-setup.md` had never been done. That job now checks the variables first and skips with a notice naming them; verified by dispatching it (green, all gcloud steps skipped). Consequently **Firebase Test Lab has never actually run** — the emulator job in `Android CI` is the real coverage until someone does that setup.

## Conventions

- Kotlin 2.2.10, Compose BOM, Material 3. `compileSdk`/`targetSdk` = 37, `minSdk` = 27, `compileOptions` = Java 11.
- Compose-only UI; no XML layouts. `buildFeatures.compose = true`, no `viewBinding`.
- AGP 9 `optimization { packageScope = ... }` in the release block — shrink `androidx.**` and `kotlinx.**` only. **`kotlin.**` must stay out.** With it in scope, R8 repackaged the stdlib and shipped a release APK that died on launch with `IllegalAccessError: Class kotlin.collections.j extended by class yn is inaccessible`, thrown from `ProcessLifecycleInitializer` before `MainActivity` started. There are no `proguardFiles` at all, so that block is the entire shrink configuration.

### The release crash nothing in CI can see

A release-only R8 failure reached a signed APK and **no job in `android.yml` could have caught it**: `Build & Test APK` and `Signed release APK` both assemble without launching the app, and `emulator` installs the *debug* variant, which is never shrunk.

Worse, it was **latent rather than caused by any one change**. The build immediately before it produced a working APK from an identical configuration, and that exact older APK still ran clean when reinstalled — so "the new code broke it" was the wrong conclusion, and worth ruling out before touching anything. A trivial edit was enough to tip R8 into the broken outcome.

Consequence: a green run does not mean the release artifact *starts*. **Install the release APK and launch it before trusting any shrink-related change.** Removing `kotlin.**` was the fix; the stdlib ships minified anyway, so the size saving was negligible.

### Secret codes cannot be sent, only handed off

`*#06#` and friends are delivered by broadcasting `android.provider.Telephony.SECRET_CODE`. A launcher **cannot** do that: the action is protected by `MODIFY_PHONE_STATE`, a signature permission no ordinary app can ever hold, and `ROLE_DIALER` is an unrelated grant that does not help — confirmed on Android 17 by watching it fail. `com.android.phone` also registers no `SECRET_CODE` receiver on API 37, so the old dialer-side path is gone too.

The working approach is to hand the code to a **foreign** dialer via `ACTION_DIAL` and let that dialer run it. The trap: Parallax registers for `ACTION_DIAL` with a `tel:` scheme, so a plain `startActivity` resolves straight back to Parallax and the code goes nowhere. `SecretCodes.send` filters candidates to foreign packages, and Samsung's `com.samsung.android.dialer` then receives the code intact — verified on device, `*#06#` arrives unmangled.

Two shapes exist and **both must match**: `*#06#` and `*#*#6485#*#*`. Matching only the first silently breaks everything else — those codes fail `detect()`, fall through to `placeCall`, and the app *tries to dial them*. Check the pattern against the whole table plus the false-positive cases (`*`, `#` and ordinary numbers must not match).

Samsung's dialer nags "Set Phone as default phone app?" and closes itself if you decline, so the code is only usable if you act before dismissing. Do not grant that role to test Parallax's handoff — the handoff works without it.

### Media transport needs Media3, not androidx.media

`androidx.media` **no longer ships `MediaControllerCompat` or `MediaSessionManagerCompat`.** Depending on it compiles to nothing usable and fails as three "Unresolved reference" errors on the imports. Verified rather than assumed: the `androidx/media/media` AARs for 1.6.0, 1.7.0 and 1.8.0 were pulled and listed, and none contains `androidx/media/session/MediaControllerCompat`. All that survives in that package is `MediaButtonReceiver`.

The dependency is `androidx.media3:media3-session`. Three facts that are easy to get wrong:

- `MediaController.Builder` has **`setApplicationLooper`**, not `setLooper`. Without an explicit looper the bind throws, because the poll runs on a dispatcher with no thread-local `Looper`.
- Media3's `MediaMetadata` has **no `artwork` Bitmap**. Artwork arrives as `artworkData: ByteArray` plus a MIME type and has to be decoded. The platform `MediaMetadata` used to hand over a Bitmap directly.
- `weight` is a `RowScope`/`ColumnScope` **member**, not a top-level import, and it is available only lexically inside that scope's content lambda. A separate composable called from a `Column` does not inherit it — pass a modifier in instead.

Session discovery is `MediaSessionManager.getActiveSessions(context, packageName = null, listener)` — **static**, and `null` for the package name means all packages. It needs `MEDIA_CONTENT_CONTROL` or an enabled notification listener; this app already registers `ParallaxNotificationListener`, so the grant Mode 7 asks for is the one that matters.

Repeat and shuffle are the entire reason for Media3: `android.media.session.MediaController` has no `setRepeatMode` or `setShuffleMode` at all, because those are set on the *session* side and a launcher that is not the active player has no session.

**The player has never been seen working with real audio.** It has only been exercised in its empty state, and there is no media file on any lab device yet. Treat the transport as unverified until it has been driven against a live session.

### A launcher cannot take a Z Flip's cover display

Removed entirely. Do not attempt this again without new evidence. It was tried,
it did not work, and the code was deleted rather than left half-finished.

On **both** a Galaxy Z Flip8 (SM-F776B, API 37) and a Z Flip5 (SM-F731B, API 34) folding shut gives:

```
Parallax windows: mDisplayId=0   (inner display, switched OFF while folded)
cover display:    mDisplayId=1   com.android.systemui.subscreen.SubHomeActivity, uid 10060
```

The activity is **not** relocated to the cover display. The inner display is off, so anything drawn there is invisible, while Samsung's Subscreen home holds the screen that is actually showing.

Three routes tried, all dead ends:

- **`android:resizeableActivity`** — the wrong lever entirely. It governs resizing *within* a display and recreation on configuration change, not migration *across* `Display` instances. Adding it changed nothing observable.
- **A setting** — none exists. `show_navigation_for_subscreen` and `cover_screen_show_notification_tip` are display preferences, not ownership.
- **A window on the cover display** — `TYPE_APPLICATION_OVERLAY` via `SYSTEM_ALERT_WINDOW`, bound to the cover `Display`. The platform APIs for this are largely gone or hidden on compileSdk 37: `Context.createWindowContext` has no usable public 2-arg overload, `Display.TYPE_INTERNAL` / `FLAG_OWN_DISPLAY_ONLY` / `Display.type` do not resolve, and `DisplayManager.createDisplayContext` does not resolve either — even though `DisplayManager` itself resolves fine.

`rememberFoldState()` and the closed-flip takeover were **removed** rather than left in a half-working state. The feature was deleted, `androidx.window` dropped, and the handset's closed flip went back to showing the cover panel it always had. The record above is kept because the finding is expensive and not visible from the source.

The 2005-iTunes player is unrelated to any of this and survives: it lives under `Multimedia` in Mode 7 and is driven from the handset's own keypad.
- `local.properties`, `.idea/caches`, `/build` are gitignored. `.idea/misc.xml` has been committed before and shows up as unrelated noise in diffs — ignore it.
- CI treats `main` and `master` as the integration branches; the repo is on `main`.

