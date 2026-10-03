package com.parallax.parallaxlauncher.core.telecom

import android.content.Context
import android.telephony.TelephonyManager
import com.parallax.parallaxlauncher.core.model.AppInfo
import java.util.Locale
import kotlin.random.Random

data class AppContact(
    val app: AppInfo,
    val formattedNumber: String,
    val rawDigits: String,
    val extension: String,
)

class TelecomRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("telecom_numbers", Context.MODE_PRIVATE)
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    val countryCode: String = detectCountryCode()
    val countryPrefix: String = detectPrefix(countryCode)

    private fun detectCountryCode(): String {
        val simCountry = telephonyManager?.simCountryIso?.uppercase(Locale.ROOT)
        if (!simCountry.isNullOrBlank()) return simCountry
        val netCountry = telephonyManager?.networkCountryIso?.uppercase(Locale.ROOT)
        if (!netCountry.isNullOrBlank()) return netCountry
        return Locale.getDefault().country.uppercase(Locale.ROOT)
    }

    private fun detectPrefix(country: String): String = when (country) {
        "IN" -> "+91"
        "US", "CA" -> "+1"
        "GB" -> "+44"
        "AU" -> "+61"
        "DE" -> "+49"
        "FR" -> "+33"
        "JP" -> "+81"
        else -> "+91" // Default to India as requested or fallback
    }

    fun getOrAssignContact(app: AppInfo, index: Int = 0): AppContact {
        val key = "app_${app.packageName}"
        val existing = prefs.getString(key, null)
        val rawDigits: String
        val extension: String

        if (existing != null) {
            val parts = existing.split(":")
            rawDigits = parts.getOrNull(0) ?: generateRawDigits(app.packageName)
            extension = parts.getOrNull(1) ?: String.format(Locale.ROOT, "%03d", (index + 1) % 1000)
        } else {
            rawDigits = generateRawDigits(app.packageName)
            extension = String.format(Locale.ROOT, "%03d", (index + 1) % 1000)
            prefs.edit().putString(key, "$rawDigits:$extension").apply()
        }

        val formatted = formatNumber(rawDigits, countryPrefix)
        return AppContact(
            app = app,
            formattedNumber = formatted,
            rawDigits = rawDigits,
            extension = extension,
        )
    }

    private fun generateRawDigits(seed: String): String {
        // Deterministic pseudo-random based on package hash + random salt if first generation
        val rng = Random(seed.hashCode().toLong() xor System.currentTimeMillis())
        val sb = StringBuilder()
        when (countryPrefix) {
            "+91" -> {
                // Indian mobile numbers typically start with 6, 7, 8, 9 followed by 9 digits
                val first = (6..9).random(rng)
                sb.append(first)
                repeat(9) { sb.append((0..9).random(rng)) }
            }
            "+1" -> {
                // North American 10 digits
                val area = (200..999).random(rng)
                val exch = (200..999).random(rng)
                val line = (1000..9999).random(rng)
                sb.append(area).append(exch).append(line)
            }
            else -> {
                repeat(10) { sb.append((0..9).random(rng)) }
            }
        }
        return sb.toString()
    }

    private fun formatNumber(raw: String, prefix: String): String {
        return when (prefix) {
            "+91" -> {
                if (raw.length >= 10) {
                    "$prefix ${raw.substring(0, 5)} ${raw.substring(5, 10)}"
                } else "$prefix $raw"
            }
            "+1" -> {
                if (raw.length >= 10) {
                    "$prefix (${raw.substring(0, 3)}) ${raw.substring(3, 6)}-${raw.substring(6, 10)}"
                } else "$prefix $raw"
            }
            else -> "$prefix $raw"
        }
    }
}
