package com.parallax.parallaxlauncher.ui.media

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Parallax's own media session, so the launcher can be the thing playing.
 *
 * ## Why this exists
 *
 * "Choose a song and play" cannot be satisfied by handing the URI to another
 * app. An SM-F776B has no music player installed at all — verified, not
 * assumed: `query-activities` for `audio/mpeg` returned only Parallax itself.
 * With nothing to hand off to, the chooser had nothing to choose and Now
 * Playing was permanently empty. So Parallax decodes its own audio.
 *
 * This reverses an earlier decision recorded in `MediaBrowserScreen`: that a
 * launcher shipping its own decoder was "worse than the gallery that is
 * already installed". That was right when the only use was *viewing* a file
 * someone else chose. It is wrong for playback, where being unable to play is
 * worse than having a second player.
 *
 * ## Why a MediaSessionService and not a bare ExoPlayer
 *
 * Because `NowPlayingRepository` already drives everything through a
 * `MediaController`, and this app should look exactly like any other player
 * from the outside: it appears in the system's media controls, it obeys
 * headset buttons, and it does not need a special case just because it is the
 * launcher. Without the session, our own playback would be invisible to the
 * lock screen and to the media notification.
 */
class ParallaxMediaService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                // USAGE_MEDIA rather than USAGE_VOICE_COMMUNICATION: this is
                // music, and it should be routable and duckable like any other
                // media app.
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            // Wake and stay awake so a track does not stop when the cover
            // display sleeps mid-song.
            .setHandleAudioBecomingNoisy(true)
            .build()

        session = MediaSession.Builder(this, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, com.parallax.parallaxlauncher.MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        // A launcher outlives its task, so tearing the player down here would
        // silence music the moment the user swipes the home screen away.
        // Deliberately not calling stop().
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}