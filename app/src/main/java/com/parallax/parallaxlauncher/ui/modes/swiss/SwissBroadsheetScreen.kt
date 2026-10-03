package com.parallax.parallaxlauncher.ui.modes.swiss

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.notifications.NotificationFeed
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.swiss.components.Classifieds
import com.parallax.parallaxlauncher.ui.modes.swiss.components.LeadStories
import com.parallax.parallaxlauncher.ui.modes.swiss.components.Masthead
import com.parallax.parallaxlauncher.ui.modes.swiss.shaders.PaperGrain

private fun wireEnabled(ctx: Context): Boolean =
    Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners")
        ?.contains(ctx.packageName) == true

@Composable
fun SwissBroadsheetScreen(repo: AppsRepository, telemetry: TelemetryService, haptics: HapticEngine) {
    val ctx = LocalContext.current
    val apps by repo.apps.collectAsState()
    val counts by repo.launchCounts.collectAsState()
    val tel by telemetry.state.collectAsState()
    val news by NotificationFeed.items.collectAsState()
    val paper = remember { PaperGrain() }
    val list = rememberLazyListState()

    // Hairline-snap haptic whenever the first visible item changes while scrolling.
    LaunchedEffect(list) {
        androidx.compose.runtime.snapshotFlow { list.firstVisibleItemIndex }
            .collect { haptics.tick() }
    }

    val leads = remember(apps, counts) {
        apps.sortedByDescending { counts[it.packageName] ?: 0 }.take(2)
    }
    val rest = remember(apps, leads) { apps.filterNot { it in leads } }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { paper.draw(this) }
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp),
            state = list,
        ) {
            item {
                Masthead(
                    batteryMv = tel.batteryMv,
                    headline = news.firstOrNull(),
                    wireEnabled = wireEnabled(ctx),
                    onEnableWire = {
                        runCatching {
                            ctx.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                    onHeadline = { h -> apps.firstOrNull { it.packageName == h.packageName }?.let(repo::launch) },
                )
            }
            item { LeadStories(leads, haptics::click) { haptics.thud(); repo.launch(it) } }
            item { Classifieds(rest, haptics::click) { haptics.thud(); repo.launch(it) } }
        }
    }
}
