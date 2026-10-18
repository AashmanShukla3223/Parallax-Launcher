package com.parallax.parallaxlauncher.ui.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionManager
import androidx.media3.session.SessionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Which way the track is set to loop. */
enum class RepeatMode(val label: String) {
    OFF("OFF"),
    ALL("ALL"),
    ONE("ONE"),
    ;

    fun next(): RepeatMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun of(v: Int): RepeatMode = when (v) {
            Player.REPEAT_MODE_ALL -> ALL
            Player.REPEAT_MODE_ONE -> ONE
            else -> OFF
        }
    }
}

/** Everything the player draws, and everything its keys act on. */
data class Transport(
    /** True while a media session is live and controllable right now. */
    val hasSession: Boolean = false,
    /**
     * True when there is a real track to display.
     *
     * Separate from [hasSession] because the two genuinely differ: pausing a
     * track ends the session but not the track. The panel gates on this, and
     * the transport row gates on [hasSession].
     */
    val hasTrack: Boolean = false,
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
    fun progress(): Float =
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    /** Time left, which is what iTunes showed on the right of the readout. */
    fun remainingMs(): Long = (durationMs - positionMs).coerceAtLeast(0L)

    companion object {
        /** `m:ss`, how both formats were written on a 2005 player. */
        fun clock(ms: Long): String {
            val total = (ms / 1000).coerceAtLeast(0)
            return "%d:%02d".format(total / 60, total % 60)
        }
    }
}

/**
 * Reads *and drives* whatever is playing.
 *
 * ## Why Media3 and not androidx.media
 *
 * The obvious choice was `MediaControllerCompat`, which can set repeat and
 * shuffle — something the platform `android.media.session.MediaController`
 * cannot do at all. It turns out androidx.media no longer ships those classes.
 * Checked rather than assumed: `androidx/media/media` 1.6.0, 1.7.0 and 1.8.0
 * were pulled and inspected, and none contains
 * `androidx/media/session/MediaControllerCompat` or `MediaSessionManagerCompat`.
 * Depending on it compiles to nothing usable. Media3 is the supported
 * replacement and has the same capability via `Player.setRepeatMode`.
 *
 * ## Discovery
 *
 * `MediaSessionManager.getActiveSessions(context, packageName = null, listener)`
 * returns a future of `SessionToken`s — `null` for the package name means *all*
 * packages. It needs either `MEDIA_CONTENT_CONTROL` or an enabled notification
 * listener; this app registers `ParallaxNotificationListener`, so the grant
 * Mode 7 already asks for is the one that matters. Without it the call throws
 * `SecurityException`, which is a normal state here rather than an error, so it
 * surfaces as [Transport.needsPermission].
 *
 * ## Why one controller
 *
 * Sessions are resolved once and then a `MediaController` is built for the one
 * in charge. Building a controller per session every tick would mean several
 * async binds 2x a second to answer a question the current controller already
 * knows; the alternative — a controller per session, ranked by `isPlaying` —
 * is more correct but costs a bind per session for a launcher that is only
 * being glanced at. The controller is kept for as long as its token stays
 * active, and re-bound only when the active set changes underneath it.
 *
 * ## Polling
 *
 * 500 ms while playing, 2 s while idle. A media session only reports position
 * when asked, so there is nothing to subscribe to, and the poll stops with the
 * composition rather than running behind a launcher the user has swiped away.
 */
class NowPlayingRepository(context: Context) {

    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(Transport())
    val state: StateFlow<Transport> = _state.asStateFlow()

    /**
     * Written on the main thread by the bind callback, read on the poll thread.
     * Without volatile the poll can keep seeing null for an unbounded time, and
     * a stale controller reference is exactly what makes a "session gone" look
     * like a crash.
     */
    @Volatile
    private var controller: MediaController? = null

    @Volatile
    private var boundToken: SessionToken? = null
    private var started = false

    /**
     * Last snapshot that came from a real session.
     *
     * Held because `getActiveSessions` reports what is *currently* active: the
     * moment a track pauses, Samsung Music marks its session inactive, the
     * token list goes empty and the controller is torn down. Without this the
     * panel blanks the instant you press pause, which is exactly backwards —
     * every real player freezes on the paused track. Seen on an SM-F776B:
     * pausing produced two controller Releases and an immediate "Play
     * something and it shows up here".
     */
    @Volatile
    private var lastKnown: Transport = Transport()

    /**
     * The caller's scope, kept so every controller call can be marshalled back
     * to the main thread. See [onMain].
     */
    private var mainScope: CoroutineScope? = null

    /** Starts polling. Safe to call more than once; only the first call starts. */
    fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        mainScope = scope
        scope.launch(Dispatchers.Default) {
            while (true) {
                // Every tick is guarded. A launcher that crashes because one
                // media read threw is worse than a launcher that shows stale
                // track info, and the session can vanish between the check and
                // the read.
                val next = try {
                    tick()
                } catch (e: Exception) {
                    Log.w("NowPlaying", "tick failed; retrying", e)
                    _state.value
                }
                _state.value = next
                delay(if (next.hasSession && next.playing) 500L else 2000L)
            }
        }
    }

    /**
     * Runs [block] on the main thread.
     *
     * **This is load-bearing, not stylistic.** A `MediaController` asserts the
     * thread it was built on for every method call and throws
     * `IllegalStateException: MediaController method is called from a wrong
     * thread` otherwise. The controller is built on the main looper, so *every*
     * read and every transport call has to come back to main — which the first
     * version got wrong and crashed nine times the moment a real session
     * appeared, having looked completely healthy through the empty state.
     */
    private fun onMain(block: () -> Unit) {
        val s = mainScope
        if (s == null) {
            runCatching { block() }
        } else {
            s.launch { runCatching { block() } }
        }
    }

    private suspend fun tick(): Transport {
        // Token resolution is the only part that may block, so it is the only
        // part that goes to IO.
        val tokens = withContext(Dispatchers.IO) { activeTokens() }
            ?: return Transport(needsPermission = true)

        if (tokens.isEmpty()) {
            releaseController()
            // Hold the last real track rather than blanking. `hasSession` is
            // deliberately dropped so the panel still knows it is showing a
            // paused track, and `playing` is false, so the transport row and
            // the PLAY/PAUSE caption both settle correctly.
            val held = lastKnown
            return if (held.title.isBlank()) {
                Transport()
            } else {
                Transport(
                    hasTrack = true,
                    title = held.title,
                    artist = held.artist,
                    album = held.album,
                    durationMs = held.durationMs,
                    positionMs = held.positionMs,
                    repeat = held.repeat,
                    shuffle = held.shuffle,
                    artwork = held.artwork,
                    playing = false,
                )
            }
        }

        val held = controller
        // Keep the current controller while its token is still live; otherwise
        // bind the first available session.
        if (held == null || boundToken !in tokens) {
            releaseController()
            bind(tokens.first())
            // The bind is async, so this tick reports "nothing" and the next
            // one 500 ms later has real state. Better than blocking the tick.
            return Transport()
        }

        return withContext(Dispatchers.Main.immediate) { snapshot(held) }
            .also { lastKnown = it }
    }

    /** Null means "no permission", empty means "nothing playing". */
    private fun activeTokens(): List<SessionToken>? = try {
        MediaSessionManager
            .getActiveSessions(appContext, null, LISTENER)
            .get(2, TimeUnit.SECONDS)
    } catch (_: SecurityException) {
        null
    } catch (_: Exception) {
        emptyList()
    }

    /**
     * Binds a controller and adopts it once the future resolves.
     *
     * `setApplicationLooper` is explicit because the poll runs on a background
     * dispatcher with no thread-local Looper, and Media3 throws rather than
     * defaulting somewhere useful. (It is `setApplicationLooper`, not
     * `setLooper` — there is no `setLooper` on MediaController.Builder.)
     */
    private fun bind(token: SessionToken) {
        val future = try {
            MediaController.Builder(appContext, token)
                .setApplicationLooper(Looper.getMainLooper())
                .buildAsync()
        } catch (_: Exception) {
            return
        }
        future.addListener(
            {
                val built = try {
                    future.get()
                } catch (_: Exception) {
                    null
                } ?: return@addListener
                // A session can vanish between deciding to bind and the bind
                // completing; do not clobber a newer controller with a stale one.
                if (boundToken != null && boundToken != token) {
                    runCatching { built.release() }
                    return@addListener
                }
                controller = built
                boundToken = token
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    private fun releaseController() {
        val c = controller ?: return
        controller = null
        boundToken = null
        // release() is a MediaController call, so it has to go through main too.
        onMain { runCatching { c.release() } }
    }

    private fun snapshot(c: MediaController): Transport {
        val meta: MediaMetadata = c.mediaMetadata
        return Transport(
            hasSession = true,
            hasTrack = true,
            // isPlaying rather than playbackState == PLAYING: Media3 keeps the
            // state coherent for us, and a paused track must still show.
            playing = c.isPlaying,
            title = meta.title?.toString().orEmpty().ifBlank { "Unknown track" },
            artist = meta.artist?.toString().orEmpty(),
            album = meta.albumTitle?.toString().orEmpty(),
            durationMs = c.duration.takeIf { it > 0 } ?: 0L,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            repeat = RepeatMode.of(c.repeatMode),
            shuffle = c.shuffleModeEnabled,
            artwork = meta.artworkData?.let { bytes ->
                // Media3 carries artwork as raw bytes plus a MIME type, not as
                // a Bitmap like the platform MediaMetadata did. Decoding it here
                // means the cost is paid once per snapshot rather than by the
                // session on every metadata update.
                runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()
            },
        )
    }

    // ---- Transport controls ----------------------------------------------------

    /**
     * Runs [block] against the bound controller.
     *
     * The controlling session can change between calls — the user can start
     * another app mid-song — so nothing is cached across calls beyond the
     * controller itself, and every failure is swallowed: a session disappearing
     * between pressing a key and the key landing is not worth a crash in a
     * launcher.
     *
     * Marshalled to main for the same reason as the poll: the controller
     * rejects calls from any other thread.
     */
    private fun withController(block: (MediaController) -> Unit) {
        onMain {
            val c = controller ?: return@onMain
            runCatching { block(c) }
        }
    }

    fun togglePlayPause() = withController { c ->
        if (c.isPlaying) c.pause() else c.play()
    }

    /** Scrub. Clamped so a stray tap past either end cannot wedge the session. */
    fun seekTo(ms: Long) = withController { c ->
        val d = c.duration
        c.seekTo(ms.coerceIn(0L, if (d > 0) d else Long.MAX_VALUE))
    }

    /** Nudges by [deltaMs]; the D-pad LEFT/RIGHT scrub rather than skip tracks. */
    fun nudge(deltaMs: Long) {
        val now = _state.value
        seekTo((now.positionMs + deltaMs).coerceAtLeast(0L))
    }

    fun next() = withController { it.seekToNext() }

    fun previous() = withController { it.seekToPrevious() }

    fun cycleRepeat() = withController { c ->
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun toggleShuffle() = withController { c ->
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    /**
     * Plays a MediaStore item in Parallax's own session.
     *
     * Preferred over taking over the session another app is already using: the
     * chosen track is ours, so binding our own [ParallaxMediaService] keeps the
     * two from fighting over one controller. Falls back to the active session
     * when our service is not reachable, which is what happens on a device
     * where playback has to go through whatever else is installed.
     */
    fun play(item: MediaItem) {
        val token = SessionToken(appContext, ComponentName(appContext, ParallaxMediaService::class.java))
        val future = try {
            MediaController.Builder(appContext, token)
                .setApplicationLooper(Looper.getMainLooper())
                .buildAsync()
        } catch (_: Exception) {
            null
        } ?: return

        future.addListener(
            {
                val controller = try {
                    future.get()
                } catch (_: Exception) {
                    null
                } ?: return@addListener
                onMain {
                    runCatching {
                        // Fully qualified: this package already has its own
                        // MediaItem for the media browser, and importing
                        // Media3's under the same name would silently shadow it.
                        controller.setMediaItem(
                            androidx.media3.common.MediaItem.fromUri(item.uri.toString()),
                        )
                        controller.prepare()
                        controller.play()
                    }
                }
                // Do NOT release the controller here. Releasing tears down the
                // binding to our own service, which is what keeps the track
                // playing. The next poll rediscovers this session through
                // getActiveSessions and takes over cleanly.
                lastKnown = Transport(
                    hasTrack = true,
                    playing = true,
                    title = item.title,
                    artist = item.subtitle,
                    durationMs = item.durationMs,
                )
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    private companion object {
        /**
         * The notification listener this app already registers. Passing it
         * scopes the query to sessions Parallax is allowed to see.
         */
        val LISTENER = ComponentName(
            "com.parallax.parallaxlauncher",
            "com.parallax.parallaxlauncher.core.notifications.ParallaxNotificationListener",
        )
    }
}