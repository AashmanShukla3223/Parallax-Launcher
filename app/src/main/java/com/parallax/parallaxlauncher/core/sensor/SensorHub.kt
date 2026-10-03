package com.parallax.parallaxlauncher.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Normalized gravity in screen space: x right, y down, each roughly in [-1, 1]. */
data class Tilt(val gx: Float = 0f, val gy: Float = 0.2f)

class SensorHub(context: Context) : SensorEventListener {
    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val _tilt = MutableStateFlow(Tilt())
    val tilt: StateFlow<Tilt> = _tilt.asStateFlow()
    private var sx = 0f
    private var sy = 0f

    fun start() {
        accel?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() = sm.unregisterListener(this)

    override fun onSensorChanged(e: SensorEvent) {
        // Accelerometer reads the reaction to gravity: screen gravity = (-x, +y). Low-pass rejects tremor.
        val a = 0.15f
        sx += a * (-e.values[0] / SensorManager.GRAVITY_EARTH - sx)
        sy += a * (e.values[1] / SensorManager.GRAVITY_EARTH - sy)
        _tilt.value = Tilt(sx, sy)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
