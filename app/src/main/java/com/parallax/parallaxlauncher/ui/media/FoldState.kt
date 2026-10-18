package com.parallax.parallaxlauncher.ui.media

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * Whether the device is currently folded shut like a book.
 *
 * ## What counts
 *
 * A Z Flip reporting its hinge gives a `FoldingFeature` where:
 * - `orientation == HORIZONTAL` — a clamshell hinge runs across the middle,
 *   unlike the vertical crease of a book-style fold.
 * - `isSeparating` — the hinge genuinely splits the display.
 * - `state == FLAT` — fully closed. `HALF_OPENED` is the propped-open flex
 *   mode, which is not "flip is closed".
 *
 * All three are required. A vertical crease is a different shape, and a
 * non-separating fold is a seamless flexible screen that should never have its
 * screen taken over.
 *
 * ## Devices with no hinge
 *
 * Returns false forever, which is the correct answer: a slab phone has no
 * closed state to take over.
 */
@Composable
fun rememberFoldedShut(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = false) {
        val tracker = try {
            WindowInfoTracker.getOrCreate(context)
        } catch (_: Exception) {
            return@produceState
        }
        // The Activity overload is the one guaranteed present across versions,
        // and WindowInfoTracker only reports while the activity is started. Fall
        // back to the Context overload so this still works from a non-Activity
        // context rather than silently never firing.
        val flow = (context as? Activity)?.let { tracker.windowLayoutInfo(it) }
            ?: runCatching { tracker.windowLayoutInfo(context) }.getOrNull()
            ?: return@produceState

        try {
            flow.collect { info ->
                value = info.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .any {
                        it.orientation == FoldingFeature.Orientation.HORIZONTAL &&
                            it.isSeparating &&
                            it.state == FoldingFeature.State.FLAT
                    }
            }
        } catch (_: Exception) {
            // Leave it false. The takeover must never appear on hardware that
            // cannot report a hinge.
        }
    }
}