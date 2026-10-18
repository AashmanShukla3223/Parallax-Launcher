# Testing Parallax Launcher

You have a release APK and no documentation. This is that documentation.

**Report bugs here:** https://github.com/AashmanShukla3223/Parallax-Launcher/issues
The bug template asks for a screenshot. Please include one — every visual
claim in this project gets verified by looking at a picture of it, so a
screenshot turns "Mode 7 looks wrong" into something I can actually fix.

---

## Before you install

It is a self-signed APK, not a Play Store app, so Android will warn you.
That warning is expected. It is not signed by Google and never will be yet.

**Requires Android 8.0 (API 27) or newer.**

## Installing

1. Download `app-release.apk` from the releases page.
2. Open it. Android will ask you to allow installs from that source. Allow it.
3. Install.

If it fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, you already have a
build signed with a *different* key — most likely a debug build. Uninstall
that one first. Note this **deletes the app's data**, including your phonebook
imports and any notifications it has captured.

### If it says "App not installed"

Known, and not a broken download. Samsung shows that one generic message for
several different failures. In order of likelihood:

1. **Play Protect blocked it.** This is the common one. The APK is self-signed
   *and* asks for phone, contacts and camera permissions, and registers as a
   dialer and a notification listener — that combination is what a
   caller-spoofing app looks like, whatever the code actually does. Look for a
   "Blocked by Play Protect" or "This app may be harmful" dialog, and use
   **Details → More info → Install anyway**. It is a false positive.

2. **Unknown sources not enabled** for the app you opened the file from —
   your browser or file manager. Settings → Apps → Special app access →
   Install unknown apps → pick that app → Allow.

3. **Truncated download.** Check the file is **2,993,721 bytes**. If it is
   smaller, re-download from the release page.

**The reliable route, if the tap-to-install path fights you:**

```
adb install app-release.apk
```

That bypasses Play Protect's prompt entirely and prints the real reason if it
genuinely fails. Requires USB debugging on, which is a one-time toggle in
Settings → Developer options.

## Optional: making it your launcher

The app registers as a home app and as a dialer, but **it works fine without
either**. Only set them if you want to.

- **Set as default launcher** — Settings → Apps → Default apps → Home app.
- **Set as default phone app** — Settings → Apps → Default apps → Phone app.
  Only needed for Mode 7's dialling. Granting it also grants contacts, SMS
  and microphone access, so it is a bigger step than it looks.

### How to undo it

This matters, because I have left a test device with the dialer role flipped
and had to fix it by hand.

- Launcher: Settings → Apps → Default apps → Home app → your original.
- Dialer: Settings → Apps → Default apps → Phone app → your original.
- Permissions: long-press the app → Permissions.

## The permissions, and why each one is there

Nothing is granted at install time. Every feature below silently does nothing
until you turn it on, which is the most common reason for "the phone doesn't
ring".

| Permission | Needed for |
|---|---|
| Phone | Placing and answering calls |
| Contacts | Mode 7's phonebook, on the DOWN key |
| Notifications | Mode 7's in-app message banners |
| Camera | Mode 5 only |

The notification listener only intercepts while the launcher is in the
foreground **and** Mode 7 is the active mode. Every other app's notifications
are left alone — this app posts no notifications of its own.

---

# What to test

## The highest-value thing

**Mode 7's soft keys do not respond when you tap the words.**

The bar under the internal display draws `CLEAR / SEND / CANCEL`, but nothing
inside the upper half of the screen accepts touches. The deck below is the only
working input surface:

- The **rocker's centre button** is what places a call.
- The **two bracket keys** flanking the rocker are the left and right soft keys.
- The digits, the rocker arrows, and the hinge all work.

This is a real bug, not a misunderstanding, and it is the thing I would most
like eyes on. If the words on the display are *supposed* to be tappable on the
real handset, tell me — that changes what the fix should be.

## Mode 7 with a SIM

I have only ever tested this on a phone with **no SIM inserted**, so everything
below is unverified. If you have a SIM, this is where you are most useful:

- Does the status strip show your carrier? It shows a **2G-era brand name**
  rather than the modern one — on Indian networks, `Reliance Communications`
  for Jio, `AirTel` for Airtel, `Hutch` for Vi, `BSNL` for BSNL. Tell me if
  yours shows something wrong or blank.
- Insert and remove the SIM. The strip re-reads it every second and should
  follow. **This is the one thing I have never been able to test.**
- Does it show service when you are in coverage but the radio is off, and
  correctly show no service when you are genuinely out of coverage?
- Dialling an ordinary number with no SIM should be **refused**. An emergency
  number is the only exception. Both paths go through the same gate, so the
  green key cannot get around it.
- Incoming calls, the phonebook on the DOWN key, and call screening.

## Modes 1-6

These are the launcher modes, and they are the ones you will live in if you
set it as your home app.

- Every mode renders? Themes in CFG?
- Modes 4 and 5 use shaders that need **API 33**. Below that they degrade to
  plain drawing rather than crashing — check that it degrades cleanly on your
  device if it is older.
- The `MODE n` and `CFG` chips should switch between all seven.
- Does it survive being your actual home app — swiping between home screens,
  opening the app drawer, coming back?

## Battery and honesty

Please tell me about anything that looks wrong rather than polished. I would
much rather know.

---

# Known issues

So you do not spend time rediscovering them:

- **The soft-key captions under the display are not tappable** (above).
- **SIM insertion and removal is unverified** (above).
- The emergency number table (~160 countries) and the 2G operator names
  (~110 rows) were derived from Wikipedia and only partly cross-checked
  against national sources. **If a number looks wrong for your country, say
  so** — do not trust it in an emergency.
- No Play Store release, no Play App Signing.

## A note on the keypad

Mode 7's keypad has been rebuilt to match the real handset: one continuous
etched sheet with a groove that steps down into a narrow centre channel, rather
than separate key boxes. If it still reads as "a grid of buttons" to you rather
than "a 2004 phone", that is worth telling me — I have been going back and
forth on it against reference photos, and a fresh pair of eyes is worth more
than another round of me guessing.