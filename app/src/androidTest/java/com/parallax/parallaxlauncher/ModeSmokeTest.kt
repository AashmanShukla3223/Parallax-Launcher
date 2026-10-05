package com.parallax.parallaxlauncher

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compile-level verification is all the local `testDebugUnitTest` can prove, and this
 * machine has no KVM / not enough RAM for a usable emulator. These assertions run on a real
 * device instead (GitHub Actions emulator, or a physical device on Firebase Test Lab).
 *
 * Scope is deliberately narrow: every mode screen must compose without throwing. Anything
 * richer needs the hardware this test cannot fake (see AGENTS.md).
 */
@RunWith(AndroidJUnit4::class)
class ModeSmokeTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun seedOnboardedFlag() {
        // MainActivity gates every mode screen behind the "parallax"/onboarded flag and shows
        // OnboardingScreen until it is set. commit() rather than apply(): apply() is async and
        // the launch below could otherwise race the write and only ever see onboarding.
        context.getSharedPreferences("parallax", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarded", true)
            .commit()
    }

    @After
    fun clearPrefs() {
        context.getSharedPreferences("parallax", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    /**
     * MainActivity reads `settings`/`mode` fresh in onCreate, so relaunching per mode is enough
     * to walk all seven screens. SettingsRepository clamps to 1..7, matching this range.
     */
    @Test
    fun everyModeLaunchesAndReachesResumed() {
        for (mode in 1..7) {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putInt("mode", mode)
                .commit()

            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertEquals(
                    "mode $mode never reached RESUMED (activity threw during onCreate/setContent?)",
                    Lifecycle.State.RESUMED,
                    scenario.state,
                )
                // A crash inside a LaunchedEffect/Canvas draw surfaces here rather than as a
                // silent pass, because the content view never gets attached.
                onView(withId(android.R.id.content)).check(matches(isDisplayed()))
                scenario.onActivity { activity ->
                    assertFalse("mode $mode activity is finishing", activity.isFinishing)
                }
            }
        }
    }
}