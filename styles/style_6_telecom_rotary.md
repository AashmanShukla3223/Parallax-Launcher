# Parallax Launcher - Style 6: Telecom Rotary & Hotlines

> **Design Persona:** Western Electric 500 / Bell System Payphone / Analog Switchboard.
> **Vibe:** Bakelite black / rotary cream, brass finger stop, warm glowing Nixie tubes, mechanical pulse-dial return spring, authentic DTMF audio tones, and a vintage Yellow Pages rolodex directory.

---

## 1. Core Mechanics
1. **Persistent Localized Phone Numbers:**
   - Detects user country via `TelephonyManager` or `Locale.getDefault().country`:
     - India (`IN`) -> `+91 9XXXX XXXXX`
     - US / Canada (`US`, `CA`) -> `+1 (555) XXX-XXXX`
     - UK (`GB`) -> `+44 7XXX XXXXXX`
     - Fallback -> `+1` or `+91`.
   - Each app is deterministically hashed or randomized once, stored permanently in `SharedPreferences` (`telecom_phone_numbers`).
   - Apps also get a quick 3-digit extension (e.g. `ext: 104` or `#04`) for fast speed-dialing.

2. **Authentic Rotary Dial:**
   - 10 circular finger holes: `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `0`.
   - Fixed metal finger stop at bottom-right (~140° angle).
   - Drag interaction: Dragging a finger clockwise moves the rotary wheel until hitting the stop.
   - Spring-back return: On release, an animated spring smoothly spins the wheel counter-clockwise back to the resting position while pulsing mechanical click haptics and DTMF audio tones!

3. **Audio & Haptic Feedback:**
   - Hardware `ToneGenerator` playing genuine DTMF tones for each dialed digit (`TONE_DTMF_0`..`9`).
   - Heavy relay mechanical clicks upon digit registration.
   - Ringback tone (`TONE_SUP_RINGTONE` or call audio) when an app number is dialed successfully, leading into the app launch!

4. **Nixie / VFD Call Readout:**
   - Shows currently dialed digits: `DIALED: +91 98401...`
   - Real-time caller-ID matching: As you dial, matches existing apps in your Yellow Pages directory.
   - Call & Clear buttons: Green vintage Bakelite handset button to trigger immediate launch once matched, red button to hang up / clear buffer.

5. **Vintage Yellow Pages / Rolodex:**
   - Expandable bottom drawer listing all apps, their icons, labels, and assigned phone numbers / extensions.
   - One-tap "SPEED DIAL": Auto-rotates or dials the digits into the phone!
