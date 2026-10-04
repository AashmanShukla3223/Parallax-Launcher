package com.parallax.parallaxlauncher.core.settings

import android.content.Context
import com.parallax.parallaxlauncher.ui.modes.razr.RazrFinish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Settings(
    val mode: Int = 1,
    val haptics: Boolean = true,
    // Theme & Styling
    val accentColorId: Int = 0,
    val backgroundColorId: Int = 1,
    val fontId: Int = 0,
    // Industrial Rig
    val detentDeg: Float = 15f,
    // Celestial
    val gravity: Float = 0.55f,
    val maxNodes: Int = 36,
    // Cyberdeck
    val crt: Boolean = true,
    // CineCam
    val cineAspect: Int = 0, // 0 = 2.39:1, 1 = 3:2
    val histogram: Boolean = true,
    val grid: Boolean = true,
    // Mode 7 RAZR V3i Call Tariff
    // Two independent rates that BOTH accrue and are summed:
    //  * [callTariffRate]     - per whole completed minute, in MAJOR units (rupee / dollar).
    //  * [callPerSecondRate]  - per elapsed second, in MINOR units (paise / cents).
    //    100 minor units = 1 major unit.
    val callTariffRate: Float = 1.0f,
    val callPerSecondRate: Float = 6f,
    val callCurrencyIndex: Int = 0, // 0 = ₹ (Rupees), 1 = p (Paise), 2 = $ (Dollars), 3 = ¢ (Cents)
    val ringtoneUri: String = "", // "" = system default ringtone, "silent" = none
    // Mode 7 RAZR V3i Hardware / Personalize
    val razrSkin: RazrFinish = RazrFinish.DARK_QUARTZ,
    val razrRingStyleIndex: Int = 0, // 0 = Loud, 1 = Soft, 2 = Vibrate, 3 = Vibe & Ring, 4 = Vibe then Ring, 5 = Silent
    val razrUnlockCode: String = "1234", // 4-digit unlock code, per the V3i manual
    val razrVoicemailNumber: String = "*123",
    val razrWallpaperIndex: Int = 0,
    // Global
    val showModeChip: Boolean = true,
)

class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<Settings> = _state.asStateFlow()

    private fun load(): Settings {
        val d = Settings()
        return Settings(
            mode = prefs.getInt("mode", d.mode).coerceIn(1, 7),
            haptics = prefs.getBoolean("haptics", d.haptics),
            accentColorId = prefs.getInt("accentColorId", d.accentColorId),
            backgroundColorId = prefs.getInt("backgroundColorId", d.backgroundColorId),
            fontId = prefs.getInt("fontId", d.fontId),
            detentDeg = prefs.getFloat("detentDeg", d.detentDeg),
            gravity = prefs.getFloat("gravity", d.gravity),
            maxNodes = prefs.getInt("maxNodes", d.maxNodes),
            crt = prefs.getBoolean("crt", d.crt),
            cineAspect = prefs.getInt("cineAspect", d.cineAspect),
            histogram = prefs.getBoolean("histogram", d.histogram),
            grid = prefs.getBoolean("grid", d.grid),
            callTariffRate = prefs.getFloat("callTariffRate", d.callTariffRate),
            callPerSecondRate = prefs.getFloat("callPerSecondRate", d.callPerSecondRate),
            callCurrencyIndex = prefs.getInt("callCurrencyIndex", d.callCurrencyIndex),
            ringtoneUri = prefs.getString("ringtoneUri", d.ringtoneUri) ?: "",
            razrSkin = RazrFinish.entries.getOrElse(
                prefs.getInt("razrSkin", d.razrSkin.ordinal),
            ) { d.razrSkin },
            razrRingStyleIndex = prefs.getInt("razrRingStyleIndex", d.razrRingStyleIndex)
                .coerceIn(0, 5),
            razrUnlockCode = prefs.getString("razrUnlockCode", d.razrUnlockCode) ?: d.razrUnlockCode,
            razrVoicemailNumber = prefs.getString("razrVoicemailNumber", d.razrVoicemailNumber)
                ?: d.razrVoicemailNumber,
            razrWallpaperIndex = prefs.getInt("razrWallpaperIndex", d.razrWallpaperIndex)
                .coerceIn(0, 3),
            showModeChip = prefs.getBoolean("showModeChip", d.showModeChip),
        )
    }

    fun update(block: (Settings) -> Settings) {
        val s = block(_state.value)
        _state.value = s
        prefs.edit()
            .putInt("mode", s.mode)
            .putBoolean("haptics", s.haptics)
            .putInt("accentColorId", s.accentColorId)
            .putInt("backgroundColorId", s.backgroundColorId)
            .putInt("fontId", s.fontId)
            .putFloat("detentDeg", s.detentDeg)
            .putFloat("gravity", s.gravity)
            .putInt("maxNodes", s.maxNodes)
            .putBoolean("crt", s.crt)
            .putInt("cineAspect", s.cineAspect)
            .putBoolean("histogram", s.histogram)
            .putBoolean("grid", s.grid)
            .putFloat("callTariffRate", s.callTariffRate)
            .putFloat("callPerSecondRate", s.callPerSecondRate)
            .putInt("callCurrencyIndex", s.callCurrencyIndex)
            .putString("ringtoneUri", s.ringtoneUri)
            .putInt("razrSkin", s.razrSkin.ordinal)
            .putInt("razrRingStyleIndex", s.razrRingStyleIndex)
            .putString("razrUnlockCode", s.razrUnlockCode)
            .putString("razrVoicemailNumber", s.razrVoicemailNumber)
            .putInt("razrWallpaperIndex", s.razrWallpaperIndex)
            .putBoolean("showModeChip", s.showModeChip)
            .apply()
    }

    fun reset() = update { Settings(mode = it.mode) }
}
