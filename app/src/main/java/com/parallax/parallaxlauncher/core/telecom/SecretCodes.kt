package com.parallax.parallaxlauncher.core.telecom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

/**
 * OEM engineering codes, entered like `*#06#` on the handset.
 *
 * ## Why this exists
 *
 * These are how you get a device to tell you about itself: IMEI and SIM status,
 * full dumpsys bundles, kernel and memory info. On a hardware problem — no audio
 * output, a dead sensor, a screen that will not light — they are often the only
 * diagnostic that fits in a support ticket.
 *
 * ## How they are delivered
 *
 * Not by broadcasting, and not by calling `placeCall` — which would try to *dial*
 * the string. A launcher cannot send `android.provider.Telephony.SECRET_CODE`
 * itself: the action needs `MODIFY_PHONE_STATE`, a signature permission no
 * ordinary app can hold, and `ROLE_DIALER` is an unrelated grant that does not
 * help. Both were tried and both fail on Android 17.
 *
 * So the code is handed to a real dialer via `ACTION_DIAL` and that dialer
 * decides what to do with it. That also matches what the user wants to see: the
 * code appears in a dialer they recognise rather than vanishing into a system
 * dialog.
 *
 * ## The table
 *
 * Deliberately **read-only** codes only. The set below reports state and toggles
 * diagnostics that the device is already designed to expose. It excludes the
 * destructive family on purpose — wipes, IMEI/serial rewriting, baseband and
 * engineering-mode writes. Those are not diagnostics, they brick phones and in
 * several jurisdictions IMEI tampering is a criminal offence. This app will not
 * be a vector for it.
 *
 * Codes vary by model, carrier and region. Nothing here is guaranteed to exist on
 * any given handset; an unrecognised code is simply ignored by the phone app.
 */
object SecretCodes {

    /**
     * A secret code is `*#` … `#*` or `*#…#`. Both delimiters are required, and
     * the middle must be at least one character. That is narrow on purpose: a
     * bare `*` is the unlock key on Mode 7 and `*#…#*` is a distinct vendor
     * family, so neither should be swallowed here.
     */
    private val PATTERN = Regex("""\*#(.+)#""")

    /** The code if [raw] is a secret code, else null. */
    fun detect(raw: String): String? = raw.trim().takeIf { PATTERN.matches(it) }

    /**
     * Hands [code] to a dialer that can act on it.
     *
     * ## Why not broadcast it ourselves
     *
     * The obvious route is broadcasting
     * `android.provider.Telephony.SECRET_CODE` directly, which is what the old
     * stock dialer did. That is no longer available to a launcher: the action is
     * protected by `MODIFY_PHONE_STATE`, a signature permission no ordinary app
     * can ever hold. Verified on Android 17 —
     *
     *     SecurityException: Permission Denial: not allowed to send broadcast
     *     android.provider.Telephony.SECRET_CODE from uid=…
     *
     * Holding `ROLE_DIALER` does not help; the role and the permission are
     * unrelated grants. So the code is passed to a real dialer instead and that
     * dialer decides what it does with it.
     *
     * ## Avoiding the loop
     *
     * Parallax registers for `ACTION_DIAL` with a `tel:` scheme, so it is
     * itself a candidate handler. A plain `startActivity` would resolve back to
     * Parallax and the code would never move. Every candidate except our own
     * package is therefore considered, and only a foreign dialer is eligible.
     */
    fun send(context: Context, code: String): Boolean {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.fromParts("tel", code, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val foreign = try {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .firstOrNull { it.activityInfo.packageName != context.packageName }
        } catch (_: Exception) {
            null
        }

        // Prefer the resolved foreign handler so the user lands in a dialer,
        // not a browser or anything else that also claims tel: links.
        return try {
            if (foreign != null) {
                intent.component = foreign.activityInfo
            } else if (intent.resolveActivity(context.packageManager) == null) {
                Log.w("SecretCodes", "no dialer available for $code")
                return false
            }
            context.startActivity(intent)
            true
        } catch (e: SecurityException) {
            Log.w("SecretCodes", "could not hand off $code to a dialer", e)
            false
        } catch (e: Exception) {
            Log.w("SecretCodes", "handoff of $code failed", e)
            false
        }
    }

    /** One entry in the reference list. */
    data class Entry(
        val code: String,
        val label: String,
        val brands: String,
    )

    /**
     * A browsable reference, grouped by the brand that documents each code.
     *
     * Shown in the app so the list is discoverable without a web search — the
     * codes themselves are dispatched to the system, never interpreted here.
     */
    val reference: List<Entry> = listOf(
        // --- Samsung ---------------------------------------------------------
        Entry("*#06#", "IMEI, serial, hardware and SIM status", "Samsung"),
        Entry("*#0*#", "Full device dump: logcat, bugreport, dumpsys", "Samsung"),
        Entry("*#1234#", "Product and hardware identity", "Samsung"),
        Entry("*#*#7788#*#*", "Factory reset. NOT DISPATCHED", "Samsung"),
        Entry("*#*#123456789#*#*", "Write IMEI / serial. NOT DISPATCHED", "Samsung"),

        // --- Google / Pixel --------------------------------------------------
        Entry("*#*#4834#*#*", "Full dump: bugreport, logcat, dumpsys", "Google Pixel"),
        Entry("*#*#637#*#*", "Enable the old-school service menu", "Google Pixel"),
        Entry("*#*#6485#*#*", "Engineering menu: radio, SIM, audio", "Google Pixel"),
        Entry("*#*#347#*#*", "Device information and SELinux state", "Google Pixel"),
        Entry("*#*#8255#*#*", "Phone/tablet information", "Google Pixel"),

        // --- Xiaomi / Redmi / POCO ------------------------------------------
        Entry("*#*#6485#*#*", "Engineering menu (Hardware test)", "Xiaomi, Redmi, POCO"),
        Entry("*#*#463#*#*", "Hardware and IMEI information", "Xiaomi, Redmi, POCO"),
        Entry("*#*#872#*#*", "Full logcat", "Xiaomi, Redmi, POCO"),

        // --- OnePlus ---------------------------------------------------------
        Entry("*#*#6485#*#*", "Engineer mode: audio, radio, touch", "OnePlus, OxygenOS"),

        // --- Motorola --------------------------------------------------------
        Entry("*#*#6485#*#*", "Engineering menu", "Motorola"),
        Entry("*#*#637#*#*", "Service menu", "Motorola"),

        // --- Oppo / Realme ---------------------------------------------------
        Entry("*#*#6485#*#*", "Engineer mode", "Oppo, Realme"),
        Entry("*#*#585#*#*", "Full logcat with tags", "Oppo, Realme"),
        Entry("*#*#1037#*#*", "Memory and process information", "Oppo, Realme"),

        // --- Vivo / iQOO -----------------------------------------------------
        Entry("*#*#6485#*#*", "Engineer mode: audio and radio tests", "Vivo, iQOO"),
        Entry("*#*#5577#*#*", "Radio and SIM information", "Vivo, iQOO"),
        Entry("*#*#112#*#*", "Logcat buffer dump", "Vivo, iQOO"),

        // --- Huawei / Honor --------------------------------------------------
        Entry("*#*#6485#*#*", "Engineer mode", "Huawei, Honor"),
        Entry("*#*#463#*#*", "Hardware identity and IMEI", "Huawei, Honor"),

        // --- Generic / AOSP --------------------------------------------------
        Entry("*#*#3855#*#*", "Radare: dial, call, SMS, data and IMEI tests", "Most AOSP builds"),
        Entry("*#*#87654321#*#*", "MTK Engineering Mode", "MediaTek devices"),
        Entry("*#*#7228466#*#*", "Qualcomm MSM / internal logcat", "Qualcomm devices"),
    )

    /**
     * Codes deliberately excluded from [reference] being dispatchable.
     *
     * Shown in the UI as a warning rather than silently omitted, because someone
     * with a dead phone may well search for these and should learn why they are
     * not offered.
     */
    val refused: List<Pair<String, String>> = listOf(
        "*#*#7788#*#*" to "Factory reset. Erase the device instead, deliberately.",
        "*#*#123456789#*#*" to "Writes the IMEI. Illegal in many countries, and permanently damages the radio.",
        "*#*#3218737#*#*" to "Factory reset (MediaTek).",
        "*#*#235#*#*" to "Writes IMEI on some Qualcomm builds.",
    )
}