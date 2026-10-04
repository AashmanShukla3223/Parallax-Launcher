package com.parallax.parallaxlauncher.core.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Snapshot of the active system call, as seen by the UI. */
data class LiveCall(
    val number: String,
    val state: Int,            // android.telecom.Call.STATE_*
    val connectTimeMillis: Long, // 0 until the call is ACTIVE
    val isIncoming: Boolean,
    val muted: Boolean,
    val speaker: Boolean,
    val onHold: Boolean,
    val disconnectCause: Int?, // set once disconnected
)

/** Process-wide bridge between the InCallService and Compose UI. */
object CallManager {
    private val _call = MutableStateFlow<LiveCall?>(null)
    val call: StateFlow<LiveCall?> = _call.asStateFlow()

    internal var service: RazrInCallService? = null
    private var current: Call? = null
    private var muted = false
    private var speaker = false

    private var ringtone: android.media.Ringtone? = null
    private var rawPlayer: android.media.MediaPlayer? = null

    internal fun startRinging(ctx: android.content.Context) {
        stopRinging()
        val pref = ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getString("ringtoneUri", "") ?: ""
        if (pref == "silent") return

        if (pref.startsWith("android.resource://") || pref.isEmpty()) {
            val resId = if (pref.contains("razr_v3_original")) com.parallax.parallaxlauncher.R.raw.razr_v3_original
                        else com.parallax.parallaxlauncher.R.raw.hello_moto
            rawPlayer = runCatching {
                android.media.MediaPlayer.create(ctx.applicationContext, resId)?.apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    start()
                }
            }.getOrNull()
            if (rawPlayer != null) return
        }

        val uri = if (pref.isEmpty()) {
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
        } else android.net.Uri.parse(pref)
        ringtone = runCatching {
            android.media.RingtoneManager.getRingtone(ctx.applicationContext, uri)?.also {
                it.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                it.isLooping = true
                it.play()
            }
        }.getOrNull()
    }

    internal fun stopRinging() {
        runCatching { ringtone?.stop() }
        ringtone = null
        runCatching {
            rawPlayer?.stop()
            rawPlayer?.release()
        }
        rawPlayer = null
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (state != Call.STATE_RINGING) stopRinging()
            publish(call)
        }
        override fun onDetailsChanged(call: Call, details: Call.Details) = publish(call)
    }

    internal fun attach(call: Call) {
        current?.unregisterCallback(callback)
        current = call
        call.registerCallback(callback)
        publish(call)
    }

    internal fun detach(call: Call) {
        stopRinging()
        if (current === call) {
            call.unregisterCallback(callback)
            val last = _call.value
            // Keep a final DISCONNECTED snapshot so the UI can show the summary.
            _call.value = last?.copy(state = Call.STATE_DISCONNECTED)
            current = null
        }
    }

    internal fun audioChanged(state: CallAudioState) {
        muted = state.isMuted
        speaker = state.route == CallAudioState.ROUTE_SPEAKER
        current?.let { publish(it) }
    }

    private fun publish(call: Call) {
        val d = call.details
        val number = d?.handle?.schemeSpecificPart.orEmpty()
        val connect = d?.connectTimeMillis ?: 0L
        _call.value = LiveCall(
            number = number.ifBlank { "Unknown" },
            state = call.state,
            connectTimeMillis = if (call.state == Call.STATE_ACTIVE || call.state == Call.STATE_HOLDING) connect else 0L,
            isIncoming = call.state == Call.STATE_RINGING || d?.callDirection == Call.Details.DIRECTION_INCOMING,
            muted = muted,
            speaker = speaker,
            onHold = call.state == Call.STATE_HOLDING,
            disconnectCause = if (call.state == Call.STATE_DISCONNECTED) d?.disconnectCause?.code else null,
        )
    }

    fun answer() { current?.answer(VideoProfile.STATE_AUDIO_ONLY) }
    fun reject() { current?.reject(false, null) }
    fun hangUp() { current?.disconnect(); service?.endCurrentCall() }
    fun hold(on: Boolean) { current?.let { if (on) it.hold() else it.unhold() } }
    fun mute(on: Boolean) { service?.setMuted(on) }
    fun speaker(on: Boolean) {
        service?.setAudioRoute(if (on) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE)
    }
    fun dtmf(c: Char) { current?.playDtmfTone(c); current?.stopDtmfTone() }
    fun clearFinished() { if (current == null) _call.value = null }
}

class RazrInCallService : InCallService() {
    override fun onCreate() {
        super.onCreate()
        CallManager.service = this
    }

    override fun onDestroy() {
        if (CallManager.service === this) CallManager.service = null
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        CallManager.attach(call)
        if (call.state == Call.STATE_RINGING) CallManager.startRinging(this)
        // Bring the launcher forward so incoming calls are visible/answerable.
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            runCatching { startActivity(it) }
        }
    }

    fun endCurrentCall() {
        // Use the service call list as a fallback when the bridge reference is stale.
        calls.filter { it.state != Call.STATE_DISCONNECTED }.forEach { it.disconnect() }
    }

    override fun onCallRemoved(call: Call) = CallManager.detach(call)

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(audioState: CallAudioState) = CallManager.audioChanged(audioState)
}
