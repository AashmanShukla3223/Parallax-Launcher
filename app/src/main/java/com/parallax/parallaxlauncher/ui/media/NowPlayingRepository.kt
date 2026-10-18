package com.parallax.parallaxlauncher.ui.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What is playing right now, as far as the system knows. */
data class NowPlaying(
    val title: String,
    val artist: String,
    val album: String,
    val playing: Boolean,
    val packageName: String,
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
 * Uses [MediaSessionManager.getActiveSessions], which needs the notification
 * listener permission rather than anything media-specific -- and this app
 * already registers a NotificationListenerService for Mode 7. So Now Playing
 * becomes available at the same moment the user grants notification access, with
 * no new prompt and nothing to explain.
 *
 * Without that grant the constructor throws SecurityException. That is expected,
 * not exceptional, so it is caught here and reported as "nothing playing" rather
 * than crashing a launcher.
 */
class NowPlayingRepository(private val context: Context) {

    suspend fun current(): NowPlaying? = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return@withContext null
        val sessions: List<MediaSession> = try {
            manager.activeSessions
        } catch (_: SecurityException) {
            return@withContext null
        } catch (_: Exception) {
            return@withContext null
        }

        sessions
            .asSequence()
            .filter { it.playbackState?.state == MediaSession.PlaybackState.STATE_PLAYING }
            .maxByOrNull { it.playbackState?.position ?: 0L }
            ?.toNowPlaying()
    }

    private fun MediaSession.toNowPlaying(): NowPlaying {
        val meta = metadata
        val state = playbackState
        val duration = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        return NowPlaying(
            title = meta?.title()?.toString().orEmpty().ifBlank { "Unknown track" },
            artist = meta?.artist()?.toString().orEmpty(),
            album = meta?.album()?.toString().orEmpty(),
            playing = state?.state == MediaSession.PlaybackState.STATE_PLAYING,
            packageName = sessionComponent?.packageName.orEmpty(),
            durationMs = duration,
            positionMs = state?.position ?: 0L,
        )
    }

    /** Human label for the app currently playing, or blank if it cannot be resolved. */
    fun labelFor(pkg: String): String = try {
        if (pkg.isBlank()) "" else context.packageManager
            .getApplicationLabel(
                context.packageManager.getApplicationInfo(pkg, 0)
            ).toString()
    } catch (_: Exception) {
        ""
    }

    companion object {
        fun component(context: Context): ComponentName =
            ComponentName(context, com.parallax.parallaxlauncher.core.notifications.ParallaxNotificationListener::class.java)
    }
}