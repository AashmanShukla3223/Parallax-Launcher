package com.parallax.parallaxlauncher.ui.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What is playing right now, as far as the system knows. */
data class NowPlaying(
    val title: String,
    val artist: String,
    val album: String,
    val playing: Boolean,
    val durationMs: Long,
    val positionMs: Long,
) {
    fun progress(): Float =
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    fun positionLabel(): String {
        val s = positionMs / 1000
        return "%d:%02d".format(s / 60, s % 60)
    }

    fun durationLabel(): String {
        val s = durationMs / 1000
        return "%d:%02d".format(s / 60, s % 60)
    }
}

/**
 * Reads the active media session.
 *
 * [MediaSessionManager.getActiveSessions] returns MediaControllers, not
 * MediaSessions -- the token-based API is the one that does not require holding
 * a session yourself. It needs notification listener permission, which this app
 * already registers for Mode 7, so Now Playing becomes available at the same
 * moment the user grants notification access and there is no new prompt to
 * explain.
 *
 * Without that grant it throws SecurityException. That is expected rather than
 * exceptional, so it is caught and reported as "nothing playing" instead of
 * taking down a launcher.
 */
class NowPlayingRepository(private val context: Context) {

    suspend fun current(): NowPlaying? = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return@withContext null
        val controllers: List<MediaController> = try {
            manager.getActiveSessions(notificationListener)
        } catch (_: SecurityException) {
            // Notification access not granted.
            return@withContext null
        } catch (_: Exception) {
            return@withContext null
        }

        controllers
            .asSequence()
            .filter { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            // Most recently active wins; several apps can hold sessions at once.
            .maxByOrNull { it.playbackState?.position ?: 0L }
            ?.let { c ->
                val meta = c.metadata
                // getString via the METADATA_KEY_ constants rather than the
                // title()/artist()/album() helpers, which do not resolve against
                // this SDK. Bundle access has worked on every API level.
                NowPlaying(
                    title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                        .orEmpty()
                        .ifBlank { "Unknown track" },
                    artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
                    album = meta?.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty(),
                    playing = c.playbackState?.state == PlaybackState.STATE_PLAYING,
                    durationMs = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                    positionMs = c.playbackState?.position ?: 0L,
                )
            }
    }

    private companion object {
        /**
         * Passing our own listener component scopes the result to this app and is
         * the documented form since API 23.
         */
        val notificationListener = android.content.ComponentName(
            "com.parallax.parallaxlauncher",
            "com.parallax.parallaxlauncher.core.notifications.ParallaxNotificationListener",
        )
    }
}