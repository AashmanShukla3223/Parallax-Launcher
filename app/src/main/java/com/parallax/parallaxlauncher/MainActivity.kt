package com.parallax.parallaxlauncher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.sensor.SensorHub
import com.parallax.parallaxlauncher.core.settings.SettingsRepository
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.celestial.OrbitOsScreen
import com.parallax.parallaxlauncher.ui.modes.cinecam.CineCamScreen
import com.parallax.parallaxlauncher.ui.modes.cinecam.ShutterEvents
import com.parallax.parallaxlauncher.ui.modes.cyberdeck.CyberdeckScreen
import com.parallax.parallaxlauncher.ui.modes.industrial.IndustrialRigScreen
import com.parallax.parallaxlauncher.ui.modes.razr.RazrV3iScreen
import com.parallax.parallaxlauncher.ui.modes.swiss.SwissBroadsheetScreen
import com.parallax.parallaxlauncher.ui.modes.telecom.TelecomRotaryScreen
import com.parallax.parallaxlauncher.ui.onboarding.OnboardingScreen
import com.parallax.parallaxlauncher.ui.settings.SettingsScreen
import com.parallax.parallaxlauncher.ui.theme.ParallaxLauncherTheme

class MainActivity : ComponentActivity() {
    private lateinit var repo: AppsRepository
    private lateinit var telemetry: TelemetryService
    private lateinit var sensors: SensorHub
    private lateinit var settingsRepo: SettingsRepository
    private var isDefault by mutableStateOf(false)
    private var onboarded by mutableStateOf(false)
    private var showSettings by mutableStateOf(false)

    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isDefault = checkDefault()
        }

    private fun checkDefault(): Boolean {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.resolveActivity(home, 0)?.activityInfo?.packageName == packageName
    }

    private fun requestDefault() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        runCatching { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repo = AppsRepository(this)
        telemetry = TelemetryService(this).also { it.start() }
        sensors = SensorHub(this)
        settingsRepo = SettingsRepository(this)
        val haptics = HapticEngine(this) { settingsRepo.state.value.haptics }
        val prefs = getSharedPreferences("parallax", Context.MODE_PRIVATE)
        onboarded = prefs.getBoolean("onboarded", false)
        isDefault = checkDefault()

        setContent {
            val s by settingsRepo.state.collectAsState()
            ParallaxLauncherTheme(
                accentId = s.accentColorId,
                backgroundId = s.backgroundColorId,
                fontId = s.fontId,
            ) {
                when {
                    !onboarded -> OnboardingScreen(
                        isDefaultHome = isDefault,
                        onSetDefault = ::requestDefault,
                        onFinish = {
                            prefs.edit().putBoolean("onboarded", true).apply()
                            onboarded = true
                        },
                    )
                    showSettings -> SettingsScreen(
                        settings = s,
                        onChange = settingsRepo::update,
                        isDefaultHome = isDefault,
                        onSetDefault = ::requestDefault,
                        onClearCounts = repo::clearCounts,
                        onReplayOnboarding = {
                            prefs.edit().putBoolean("onboarded", false).apply()
                            showSettings = false
                            onboarded = false
                        },
                        onReset = settingsRepo::reset,
                        onClose = { showSettings = false },
                    )
                    else -> Box(Modifier.fillMaxSize()) {
                        when (s.mode) {
                            2 -> OrbitOsScreen(repo, sensors, haptics, s.gravity, s.maxNodes)
                            3 -> SwissBroadsheetScreen(repo, telemetry, haptics)
                            4 -> CyberdeckScreen(repo, telemetry, haptics, s.crt)
                            5 -> CineCamScreen(repo, sensors, telemetry, haptics, s)
                            6 -> TelecomRotaryScreen(repo, haptics)
                            7 -> RazrV3iScreen(repo, telemetry, haptics, s)
                            else -> IndustrialRigScreen(repo, telemetry, haptics, s.detentDeg)
                        }
                        Row(
                            Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp),
                        ) {
                            if (s.showModeChip) {
                                Chip("MODE ${s.mode}") {
                                    haptics.click()
                                    settingsRepo.update { it.copy(mode = it.mode % 7 + 1) }
                                }
                            }
                            Chip("CFG") { haptics.click(); showSettings = true }
                        }
                    }
                }
            }
        }
    }

    // Volume rocker acts as dual-stage shutter in CineCam mode.
    private fun shutterActive() = onboarded && !showSettings && settingsRepo.state.value.mode == 5

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (shutterActive() && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (event.repeatCount == 0) ShutterEvents.events.tryEmit(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (shutterActive() && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            ShutterEvents.events.tryEmit(false)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        isDefault = checkDefault()
        sensors.start()
    }

    override fun onPause() {
        sensors.stop()
        super.onPause()
    }

    override fun onDestroy() {
        telemetry.stop()
        repo.close()
        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
private fun Chip(label: String, onClick: () -> Unit) {
    val palette = com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette.current
    Text(
        label,
        modifier = Modifier
            .padding(start = 6.dp)
            .background(Color(0x88000000), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = TextStyle(color = palette.accent, fontFamily = palette.font, fontSize = 11.sp),
    )
}