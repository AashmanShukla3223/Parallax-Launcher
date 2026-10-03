package com.parallax.parallaxlauncher.core.settings

import android.content.Context
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
            mode = prefs.getInt("mode", d.mode).coerceIn(1, 5),
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
            .putBoolean("showModeChip", s.showModeChip)
            .apply()
    }

    fun reset() = update { Settings(mode = it.mode) }
}
