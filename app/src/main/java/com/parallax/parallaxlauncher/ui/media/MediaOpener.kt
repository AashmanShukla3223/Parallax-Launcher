package com.parallax.parallaxlauncher.ui.media

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Hands a media item to whatever the user wants to open it with.
 *
 * Deliberately an explicit `ACTION_VIEW` chooser rather than letting the system
 * pick. Two reasons: the point of the screen is that Parallax is *an* option
 * rather than the only one, and silently defaulting a video to some app the user
 * has never chosen is exactly the behaviour people dislike about launchers.
 */
object MediaOpener {

    /**
     * Opens [item] in the system chooser, with Parallax offered too.
     *
     * `CATEGORY_BROWSABLE` is included deliberately: without it the chooser hides
     * Parallax from its own list, because the app appears to be opening its own
     * file.
     */
    fun openWith(context: Context, item: MediaItem): Boolean = try {
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(item.uri, item.mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(view, item.title)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        chooser.addCategory(Intent.CATEGORY_BROWSABLE)
        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
            true
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }

    /**
     * The list of apps that could open [item], for a screen that shows the
     * options rather than launching the chooser.
     *
     * Package visibility means this only returns what the manifest `<queries>`
     * block declares, so it is a short list on modern Android rather than every
     * installed app.
     */
    fun handlersFor(context: Context, item: MediaItem): List<Pair<String, String>> = try {
        val pm = context.packageManager
        pm.queryIntentActivities(
            Intent(Intent.ACTION_VIEW).setDataAndType(item.uri, item.mime),
            0,
        )
            .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
            .distinctBy { it.first to it.second }
            .sortedBy { it.first.lowercase() }
    } catch (_: Exception) {
        emptyList()
    }

    /** True when there is only one thing that can open it, so no choice is needed. */
    fun isSoleHandler(context: Context, item: MediaItem): Boolean =
        handlersFor(context, item).size <= 1
}