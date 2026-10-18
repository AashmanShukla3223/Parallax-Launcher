package com.parallax.parallaxlauncher.ui.media

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.parallax.parallaxlauncher.ui.theme.ParallaxLauncherTheme

/**
 * Entry point when another app hands Parallax a media file via "Open with".
 *
 * Separate from MainActivity so opening a photo does not drag the whole launcher
 * up first, and so the launch is dismissible on the way out rather than leaving
 * a home screen behind it.
 *
 * Tapping the item shows the chooser rather than opening it here. A launcher
 * showing a picture is not what the user asked for when they tapped "Open with";
 * they asked for the app that shows pictures, and Parallax being one of the
 * options is the point.
 */
class MediaViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming: Intent? = intent
        setContent {
            ParallaxLauncherTheme {
                val context = LocalContext.current
                var resolved by remember { mutableStateOf<MediaItem?>(null) }

                LaunchedEffect(incoming) {
                    val i = incoming ?: return@LaunchedEffect
                    val item = i.toMediaItem(context) ?: return@LaunchedEffect
                    resolved = item
                    // Hand straight off. If there is nothing that can open it,
                    // stay put and say so rather than silently finishing.
                    if (!MediaOpener.openWith(context, item)) {
                        resolved = item
                    }
                }

                NowPlayingPanel(
                    nowPlaying = resolved?.let { fakeNowPlaying(it) },
                    sourceLabel = "OPENED FROM ANOTHER APP",
                    onEmptyAction = { finish() },
                    modifier = Modifier,
                    background = Color(0xFF0E0F11),
                )
            }
        }
    }

    /** Wraps an incoming VIEW intent as a list item so one panel renders either way. */
    private fun Intent.toMediaItem(context: android.content.Context): MediaItem? {
        if (action != Intent.ACTION_VIEW) return null
        val uri = data ?: return null
        val mime = type ?: "*/*"
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Media"
        return MediaItem(
            id = 0L,
            uri = uri,
            title = name,
            subtitle = mime,
            mime = mime,
        )
    }

    private fun fakeNowPlaying(item: MediaItem) = NowPlaying(
        title = item.title,
        artist = item.mime,
        album = "",
        playing = false,
        packageName = packageName,
        durationMs = 0L,
        positionMs = 0L,
    )
}