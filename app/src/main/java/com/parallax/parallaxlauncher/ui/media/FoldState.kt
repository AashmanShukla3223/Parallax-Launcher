package com.parallax.parallaxlauncher.ui.media

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

/**
 * What the hinge is doing, if this device has a hinge at all.
 *
 * @param foldable whether the OS reports any folding feature. The V3i's own
 *   flip is drawn, not sensed, so it is *not* foldable in this sense — which is
 *   why both halves exist.
 * @param foldedShut true only when a hinge is fully closed.
 */
data class FoldState(val foldable: Boolean = false, val foldedShut: Boolean = false)

/**
 * Reads the real hinge state.
 *
 * ## What counts as folded shut
 *
 * A Z Flip reporting its hinge gives a `FoldingFeature` where:
 * - `orientation == HORIZONTAL` — a clamshell hinge runs across the middle,
 *   unlike the vertical crease of a book-style fold.
 * - `isSeparating` — the hinge genuinely splits the display.
 * - `state == FLAT` — fully closed. `HALF_OPENED` is the propped-open flex
 *   mode, which is not "closed".
 *
 * All three are required. A vertical crease is a different shape, and a
 * non-separating fold is a seamless flexible screen that should never have its
 * screen taken over.
 *
 * ## Why [FoldState.foldable] is separate
 *
 * On a real foldable the sensor has to be the authority for the takeover,
 * because unfolding has to end it. If the closed state also came from the V3i's
 * drawn flap, unfolding a Z Flip would leave the takeover stuck on the inner
 * screen with no keypad to dismiss it. So: foldable means the hinge decides,
 * not foldable means the drawn flip decides.
 *
 * ## Devices with no hinge
 *
 * Reports `FoldState()` — never foldable, never folded — which is the correct
 * answer for a slab phone.
 */
@Composable
fun rememberFoldState(): State<FoldState> {
    val context = LocalContext.current
    return produceState(initialValue = FoldState()) {
        val tracker = try {
            WindowInfoTracker.getOrCreate(context)
        } catch (_: Exception) {
            return@produceState
        }
        // The Activity overload is guaranteed present across versions and only
        // reports while the activity is started. Fall back to the Context
        // overload rather than silently never firing.
        val flow = (context as? Activity)?.let { tracker.windowLayoutInfo(it) }
            ?: runCatching { tracker.windowLayoutInfo(context) }.getOrNull()
            ?: return@produceState

        try {
            flow.collect { info ->
                val folds = info.displayFeatures.filterIsInstance<FoldingFeature>()
                value = FoldState(
                    foldable = folds.isNotEmpty(),
                    foldedShut = folds.any {
                        it.orientation == FoldingFeature.Orientation.HORIZONTAL &&
                            it.isSeparating &&
                            it.state == FoldingFeature.State.FLAT
                    },
                )
            }
        } catch (_: Exception) {
            // Leave it as "not a foldable". The takeover must never appear on
            // hardware that cannot report a hinge.
        }
    }
}