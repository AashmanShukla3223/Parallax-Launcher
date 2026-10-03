package com.parallax.parallaxlauncher.core.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class Telemetry(
    val batteryMv: Int = -1,
    val batteryPct: Int = -1,
    val tempC: Float = Float.NaN,
    val charging: Boolean = false,
    val cpuMhz: Int = -1, // -1 = unavailable
    val cpuLoadPct: Int = -1, // from /proc/stat; -1 when blocked by the OS
    val ramUsedMb: Int = -1,
    val ramTotalMb: Int = -1,
    val rxKbps: Float = 0f,
    val txKbps: Float = 0f,
)

class TelemetryService(context: Context) {
    private val ctx = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(Telemetry())
    val state: StateFlow<Telemetry> = _state.asStateFlow()

    private var lastRx = TrafficStats.getTotalRxBytes()
    private var lastTx = TrafficStats.getTotalTxBytes()
    private var lastT = SystemClock.elapsedRealtime()
    private var lastBusy = -1L
    private var lastTotal = -1L

    fun start() {
        scope.launch {
            while (true) {
                // Sticky broadcast: pass null receiver, read current value.
                val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
                val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

                val now = SystemClock.elapsedRealtime()
                val rx = TrafficStats.getTotalRxBytes()
                val tx = TrafficStats.getTotalTxBytes()
                val dt = (now - lastT).coerceAtLeast(1L)
                val unsupported = TrafficStats.UNSUPPORTED.toLong()
                val rxK = if (rx == unsupported || lastRx == unsupported) 0f else (rx - lastRx) * 1000f / dt / 1024f
                val txK = if (tx == unsupported || lastTx == unsupported) 0f else (tx - lastTx) * 1000f / dt / 1024f
                lastRx = rx; lastTx = tx; lastT = now

                _state.value = Telemetry(
                    batteryMv = i?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1,
                    batteryPct = if (level >= 0 && scale > 0) level * 100 / scale else -1,
                    tempC = (i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                        ?.takeIf { it != Int.MIN_VALUE }?.div(10f)) ?: Float.NaN,
                    charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL,
                    cpuMhz = readCpuMhz(),
                    cpuLoadPct = readCpuLoad(),
                    ramUsedMb = ((mi.totalMem - mi.availMem) / 1_048_576L).toInt(),
                    ramTotalMb = (mi.totalMem / 1_048_576L).toInt(),
                    rxKbps = rxK.coerceAtLeast(0f),
                    txKbps = txK.coerceAtLeast(0f),
                )
                delay(1000)
            }
        }
    }

    fun stop() = scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()

    /** Max current core freq from sysfs; -1 if blocked. */
    private fun readCpuMhz(): Int = runCatching {
        var max = -1
        for (n in 0 until Runtime.getRuntime().availableProcessors()) {
            val khz = File("/sys/devices/system/cpu/cpu$n/cpufreq/scaling_cur_freq")
                .readText().trim().toInt()
            if (khz / 1000 > max) max = khz / 1000
        }
        max
    }.getOrDefault(-1)

    /** Aggregate CPU load from the first line of /proc/stat (denied on Android 8+ for apps; -1 then). */
    private fun readCpuLoad(): Int = runCatching {
        val p = File("/proc/stat").bufferedReader().use { it.readLine() }
            .trim().split(Regex("\\s+")).drop(1).map { it.toLong() }
        val idle = p[3] + p.getOrElse(4) { 0L }
        val total = p.sum()
        val busy = total - idle
        val pct = if (lastTotal > 0 && total > lastTotal) ((busy - lastBusy) * 100 / (total - lastTotal)).toInt() else -1
        lastBusy = busy; lastTotal = total
        pct
    }.getOrDefault(-1)
}
