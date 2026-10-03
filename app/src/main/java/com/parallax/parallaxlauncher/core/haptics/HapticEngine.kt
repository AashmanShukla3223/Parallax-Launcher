package com.parallax.parallaxlauncher.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(context: Context, private val enabled: () -> Boolean = { true }) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private fun play(predefined: Int, fallbackMs: Long, amplitude: Int) {
        if (!enabled()) return
        runCatching {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createPredefined(predefined)
            } else {
                VibrationEffect.createOneShot(fallbackMs, amplitude)
            }
            v.vibrate(effect)
        }
    }

    /** Dial detent. */
    fun tick() = play(VibrationEffect.EFFECT_TICK, 8, 60)

    /** Light key press. */
    fun click() = play(VibrationEffect.EFFECT_CLICK, 15, 120)

    /** Heavy mechanical thud (toggle, launch). */
    fun thud() = play(VibrationEffect.EFFECT_HEAVY_CLICK, 35, 255)
}
