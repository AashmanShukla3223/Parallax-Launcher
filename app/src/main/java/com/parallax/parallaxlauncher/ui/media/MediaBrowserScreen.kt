package com.parallax.parallaxlauncher.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0E0F11)
private val Amber = Color(0xFFFF9500)
private val AmberDim = Color(0x99FF9500)
private val Steel = Color(0xFF26272B)
private val Ink = Color(0xFFE8E8ED)
private val Dim = Color(0xFF8A8A93)

private fun mono(size: Int, color: Color = Amber, weight: FontWeight = FontWeight.Normal, ls: Int = 2) =
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = size.sp,
        color = color,
        fontWeight = weight,
        letterSpacing = ls.sp,
    )

/**
 * Browse the device's indexed media and hand off to a real player or gallery.
 *
 * Parallax does not decode or play anything itself. A launcher that shipped its
 * own video decoder would be worse than the gallery that is already installed,
 * and every format it did not handle would be a dead end.
 */
@Composable
fun MediaBrowserScreen(
    state: MediaUiState,
    onKind: (MediaKind) -> Unit,
    onOpen: (MediaItem) -> Unit,
    onGrant: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(Bg)) {
        Text("MEDIA", style = mono(20, weight = FontWeight.Bold, ls = 3), modifier = Modifier.padding(20.dp))
        Text(
            "TAP TO CHOOSE AN APP",
            style = mono(11, Dim, ls = 2),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MediaKind.entries.forEach { k ->
                val sel = k == state.kind
                Box(
                    Modifier
                        .weight(1f)
                        .background(if (sel) Amber else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { onKind(k) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        k.label,
                        style = mono(
                            10,
                            if (sel) Color(0xFF0A0A0B) else AmberDim,
                            if (sel) FontWeight.Bold else FontWeight.Normal,
                            1,
                        ),
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            state.needsPermission -> Message(
                "PERMISSION NEEDED",
                "Android needs to be told it may read your music, video and photos.\n\nNothing leaves your device.",
                "GRANT", onGrant,
            )
            state.loading -> Message("SCANNING", "Reading what Android has indexed.", null, {})
            state.error != null -> Message("COULD NOT READ", state.error, "RETRY", onRetry)
            state.items.isEmpty() -> Message(
                "NOTHING HERE",
                "No ${state.kind.label.lowercase()} found, or none that Android has indexed.",
                "RETRY", onRetry,
            )
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.items, key = { it.uri.toString() }) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item) }
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(width = 40.dp, height = 40.dp)
                                .background(Steel, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(badgeFor(item), style = mono(13, AmberDim, ls = 0))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = mono(13, Ink, ls = 0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                buildString {
                                    append(item.subtitle)
                                    item.durationLabel()?.let { append("  $it") }
                                    if (item.sizeBytes > 0) append("  ${humanSize(item.sizeBytes)}")
                                },
                                style = mono(10, Dim, ls = 0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text("OPEN", style = mono(10, AmberDim, ls = 1))
                    }
                }
            }
        }
    }
}

private fun badgeFor(item: MediaItem): String = when {
    item.mime == "image/gif" -> "GIF"
    item.mime.startsWith("audio") -> "AUD"
    item.mime.startsWith("video") -> "VID"
    else -> "IMG"
}

private fun humanSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "%.1fGB".format(bytes / 1e9)
    bytes >= 1_000_000 -> "%.1fMB".format(bytes / 1e6)
    bytes >= 1_000 -> "%.0fKB".format(bytes / 1e3)
    else -> "$bytes B"
}

@Composable
private fun Message(title: String, body: String, action: String?, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(26.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = mono(16, Amber, FontWeight.Bold, 3))
        Spacer(Modifier.height(12.dp))
        Text(
            body,
            style = mono(12, Dim, ls = 1),
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier
                    .background(Amber, RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 26.dp, vertical = 13.dp),
            ) {
                Text(action, style = mono(13, Color(0xFF0A0A0B), FontWeight.Bold, 3))
            }
        }
    }
}

/** State for [MediaBrowserScreen]. */
data class MediaUiState(
    val kind: MediaKind = MediaKind.ALL,
    val items: List<MediaItem> = emptyList(),
    val loading: Boolean = true,
    val needsPermission: Boolean = false,
    val error: String? = null,
)