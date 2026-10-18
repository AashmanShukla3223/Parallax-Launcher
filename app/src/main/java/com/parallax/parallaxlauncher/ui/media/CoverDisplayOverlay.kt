package com.parallax.parallaxlauncher.ui.media

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext

/**
 * Presents the takeover on the *cover display* by making a window there.
 *
 * ## Why this exists at all
 *
 * On a Galaxy Z Flip5/Z Flip8 the activity stays on the inner display when the
 * device folds shut, and the inner display is switched `OFF`. Meanwhile the
 * cover display is owned by `com.android.systemui.subscreen.SubHomeActivity`.
 * Verified on both:
 *
 *   Parallax windows: mDisplayId=0     (inner, OFF)
 *   cover display:     mDisplayId=1    (SystemUI SubHomeActivity, uid system)
 *
 * `android:resizeableActivity` does not help, and that was the wrong thing to
 * try: it governs resizing *within* a display and recreation on configuration
 * change, not migration *across* Display instances. There is also no setting
 * that hands the Subscreen home to a third-party launcher —
 * `show_navigation_for_subscreen` and `cover_screen_show_notification_tip` are
 * display preferences, not ownership.
 *
 * So the only route left is to open a window on that display directly.
 *
 * ## What this needs, and whether it will work
 *
 * `TYPE_APPLICATION_OVERLAY` on a secondary display requires
 * `SYSTEM_ALERT_WINDOW`, which is granted by hand in Settings. Whether
 * Samsung lets an overlay cover its Subscreen home is **not something I can
 * promise**. It is built to be tried and measured, not assumed: [present]
 * returns false and [dismiss] tears down cleanly if the window manager refuses,
 * and [lastError] records why.
 */
object CoverDisplayOverlay {

    private const val TAG = "CoverOverlay"

    private var windowContext: Context? = null
    private var composeView: ComposeView? = null
    private val handler = Handler(Looper.getMainLooper())

    /** Why the last [present] or [dismiss] failed, or null. */
    @Volatile
    var lastError: String? = null
        private set

    /**
     * The cover display: the other built-in display.
     *
     * Filtering by `Display.TYPE_INTERNAL` / `FLAG_OWN_DISPLAY_ONLY` would be
     * the precise way to say this, but those constants do not resolve against
     * compileSdk 37 — verified, not guessed. So it is done structurally
     * instead: a clamshell has exactly two built-in panels, the activity's
     * context is bound to the inner one, and the other one is the cover.
     */
    fun coverDisplay(context: Context): Display? {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return null
        val defaultId = context.display?.displayId ?: Display.DEFAULT_DISPLAY
        return dm.displays.firstOrNull { d ->
            d.displayId != defaultId && d.isValid
        }
    }

    /**
     * Shows [content] on the cover display.
     *
     * Returns false when the display cannot be found or the window manager
     * refuses, in which case nothing is left behind. Safe to call repeatedly;
     * a second call replaces the content rather than stacking windows.
     */
    fun present(context: Context, content: @Composable () -> Unit): Boolean {
        lastError = null
        return onMainBlocking {
            runCatching {
                val display = coverDisplay(context)
                    ?: return@runCatching fail("no cover display (state=${context.display?.state})")

                if (composeView == null) {
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    // A window context is required: a View from the activity's
                    // context carries the activity's display with it, so
                    // adding it to the default window manager lands it back on
                    // the inner screen.
                    val ctx = context.createWindowContext(
                        // By display id, not by Display: the Display overload
                        // resolves to nothing usable here.
                        display.displayId,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    )
                    windowContext = ctx

                    val view = ComposeView(ctx).apply {
                        // Detach from the activity lifecycle; this view outlives
                        // whatever composition created it.
                        setViewCompositionStrategy(
                            ViewCompositionStrategy.DisposeOnDetachedFromWindow,
                        )
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setContent(content)
                    }
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT,
                    )
                    wm.addView(view, params)
                    composeView = view
                    isShowing = true
                } else {
                    composeView?.setContent(content)
                }
                true
            }.getOrElse { fail(it.toString()) }
        }
    }

    /** Takes the overlay down. Safe when nothing is showing. */
    fun dismiss(context: Context): Boolean = onMainBlocking {
        val view = composeView ?: return@onMainBlocking true
        runCatching {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(view)
            view.disposeComposition()
            composeView = null
            windowContext = null
            isShowing = false
            true
        }.getOrElse { fail(it.toString()) }
    }

    @Volatile
    var isShowing: Boolean = false
        private set

    private fun fail(why: String): Boolean {
        Log.w(TAG, "cover overlay failed: $why")
        lastError = why
        return false
    }

    /**
     * WindowManager calls must land on a thread with a Looper, and callers may
     * be on the Media3 poll thread.
     */
    private fun onMainBlocking(block: () -> Boolean): Boolean {
        if (Looper.myLooper() == Looper.getMainLooper()) return block()
        var result = false
        handler.post { result = block() }
        // Bounded wait rather than a block, so a stuck main thread cannot hang
        // the poll loop.
        val deadline = System.currentTimeMillis() + 2000
        while (System.currentTimeMillis() < deadline) {
            synchronized(this) { if (result) return true }
            Thread.sleep(20)
        }
        return result
    }
}