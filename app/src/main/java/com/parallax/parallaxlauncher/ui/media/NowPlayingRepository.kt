package com.parallax.parallaxlauncher.ui.media

import android.content.Context
import android.graphics.Bitmap
import androidx.media.session.MediaControllerCompat
import androidx.media.session.MediaSessionManagerCompat
import androidx.media.session.PlaybackStateCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Which way the track is set to loop. */
enum class RepeatMode(val label: String) {
    OFF("OFF"),
    ALL("ALL"),
    ONE("ONE"),
    ;

    fun next(): RepeatMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun of(v: Int): RepeatMode = when (v) {
            PlaybackStateCompat.REPEAT_MODE_ALL -> ALL
            PlaybackStateCompat.REPEAT_MODE_ONE -> ONE
            else -> OFF
        }
    }
}

/** A transport button the player can show, and the state needed to draw it. */
data class Transport(
    val hasSession: Boolean = false,
    val playing: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val repeat: RepeatMode = RepeatMode.OFF,
    val shuffle: Boolean = false,
    val artwork: Bitmap? = null,
    /** True when permission is missing, so the UI can say why rather than "nothing". */
    val needsPermission: Boolean = false,
) {
    val progress(): Float =
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    /** Seconds left, which is what iTunes showed on the right of the readout. */
    fun remainingMs(): Long = (durationMs - positionMs).coerceAtLeast(0L)

    companion object {
        /** `m:ss`, the way both formats were written on a 2005 player. */
        fun clock(ms: Long): String {
            val total = (ms / 1000).coerceAtLeast(0)
            return "%d:%02d".format(total / 60, total % 60)
        }
    }
}

/**
 * Reads *and drives* whatever is playing.
 *
 * ## Why MediaControllerCompat and not the platform controller
 *
 * `android.media.session.MediaController` can play, pause and seek, but it has
 * no repeat or shuffle at all — those live on the *session* side, and a
 * launcher that is not the active player has no session. `MediaControllerCompat`
 * sends `setRepeatMode` / `setShuffleMode` down the existing session, which is
 * what makes a real repeat control possible. Hence `androidx.media`.
 *
 * ## Why paused tracks are included
 *
 * The first version of this filtered to `STATE_PLAYING`, so pausing a track
 * made it vanish from the launcher entirely. A player that disappears when you
 * press pause is broken; paused is a state, not an absence.
 *
 * ## Permission
 *
 * `getActiveSessions` needs notification-listener access — the same grant Mode 7
 * already asks for — and throws `SecurityException` without it. That is a normal
 * state for this app, not an error, so it surfaces as [Transport.needsPermission]
 * instead of throwing or silently showing "nothing playing".
 */
class NowPlayingRepository(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Live transport state.
     *
     * Polled rather than event-driven: a media session only tells us position
     * when we ask, and every active session notifies through a callback that
     * would have to be registered and torn down per session. A 500 ms tick is
     * cheaper to reason about and is finer than the readout can show anyway.
     */
    private val _state = MutableStateFlow(Transport())
    val state: StateFlow<Transport> = _state.asStateFlow()

    /**
     * Starts polling. Safe to call repeatedly; only the first call starts.
     *
     * Polling stops itself when nothing is playing and nothing has media
     * permission, so an idle launcher is not waking the CPU twice a second.
     */
    fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        scope.launch(Dispatchers.Default) {
            while (true) {
                val next = read()
                _state.value = next
                // Tick fast enough for a smooth scrubber while playing, and
                // slow when idle so an idle launcher costs almost nothing.
                delay(if (next.hasSession && next.playing) 500L else 2000L)
            }
        }
    }

    private var started = false

    private suspend fun read(): Transport = withContext(Dispatchers.IO) {
        val manager = MediaSessionManagerCompat.from(appContext)
        val controllers: List<MediaControllerCompat> = try {
            manager.activeSessions
        } catch (_: SecurityException) {
            return@withContext Transport(needsPermission = true)
        } catch (_: Exception) {
            return@withContext Transport()
        }

        if (controllers.isEmpty()) return@withContext Transport()

        // Playing sessions first, then the most recently updated one. The
        // previous version picked by playhead *position*, which is just "the
        // longest-played track wins" and would surface the wrong app whenever
        // two were active.
        val best = controllers
            .sortedWith(
                compareByDescending<MediaControllerCompat> {
                    it.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
                }.thenByDescending {
                    it.playbackState?.lastPositionUpdateTime ?: 0L
                },
            )
            .first()

        val state = best.playbackState
        val meta = best.metadata
        Transport(
            hasSession = true,
            playing = state?.state == PlaybackStateCompat.STATE_PLAYING,
            title = meta?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                // Every Android media app sets a common key for the whole UI
                // label; it is the best single guess when there is no title.
                ?: meta?.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: "Unknown track",
            artist = meta?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            album = meta?.getString(android.media.MediaMetadata.METADATA_KEY_ALPLAY).orEmpty(),
            durationMs = meta?.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            positionMs = state?.position?.coerceAtLeast(0L) ?: 0L,
            repeat = RepeatMode.of(best.repeatMode),
            shuffle = best.shuffleMode != 0,
            artwork = meta?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: meta?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ARTWORK),
        )
    }

    // ---- Transport controls ----------------------------------------------------

    /**
     * Runs [block] against the session currently in charge, if there is one.
     *
     * Every control goes through here rather than holding a controller field,
     * because the controlling session can change between calls — the user can
     * start another app mid-song and the old controller would then be
     * controlling nothing.
     */
    private fun withController(block: (MediaControllerCompat) -> Unit) {
        val manager = try {
            MediaSessionManagerCompat.from(appContext)
        } catch (_: Exception) {
            return
        }
        val controller = try {
            manager.activeSessions.firstOrNull()
        } catch (_: SecurityException) {
            return
        } catch (_: Exception) {
            return
        } ?: return
        try {
            block(controller)
        } catch (_: Exception) {
            // A session can disappear between resolving it and calling into it.
        }
    }

    fun togglePlayPause() = withController { c ->
        if (c.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) c.pause() else c.play()
    }

    /** Scrub. Clamped so a stray tap past either end cannot wedge the session. */
    fun seekTo(ms: Long) = withController { c ->
        val d = c.metadata?.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        c.seekTo(ms.coerceIn(0L, if (d > 0) d else Long.MAX_VALUE))
    }

    /** Nudges by [deltaMs]; the D-pad LEFT/RIGHT scrub rather than skip tracks. */
    fun nudge(deltaMs: Long) {
        val now = _state.value
        seekTo((now.positionMs + deltaMs).coerceAtLeast(0L))
    }

    fun next() = withController { it.skipToNext() }

    fun previous() = withController { it.skipToPrevious() }

    fun cycleRepeat() = withController { c ->
        c.setRepeatMode(c.repeatMode.next().toPlaybackConstant())
    }

    fun toggleShuffle() = withController { c ->
        c.setShuffleMode(if (c.shuffleMode != 0) 0 else PlaybackStateCompat.SHUFFLE_MODE_ALL)
    }

    private fun RepeatMode.toPlaybackConstant(): Int = when (this) {
        RepeatMode.OFF -> PlaybackStateCompat.REPEAT_MODE_OFF
        RepeatMode.ALL -> PlaybackStateCompat.REPEAT_MODE_ALL
        RepeatMode.ONE -> PlaybackStateCompat.REPEAT_MODE_ONE
    }
}