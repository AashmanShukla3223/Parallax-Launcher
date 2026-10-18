package com.parallax.parallaxlauncher.ui.media

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.ui.theme.ParallaxLauncherTheme

/**
 * Entry point when another app hands Parallax a media file via "Open with".
 *
 * Separate from MainActivity so opening a photo does not drag the whole launcher
 * up first, and so the launch is dismissible on the way out rather than leaving
 * a home screen behind it.
 *
 * [MediaOpener.openWith] has already tried the real chooser by the time this
 * panel appears. It is a fallback for the case where nothing on the device can
 * handle the file — deliberately *not* the iTunes player, since this is a
 * chooser that failed, not a track, and playback controls for a file nothing
 * can play would be a lie.
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
                    // Hand straight off. If nothing can open it, stay put and say
                    // so rather than silently finishing.
                    MediaOpener.openWith(context, item)
                }

                OpenedItemPanel(
                    item = resolved,
                    onDismiss = { finish() },
                    modifier = Modifier,
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
}

@Composable
private fun OpenedItemPanel(
    item: MediaItem?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F11))
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            item?.title ?: "Nothing to open",
            color = Color(0xFFFF9500),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "No installed app can open this file.",
            color = Color(0xFF8A8A93),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .background(Color(0xFFFF9500), RoundedCornerShape(8.dp))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(
                "BACK",
                color = Color(0xFF0A0A0B),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}