package com.parallax.parallaxlauncher.core.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
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
    /** Signal bars 0..4 as the handset would draw them. 0 means no service. */
    val signalBars: Int = 0,
    /**
     * Whether the handset has usable service.
     *
     * Distinct from "has a SIM": a handset can hold a perfectly good SIM while
     * being out of coverage, in which case it names the operator but cannot
     * complete a call. The status strip has to tell those apart, because showing
     * full bars while calls cannot connect is simply wrong.
     */
    val hasService: Boolean = false,
    /** True when the radio is registered but only for emergency traffic. */
    val emergencyOnly: Boolean = false,
    /**
     * Whether a usable SIM is present, re-read every second.
     *
     * Sampled rather than cached because the profile can change underneath the
     * app: a SIM can be inserted or removed, and the operator can change, with
     * no process restart. A one-shot read leaves the UI describing a radio that
     * is no longer there.
     */
    val simPresent: Boolean = true,
    /** Raw operator string as the SIM reports it, or null. */
    val simOperatorName: String? = null,
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
                val signal = readSignal()
                val sim = readSim()
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
                    signalBars = signal.first,
                    hasService = signal.second,
                    emergencyOnly = signal.third,
                    simPresent = sim.first,
                    simOperatorName = sim.second,
                )
                delay(1000)
            }
        }
    }

    fun stop() = scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()

    private companion object {
        const val SERVICE_STATE_IN_SERVICE = 0
        const val SERVICE_STATE_EMERGENCY_ONLY = 2
    }

    /**
     * The SIM as (present, operatorName).
     *
     * `simState` needs no permission. The operator name can be withheld from
     * API 29, so that read is guarded separately and a null is reported rather
     * than guessed. A vendor implementation that throws is treated as "SIM
     * present", because refusing calls on a radio we merely failed to
     * interrogate would be the worse failure.
     */
    private fun readSim(): Pair<Boolean, String?> = runCatching {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val present = when (tm.simState) {
            TelephonyManager.SIM_STATE_READY,
            TelephonyManager.SIM_STATE_NETWORK_LOCKED,
            TelephonyManager.SIM_STATE_PIN_REQUIRED,
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> true

            else -> false
        }
        val name = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tm.createForSubscriptionId(SubscriptionManager.getDefaultSubscriptionId())
                    .simOperatorName
            } else {
                tm.simOperatorName
            }
        }.getOrNull()
        present to name
    }.getOrDefault(true to null)

    /**
     * Signal as (bars, hasService, emergencyOnly).
     *
     * Bars follow the same five-step ladder the platform reports through
     * `SignalStrength.getLevel`, and are forced to zero whenever there is no
     * service: an out-of-service registration means the bars are meaningless,
     * and drawing them filled is the part users notice first.
     */
    private fun readSignal(): Triple<Int, Boolean, Boolean> = runCatching {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Order matters. signalStrength needs no permission; getServiceState is
        // not reliably readable from API 29 and throws. Reading it first meant
        // a SecurityException was swallowed and reported as "no service", so a
        // perfectly healthy 5G handset sat there claiming NO SVC. Being wrong in
        // that direction is the dangerous one, because it tells the user their
        // calls cannot connect when they can.
        // Bars are the signal strength. Note this is deliberately independent
        // of service: a handset can sit on full bars with no service, which is
        // exactly the state the NO SVC caption exists to explain.
        val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tm.signalStrength?.level ?: 0
        } else {
            0
        }
        val bars = level.coerceIn(0, 4)

        // Service is a separate question and neither of the obvious sources
        // answers it without a permission we do not hold:
        //   getServiceState() throws SecurityException from API 29, and
        //   signalStrength keeps reporting full bars with the radio off.
        // So prefer the network's own view of whether a cellular transport is
        // up, which needs only ACCESS_NETWORK_STATE (a normal permission,
        // granted at install with no runtime prompt).
        val cellular = cellularTransportUp()
        if (cellular != null) {
            return@runCatching Triple(bars, cellular, false)
        }

        // No network view available. Fall back to service state, and treat an
        // unreadable one as unknown rather than as proof of no service.
        val ss = runCatching { tm.serviceState }.getOrNull()
            ?: return@runCatching Triple(bars, false, false)

        // ServiceState's STATE_* constants are not visible against this compile
        // SDK, but their values are fixed by the AOSP contract:
        // 0 IN_SERVICE, 1 OUT_OF_SERVICE, 2 EMERGENCY_ONLY, 3 POWER_OFF.
        val registered = ss.state == SERVICE_STATE_IN_SERVICE
        val emergencyOnly = ss.state == SERVICE_STATE_EMERGENCY_ONLY
        // Emergency-only registration still lets emergency calls through, so it
        // counts as usable service for the purposes of the strip.
        val hasService = registered || emergencyOnly
        if (!hasService) Triple(0, false, emergencyOnly) else Triple(0, true, emergencyOnly)
    }.getOrDefault(Triple(0, false, false))

    /**
     * Whether a cellular transport is currently up, or null when unknown.
     *
     * This is the only service signal available without READ_PHONE_STATE. A
     * handset that has lost its cellular network stops presenting a CELLULAR
     * network entirely, so the active network being Wi-Fi or Ethernet means no
     * mobile service, which is what the NO SVC caption reports.
     */
    private fun cellularTransportUp(): Boolean? = runCatching {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Every network, not just the active one. The active network is
        // normally Wi-Fi even while cellular is registered, so looking only
        // there reports no mobile service on a perfectly healthy handset.
        cm.allNetworks.mapNotNull { cm.getNetworkCapabilities(it) }
            .filter { it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) }
            .any { caps ->
                // NOT_SUSPENDED is the flag that actually moves when the radio
                // drops: the cellular network stays listed and stays
                // VALIDATED, and is suspended instead. Verified by diffing the
                // NetworkCapabilities with the radio on and off, where this is
                // the only difference in the whole capability set.
                //
                // It arrived in API 28, so below that we accept any cellular
                // network rather than declaring no service we cannot confirm.
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
            }
    }.getOrNull()

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
