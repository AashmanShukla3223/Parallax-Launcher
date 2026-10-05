package com.parallax.parallaxlauncher

import android.Manifest
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compile-level verification is all the local `testDebugUnitTest` can prove, and the dev
 * machines cannot run an AVD: nested virt is not exposed (no /dev/kvm) and there is under
 * 1 GB of RAM. These assertions run on a real device instead (GitHub Actions emulator, or a
 * physical device on Firebase Test Lab).
 *
 * Scope is deliberately narrow: every mode screen must compose without throwing. Anything
 * richer needs hardware this test cannot fake (see AGENTS.md).
 */
@RunWith(AndroidJUnit4::class)
class ModeSmokeTest {

    /**
     * RazrV3iScreen asks for the call permissions the moment it composes (RazrV3iScreen.kt:364).
     * Without this rule that system dialog opens on top of the activity and the test measures
     * the dialog instead of the screen.
     */
    @get:Rule
    val callPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS,
    )

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
    fun everyModeLaunchesAndDrawsItsContent() {
        for (mode in 1..7) {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putInt("mode", mode)
                .commit()

            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                // The content view is the real signal: it is only attached once the mode
                // composable has drawn, so a throw inside setContent fails here rather than
                // passing silently.
                onView(withId(android.R.id.content)).check(matches(isDisplayed()))
                assertNotFinishing(scenario, mode)

                val state = awaitResumed(scenario)
                if (mode == RAZR_MODE) {
                    // Mode 7 requests ROLE_DIALER on entry (RazrV3iScreen.kt:369). That system
                    // dialog parks the activity at STARTED, which is correct behaviour rather
                    // than a crash, so accept either state here.
                    assertTrue(
                        "mode $mode ended in $state; expected RESUMED or STARTED (dialer role " +
                            "dialog is allowed to cover it)",
                        state == Lifecycle.State.RESUMED || state == Lifecycle.State.STARTED,
                    )
                } else {
                    assertEquals(
                        "mode $mode never reached RESUMED (threw during onCreate/setContent?)",
                        Lifecycle.State.RESUMED,
                        state,
                    )
                }
            }
        }
    }

    private fun assertNotFinishing(scenario: ActivityScenario<*>, mode: Int) {
        scenario.onActivity { activity ->
            assertTrue("mode $mode activity is finishing", !activity.isFinishing)
        }
    }

    /**
     * Mode 7 is the heaviest screen to draw and CI renders it through swiftshader, so the
     * lifecycle can still be settling when launch() returns. Poll rather than sampling once:
     * a single sample read STARTED for mode 7 on a passing emulator.
     */
    private fun awaitResumed(scenario: ActivityScenario<*>): Lifecycle.State {
        val deadline = SystemClock.uptimeMillis() + RESUME_TIMEOUT_MS
        var state = scenario.state
        while (state != Lifecycle.State.RESUMED && SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_MS)
            state = scenario.state
        }
        return state
    }

    private companion object {
        const val RAZR_MODE = 7
        const val RESUME_TIMEOUT_MS = 20_000L
        const val POLL_INTERVAL_MS = 200L
    }
}