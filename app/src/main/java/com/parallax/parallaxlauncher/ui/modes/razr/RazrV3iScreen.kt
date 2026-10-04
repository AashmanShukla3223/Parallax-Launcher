package com.parallax.parallaxlauncher.ui.modes.razr

import android.Manifest
import android.app.KeyguardManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings as AndroidSettings
import android.telecom.Call
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.notifications.Headline
import com.parallax.parallaxlauncher.core.notifications.NotificationFeed
import com.parallax.parallaxlauncher.core.settings.Settings
import com.parallax.parallaxlauncher.core.telecom.CallManager
import com.parallax.parallaxlauncher.core.telecom.TonePlayer
import com.parallax.parallaxlauncher.core.telecom.VintageCarrierResolver
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrAboutScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverLocked
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverPanel
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverShell
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverLocked
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverPanel
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCalculatorPanel
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrDialingScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrHinge
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrHomeScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrIconGrid
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrInCallScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrIncomingCallScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrInboxList
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrKeypad
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrListScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrMessageMenu
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrNotificationAlert
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrPixelScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrRingStyleScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrRoutingHint
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrSoftKeyBar
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrStatusStrip
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrStatusGlyphs
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrTitleBar
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrThemeScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrToast
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrUnlockScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrUpperShell
import com.parallax.parallaxlauncher.ui.modes.razr.games.Nav
import com.parallax.parallaxlauncher.ui.modes.razr.games.RAZR_GAMES
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGame
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGameHost
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGamesMenu
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrScores
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mode 7 — Motorola RAZR V3i.
 *
 * A working clamshell: the upper shell carries the earpiece, the medallion and
 * the 2.2" 176x220 internal panel; the lower shell carries the laser-etched
 * keypad; and the hinge between them genuinely opens and closes. Closing the
 * flip hands the display over to the 96x80 external CSTN panel, exactly as the
 * handset does.
 *
 * Notification routing lives in [NotificationFeed]: while this mode owns the
 * foreground, incoming alerts are swallowed and rendered here as
 * "1 New Message Received". The moment the user leaves for another app, the
 * posting app's normal Android notification is allowed through untouched.
 */
@Composable
fun RazrV3iScreen(
    repo: AppsRepository,
    telemetry: TelemetryService,
    haptics: HapticEngine,
    settings: Settings,
    onSettingsChange: ((Settings) -> Settings) -> Unit = {},
) {
    val context = LocalContext.current
    val apps by repo.apps.collectAsState()
    val telemetryState by telemetry.state.collectAsState()
    val notificationItems by NotificationFeed.items.collectAsState()
    val intercepted by NotificationFeed.intercepted.collectAsState()
    val unread by NotificationFeed.unread.collectAsState()
    val messages = notificationItems.filter { it.isMessage }

    val palette = remember(settings.razrSkin) { RazrPalette.of(settings.razrSkin) }
    val carrierName = remember { VintageCarrierResolver.resolve(context) }
    val tonePlayer = remember { TonePlayer() }
    DisposableEffect(Unit) { onDispose { tonePlayer.release() } }

    // ---- Hardware state --------------------------------------------------------
    var flapOpen by rememberSaveable { mutableStateOf(false) }
    // The 2G handset only demanded its own unlock code while the device itself
    // was locked. Mirror the system lock so we never nag when the phone is
    // already unlocked, and fall back to the code when it is.
    val keyguard = remember { context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager }
    var locked by rememberSaveable { mutableStateOf(keyguard?.isKeyguardLocked() != false) }
    var unlockEntry by rememberSaveable { mutableStateOf("") }
    var unlockError by rememberSaveable { mutableStateOf<String?>(null) }

    var view by rememberSaveable { mutableStateOf(RazrView.STANDBY) }
    var menuIndex by rememberSaveable { mutableIntStateOf(0) }
    var subIndex by rememberSaveable { mutableIntStateOf(0) }
    var listIndex by rememberSaveable { mutableIntStateOf(0) }
    var dialBuffer by rememberSaveable { mutableStateOf("") }
    var lastDialed by rememberSaveable { mutableStateOf("") }
    var toast by rememberSaveable { mutableStateOf<String?>(null) }
    var callSeconds by rememberSaveable { mutableIntStateOf(0) }
    var callVolume by rememberSaveable { mutableIntStateOf(7) }
    var missedCalls by rememberSaveable { mutableIntStateOf(0) }
    var gameIndex by rememberSaveable { mutableIntStateOf(0) }
    var playingGameId by rememberSaveable { mutableStateOf<String?>(null) }
    val scores = remember { RazrScores(context) }
    val currentGame: RazrGame? = playingGameId?.let { id ->
        remember(playingGameId) {
            RAZR_GAMES.firstOrNull { g -> g().title == id }?.invoke()
        }
    }

    // ---- Clock -----------------------------------------------------------------
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    // 12-hour clock with a matching AM/PM marker — never 24-hour text next to "PM".
val clockText = remember(now) { SimpleDateFormat("h:mm", Locale.ROOT).format(Date(now)) }
    val amPm = remember(now) {
        SimpleDateFormat("a", Locale.ROOT).format(Date(now)).uppercase(Locale.ROOT)
    }
    val dateText = remember(now) { SimpleDateFormat("dd-MMM-yy", Locale.ROOT).format(Date(now)) }

    // ---- Telephony -------------------------------------------------------------
    val live by CallManager.call.collectAsState()
    val isRinging = live?.state == Call.STATE_RINGING
    val connectedNumber = live?.number.orEmpty()
    val isMuted = live?.muted ?: false
    val isOnHold = live?.onHold ?: false
    val isSpeaker = live?.speaker ?: false

    val currencySymbol = remember(settings.callCurrencyIndex) {
        when (settings.callCurrencyIndex) {
            0 -> "₹"; 1 -> "p"; 2 -> "$"; else -> "¢"
        }
    }
    // Two rates accrue and are summed:
    //   per-second   -> minor units (paise / cents), 100 minor = 1 major
    //   per-minute   -> major units (rupee / dollar), one whole unit per
    //                   completed minute
    val completedMinutes = callSeconds / 60
    val secondsAccruedMajor = settings.callPerSecondRate * callSeconds / 100f
    val minutesAccruedMajor = completedMinutes * settings.callTariffRate
    val totalCostMajor = secondsAccruedMajor + minutesAccruedMajor

    val minorSymbol = if (settings.callCurrencyIndex == 0 || settings.callCurrencyIndex == 1) "p" else "\u00A2"
    val perSecondLabel = remember(settings.callPerSecondRate, minorSymbol) {
        "$minorSymbol${trimNumber(settings.callPerSecondRate)}/sec"
    }
    val perMinuteLabel = remember(settings.callTariffRate, currencySymbol) {
        "$currencySymbol${trimNumber(settings.callTariffRate)}/min"
    }
    val secondsChargedLabel = remember(secondsAccruedMajor, minorSymbol) {
        "$minorSymbol${trimNumber(secondsAccruedMajor * 100f)}"
    }
    val minutesChargedLabel = remember(minutesAccruedMajor, currencySymbol) {
        "$currencySymbol${trimNumber(minutesAccruedMajor)}"
    }
    val totalLabel = remember(totalCostMajor, currencySymbol) {
        "$currencySymbol${trimNumber(totalCostMajor)}"
    }

    // ---- Permissions and roles -------------------------------------------------
    var pendingNumber by remember { mutableStateOf<String?>(null) }

    val credentialLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            locked = false
            unlockEntry = ""
            unlockError = null
            view = RazrView.STANDBY
            tonePlayer.playRazrChirp()
        } else {
            unlockError = "CANCELLED"
        }
    }

    fun placeCall(raw: String) {
        val clean = raw.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (clean.isEmpty()) return
        lastDialed = clean
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        runCatching { tm.placeCall(Uri.fromParts("tel", clean, null), Bundle()) }
            .onFailure { toast = "CALL FAILED - CHECK SIM" }
        dialBuffer = ""
        view = RazrView.IN_CALL
    }

    fun startCall(
        number: String,
        perm: ActivityResultLauncher<Array<String>>,
        role: ActivityResultLauncher<Intent>,
    ) {
        haptics.thud()
        val target = number.ifBlank { lastDialed }
        if (target.isBlank()) { tonePlayer.playRazrChirp(); return }
        if (!hasCallPermission(context)) {
            pendingNumber = target
            perm.launch(
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS)
            )
            return
        }
        if (!isDefaultDialer(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pendingNumber = target
            requestDialerRole(context) { role.launch(it) }
            val rm = context.getSystemService(RoleManager::class.java)
            if (!rm.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                pendingNumber = null
                placeCall(target)
            }
            return
        }
        placeCall(target)
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { pendingNumber?.let { placeCall(it); pendingNumber = null } }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        if (res[Manifest.permission.CALL_PHONE] == true) {
            requestDialerRole(context) { roleLauncher.launch(it) }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                pendingNumber?.let { placeCall(it); pendingNumber = null }
            }
        } else {
            pendingNumber = null
            toast = "CALL PERMISSION DENIED"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCallPermission(context)) {
            permLauncher.launch(
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS)
            )
        } else if (!isDefaultDialer(context)) {
            requestDialerRole(context) { roleLauncher.launch(it) }
        }
    }

    // Talk timer driven by the real connect timestamp.
    LaunchedEffect(live?.connectTimeMillis, live?.state) {
        val l = live ?: return@LaunchedEffect
        if (l.connectTimeMillis > 0L && l.state != Call.STATE_DISCONNECTED) {
            while (true) {
                callSeconds =
                    ((System.currentTimeMillis() - l.connectTimeMillis) / 1000L).toInt().coerceAtLeast(0)
                delay(500)
            }
        } else if (l.state == Call.STATE_RINGING || l.state == Call.STATE_DIALING) {
            callSeconds = 0
        }
    }

    // Drive the panel from real call state.
    LaunchedEffect(live?.state) {
        val l = live
        if (l == null) return@LaunchedEffect
        if (l.state == Call.STATE_DISCONNECTED) {
            tonePlayer.playBusy()
            val dur = String.format(Locale.ROOT, "%02d:%02d", callSeconds / 60, callSeconds % 60)
            toast = "ENDED - $dur - CHARGED $totalLabel"
            CallManager.clearFinished()
            dialBuffer = ""
            view = RazrView.STANDBY
        } else {
            view = if (l.state == Call.STATE_RINGING) RazrView.INCOMING else RazrView.IN_CALL
        }
    }

    // Tally unanswered calls so the cover display can show "X Missed Calls".
    var wasRinging by remember { mutableStateOf(false) }
    var everAnswered by remember { mutableStateOf(false) }
    LaunchedEffect(live?.state) {
        when {
            live?.state == Call.STATE_RINGING -> wasRinging = true
            live?.state == Call.STATE_ACTIVE || live?.state == Call.STATE_HOLDING -> everAnswered = true
            live?.state == Call.STATE_DISCONNECTED && wasRinging && !everAnswered -> {
                missedCalls += 1
                wasRinging = false
                everAnswered = false
            }
            live == null -> {
                wasRinging = false
                everAnswered = false
            }
        }
    }

    // Earpiece volume onto the real voice-call stream.
    LaunchedEffect(callVolume) {
        runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            am.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                (callVolume * max / 10).coerceAtLeast(1),
                0,
            )
        }
    }

    // Auto-dismiss the transient toast strip.
    LaunchedEffect(toast) {
        if (toast != null) { delay(2600); toast = null }
    }

    // Opening the inbox clears the unread tally, per the manual's message flow.
    LaunchedEffect(view) {
        if (view == RazrView.INBOX || view == RazrView.MESSAGES) NotificationFeed.markAllRead()
    }

    // ---- Intent helpers --------------------------------------------------------
    /**
     * Hands off to the real Android lock screen: swipe up, type the PIN, and we
     * are in. Falls back to simply unlocking when the device has no credential
     * set, and re-locks if the user backs out.
     */
    fun requestSystemUnlock() {
        val intent = runCatching {
            keyguard?.createConfirmDeviceCredentialIntent(
                "Unlock RAZR V3i",
                "Swipe up and enter your PIN, pattern, or password",
            )
        }.getOrNull()
        if (intent == null) {
            // No screen lock configured, so there is nothing to confirm.
            locked = false
            unlockError = null
            view = RazrView.STANDBY
            tonePlayer.playRazrChirp()
            return
        }
        runCatching { credentialLauncher.launch(intent) }
            .onFailure {
                // Device refused to show it (e.g. no credential set after all).
                locked = false
                unlockError = null
                view = RazrView.STANDBY
            }
    }

    fun open(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun openMessaging() {
        tonePlayer.playRazrChirp()
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
        runCatching { context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { open(Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))) }
    }

    fun openContacts() {
        tonePlayer.playRazrChirp()
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)
        runCatching { context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { open(Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)) }
    }

    fun openCalls() {
        tonePlayer.playRazrChirp()
        open(Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI))
    }

    fun openCamera() {
        haptics.thud()
        open(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    fun openBrowser() {
        tonePlayer.playRazrChirp()
        open(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")))
    }

    fun goHome() {
        view = RazrView.STANDBY
        subIndex = 0
        listIndex = 0
    }

    // ---- Menu selection --------------------------------------------------------
    fun selectMenuItem(item: RazrMenuItem) {
        tonePlayer.playRazrChirp()
        when (item.id) {
            1 -> { view = RazrView.CALLS; listIndex = 0 }
            2 -> { view = RazrView.MESSAGES; subIndex = 0 }
            3 -> { openContacts(); goHome() }
            4 -> openBrowser()
            5 -> { view = RazrView.GAMES; listIndex = 0 }
            6 -> { view = RazrView.RINGTONES; listIndex = 0 }
            7 -> { view = RazrView.TOOLS; subIndex = 0 }
            8 -> { view = RazrView.SETTINGS; subIndex = 0 }
            9 -> openCamera()
            else -> { listIndex = 0; view = RazrView.APPS_LIST }
        }
    }

    fun selectSettingsItem(item: RazrMenuItem) {
        tonePlayer.playRazrChirp()
        when (item.id) {
            101 -> { view = RazrView.THEME; subIndex = settings.razrSkin.ordinal }
            102 -> view = RazrView.RINGTONES
            106 -> view = RazrView.ABOUT
            108 -> { view = RazrView.TOOLS; subIndex = 0 }
            else -> { toast = item.title.uppercase(Locale.ROOT); goHome() }
        }
    }

    fun selectToolsItem(item: RazrMenuItem) {
        tonePlayer.playRazrChirp()
        when (item.id) {
            201 -> view = RazrView.CALCULATOR
            205 -> open(Intent(AlarmClock.ACTION_SET_ALARM))
            else -> { toast = item.title.uppercase(Locale.ROOT); goHome() }
        }
    }

    // ---- Keypad input ----------------------------------------------------------
    fun handleKey(ch: Char) {
        // A running game consumes every key itself.
        currentGame?.let { game ->
            if (view == RazrView.GAME) {
                when (ch) {
                    '0' -> { playingGameId = null; view = RazrView.GAMES }
                    '5' -> if (game.isOver) game.reset() else game.onKey(ch)
                    else -> game.onKey(ch)
                }
                tonePlayer.playDtmf(ch)
                return
            }
        }
        tonePlayer.playDtmf(ch)
        if (locked) {
            // '*' opens the real Android lock screen: swipe up, enter the PIN.
            if (ch == '*') {
                tonePlayer.playRazrChirp()
                requestSystemUnlock()
                return
            }
            if (ch.isDigit()) {
                unlockEntry += ch
                view = RazrView.UNLOCK
                if (unlockEntry.length >= 4) {
                    if (unlockEntry == settings.razrUnlockCode) {
                        locked = false
                        unlockEntry = ""
                        unlockError = null
                        view = RazrView.STANDBY
                        tonePlayer.playRazrChirp()
                    } else {
                        unlockError = "WRONG CODE"
                        unlockEntry = ""
                    }
                }
            }
            return
        }

        when (view) {
            RazrView.MESSAGES -> when (ch) {
                '1' -> { view = RazrView.INBOX; listIndex = 0 }
                '2' -> open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.razrVoicemailNumber}")))
                '3' -> openMessaging()
                else -> Unit
            }

            RazrView.IN_CALL -> {
                if (ch != 'C') CallManager.dtmf(ch)
                if (ch == '0' || ch == '+') callVolume = (callVolume + 1).coerceAtMost(10)
            }

            else -> {
                if (ch == 'C') {
                    if (dialBuffer.isNotEmpty()) dialBuffer = dialBuffer.dropLast(1)
                    else if (view == RazrView.DIALING) goHome()
                    return
                }
                dialBuffer += ch
                view = RazrView.DIALING
            }
        }
    }

    // ---- Rocker behaviour ------------------------------------------------------
    /** Volume-rocker shortcut from the manual: rotate through ring styles. */
    fun cycleRingStyle(delta: Int) {
        val count = RAZR_RING_STYLES.size
        val next = ((settings.razrRingStyleIndex + delta) % count + count) % count
        onSettingsChange { it.copy(razrRingStyleIndex = next) }
        toast = "RING STYLE: ${RAZR_RING_STYLES[next].name.uppercase(Locale.ROOT)}"
        tonePlayer.playRazrChirp()
    }

    fun onUp() {
        if (view == RazrView.GAME) { currentGame?.onNav(Nav.UP); return }
        when {
            locked -> Unit
            view == RazrView.STANDBY -> cycleRingStyle(1)
            view == RazrView.MAIN_MENU -> if (menuIndex >= 3) { menuIndex -= 3; tonePlayer.playRazrChirp() }
            view == RazrView.SETTINGS -> if (subIndex >= 3) { subIndex -= 3; tonePlayer.playRazrChirp() }
            view == RazrView.TOOLS -> if (subIndex >= 3) { subIndex -= 3; tonePlayer.playRazrChirp() }
            view == RazrView.MESSAGES -> if (subIndex > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.THEME -> if (subIndex > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.GAMES -> if (listIndex < RAZR_GAMES.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.APPS_LIST -> if (listIndex > 0) { listIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.INBOX -> if (listIndex > 0) { listIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.IN_CALL -> { callVolume = (callVolume + 1).coerceAtMost(10); tonePlayer.playRazrChirp() }
        }
    }

    fun onDown() {
        if (view == RazrView.GAME) { currentGame?.onNav(Nav.DOWN); return }
        when {
            locked -> Unit
            view == RazrView.STANDBY -> openCalls()
            view == RazrView.MAIN_MENU ->
                if (menuIndex <= RAZR_MAIN_MENU.size - 4) { menuIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.SETTINGS ->
                if (subIndex <= RAZR_SETTINGS_MENU.size - 4) { subIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.TOOLS ->
                if (subIndex <= RAZR_TOOLS_MENU.size - 4) { subIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.MESSAGES -> if (subIndex < 2) { subIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.THEME ->
                if (subIndex < RazrFinish.entries.lastIndex) { subIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.APPS_LIST ->
                if (listIndex < apps.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.INBOX ->
                if (listIndex < messages.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.IN_CALL -> { callVolume = (callVolume - 1).coerceAtLeast(1); tonePlayer.playRazrChirp() }
        }
    }

    fun onLeft() {
        if (view == RazrView.GAME) { currentGame?.onNav(Nav.LEFT); return }
        when {
            locked -> Unit
            view == RazrView.STANDBY -> { view = RazrView.INBOX; listIndex = 0 }
            view == RazrView.MAIN_MENU -> if (menuIndex % 3 > 0) { menuIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.SETTINGS -> if (subIndex % 3 > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.TOOLS -> if (subIndex % 3 > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
        }
    }

    fun onRight() {
        if (view == RazrView.GAME) { currentGame?.onNav(Nav.RIGHT); return }
        when {
            locked -> Unit
            view == RazrView.STANDBY -> openCamera()
            view == RazrView.MAIN_MENU -> if (menuIndex % 3 < 2) { menuIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.SETTINGS -> if (subIndex % 3 < 2) { subIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.TOOLS -> if (subIndex % 3 < 2) { subIndex += 1; tonePlayer.playRazrChirp() }
        }
    }

    fun onCenter() {
        if (locked) { tonePlayer.playRazrChirp(); return }
        haptics.thud()
        when (view) {
            RazrView.STANDBY -> { view = RazrView.MAIN_MENU; menuIndex = 0; tonePlayer.playRazrChirp() }
            RazrView.DIALING -> startCall(dialBuffer, permLauncher, roleLauncher)
            RazrView.MAIN_MENU -> selectMenuItem(RAZR_MAIN_MENU[menuIndex.coerceIn(RAZR_MAIN_MENU.indices)])
            RazrView.SETTINGS -> selectSettingsItem(RAZR_SETTINGS_MENU[subIndex.coerceIn(RAZR_SETTINGS_MENU.indices)])
            RazrView.TOOLS -> selectToolsItem(RAZR_TOOLS_MENU[subIndex.coerceIn(RAZR_TOOLS_MENU.indices)])
            RazrView.MESSAGES -> when (subIndex) {
                0 -> { view = RazrView.INBOX; listIndex = 0 }
                1 -> open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.razrVoicemailNumber}")))
                else -> openMessaging()
            }
            RazrView.THEME -> {
                onSettingsChange {
                    it.copy(razrSkin = RazrFinish.entries[subIndex.coerceIn(RazrFinish.entries.indices)])
                }
                toast = "THEME APPLIED"
                goHome()
            }
            RazrView.GAMES -> {
                val g = RAZR_GAMES[listIndex.coerceIn(RAZR_GAMES.indices)].invoke()
                playingGameId = g.title
                view = RazrView.GAME
                g.reset()
            }
            RazrView.GAME -> currentGame?.onNav(Nav.CENTRE)
            RazrView.APPS_LIST -> apps.getOrNull(listIndex)?.let {
                tonePlayer.playRazrChirp()
                repo.launch(it)
            }
            RazrView.INBOX -> openMessaging()
            RazrView.CALLS -> openCalls()
            RazrView.RINGTONES -> open(Intent(AndroidSettings.ACTION_SOUND_SETTINGS))
            RazrView.IN_CALL -> { CallManager.hold(!isOnHold); tonePlayer.playRazrChirp() }
            RazrView.INCOMING -> CallManager.answer()
            else -> tonePlayer.playRazrChirp()
        }
    }

    fun onSoftLeft() {
        tonePlayer.playRazrChirp()
        if (locked) return
        when (view) {
            RazrView.GAME, RazrView.GAMES -> { playingGameId = null; goHome() }
            RazrView.STANDBY -> { view = RazrView.INBOX; listIndex = 0 }
            RazrView.IN_CALL -> CallManager.mute(!isMuted)
            RazrView.INCOMING -> CallManager.reject()
            else -> goHome()
        }
    }

    fun onSoftRight() {
        tonePlayer.playRazrChirp()
        if (locked) return
        when (view) {
            RazrView.STANDBY -> openCamera()
            RazrView.IN_CALL -> CallManager.speaker(!isSpeaker)
            RazrView.INCOMING -> CallManager.answer()
            RazrView.DIALING -> { dialBuffer = ""; goHome() }
            else -> goHome()
        }
    }

    // ---- Soft-key captions -----------------------------------------------------
    val softLeft = when {
        locked -> "UNLOCK"
        view == RazrView.IN_CALL -> "MUTE"
        view == RazrView.INCOMING -> "DECLINE"
        view == RazrView.STANDBY -> "INBOX"
        view == RazrView.DIALING -> "CLEAR"
        else -> "BACK"
    }
    val softCenter = when {
        locked -> "MOTOROLA"
        view == RazrView.IN_CALL -> "HOLD"
        view == RazrView.INCOMING -> "ANSWER"
        view == RazrView.STANDBY -> "MENU"
        view == RazrView.DIALING -> "SEND"
        else -> "SELECT"
    }
    val softRight = when {
        locked -> ""
        view == RazrView.IN_CALL -> "SPKR"
        view == RazrView.INCOMING -> "ANSWER"
        view == RazrView.STANDBY -> "CAMERA"
        view == RazrView.DIALING -> "CANCEL"
        else -> "BACK"
    }

    val wallpaper = RazrWallpapers.all[
        settings.razrWallpaperIndex.coerceIn(RazrWallpapers.all.indices)
    ].res

    // ---- Chassis ---------------------------------------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0D))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Flip open: the full clamshell — inner panel, hinge, keypad.
      if (flapOpen) {
        RazrUpperShell(palette, Modifier.weight(1f).fillMaxWidth()) {
            RazrPixelScreen(palette = palette, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxSize()) {
                    RazrStatusStrip(palette) {
                        RazrStatusGlyphs(
                            palette = palette,
                            signalBars = 3,
                            batteryPercent = telemetryState.batteryPct.coerceAtLeast(0),
                            charging = telemetryState.charging,
                            unreadMessages = unread,
                            ringStyleName = RAZR_RING_STYLES[
                                settings.razrRingStyleIndex.coerceIn(RAZR_RING_STYLES.indices)
                            ].name,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    RazrTitleBar(palette, titleFor(view, isRinging, currentGame?.title ?: "Games"))

                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when {
                            isRinging -> RazrIncomingCallScreen(palette, connectedNumber)
                            locked -> RazrUnlockScreen(palette, unlockError)
                            else -> ActiveView(
                                view = view,
                                palette = palette,
                                settings = settings,
                                wallpaperRes = wallpaper,
                                apps = apps,
                                messages = messages,
                                menuIndex = menuIndex,
                                subIndex = subIndex,
                                listIndex = listIndex,
                                dialBuffer = dialBuffer,
                                clockText = clockText,

                                amPm = amPm,
                                dateText = dateText,
                                callSeconds = callSeconds,
                                callStatus = when {
                                    isOnHold -> "CALL ON HOLD"
                                    live?.state == Call.STATE_DIALING ||
                                        live?.state == Call.STATE_CONNECTING -> "CALLING..."
                                    else -> "CONNECTED"
                                },
                                number = connectedNumber,
                                perSecondLabel = perSecondLabel,
                                perMinuteLabel = perMinuteLabel,
                                secondsChargedLabel = secondsChargedLabel,
                                minutesChargedLabel = minutesChargedLabel,
                                totalLabel = totalLabel,
                                isMuted = isMuted,
                                isOnHold = isOnHold,
                                isSpeaker = isSpeaker,
                                batteryPct = telemetryState.batteryPct.coerceAtLeast(0),
                                missedCalls = missedCalls,
                                lastDialed = lastDialed,
                                scores = scores,
                                currentGame = currentGame,
                                playingGameId = playingGameId,
                                onPickGame = { idx ->
                                    playingGameId = RAZR_GAMES[idx].invoke().title
                                    view = RazrView.GAME
                                },
                                onExitGames = { playingGameId = null; view = RazrView.GAMES },
                            )
                        }

                        // The in-app alert floats over whatever view is showing.
                        Column(
                            Modifier.fillMaxSize().padding(3.dp),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            RazrNotificationAlert(
                                palette = palette,
                                alert = intercepted,
                                onOpenMessaging = { openMessaging() },
                            )
                        }

                        toast?.let { message ->
                            Box(
                                Modifier.fillMaxSize().padding(bottom = 24.dp),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                RazrToast(palette, message, Modifier.padding(horizontal = 6.dp))
                            }
                        }
                    }

                    RazrSoftKeyBar(palette, left = softLeft, center = softCenter, right = softRight)
                }
            }
        }

        RazrHinge(palette, Modifier.fillMaxWidth(), open = flapOpen) {
            haptics.thud()
            flapOpen = !flapOpen
            tonePlayer.playRazrChirp()
        }

            RazrKeypad(
                palette = palette,
                modifier = Modifier.weight(1.15f),
                haptics = haptics,
                onUp = { onUp() },
                onDown = { onDown() },
                onLeft = { onLeft() },
                onRight = { onRight() },
                onCenter = { onCenter() },
                onSoftLeft = { onSoftLeft() },
                onSoftRight = { onSoftRight() },
                onCall = {
                    when {
                        isRinging -> { haptics.thud(); CallManager.answer() }
                        view == RazrView.IN_CALL -> CallManager.hold(!isOnHold)
                        locked -> { view = RazrView.UNLOCK; unlockEntry = ""; unlockError = null }
                        else -> startCall(dialBuffer, permLauncher, roleLauncher)
                    }
                },
                onEnd = {
                    when {
                        isRinging -> { haptics.thud(); CallManager.reject() }
                        view == RazrView.IN_CALL -> { CallManager.hangUp(); goHome() }
                        locked -> { view = RazrView.STANDBY; unlockEntry = "" }
                        else -> { tonePlayer.playBusy(); dialBuffer = ""; goHome() }
                    }
                },
                onGlobe = {
                    if (locked) { view = RazrView.UNLOCK; unlockEntry = "" } else openBrowser()
                },
                onEnvelope = {
                    tonePlayer.playRazrChirp()
                    if (locked) { view = RazrView.UNLOCK; unlockEntry = ""; unlockError = null }
                    else { view = RazrView.INBOX; listIndex = 0 }
                },
                onVoice = {
                    tonePlayer.playRazrChirp()
                    toast = "VOICE COMMANDS"
                    goHome()
                },
                onKey = { handleKey(it) },
            )
      } else {
        // Flip closed: the inner panel is gone entirely. Only the lower shell
        // (the outer face) remains, carrying the wallpaper, the clock, and any
        // notification or message waiting for the user.
        Box(Modifier.weight(1f).fillMaxWidth()) {
            RazrCoverShell(
                palette = palette,
                modifier = Modifier.fillMaxSize(),
                onOpen = {
                    haptics.thud()
                    flapOpen = true
                    tonePlayer.playRazrChirp()
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    when {
                        locked -> RazrCoverLocked(
                            palette = palette,
                            onUnlock = {
                                haptics.thud()
                                requestSystemUnlock()
                            },
                        )

                        else -> RazrCoverPanel(
                            palette = palette,
                            wallpaperRes = wallpaper,
                            time = clockText,
                            amPm = amPm,
                            date = dateText,
                            alert = intercepted?.let {
                                if (it.headline.isMessage) {
                                    if (it.count == 1) "1 NEW MESSAGE" else "${it.count} NEW MESSAGES"
                                } else {
                                    if (it.count == 1) "1 NEW ALERT" else "${it.count} NEW ALERTS"
                                }
                            },
                            unread = unread,
                            missedCalls = missedCalls,
                        )
                    }
                }
            }
        }
      }
    }
}

/** Every internal-panel view, driven purely by the [RazrView] state. */
@Composable
private fun ActiveView(
    view: RazrView,
    palette: RazrPalette,
    settings: Settings,
    wallpaperRes: Int,
    apps: List<AppInfo>,
    messages: List<Headline>,
    menuIndex: Int,
    subIndex: Int,
    listIndex: Int,
    dialBuffer: String,
    clockText: String,

    amPm: String,
    dateText: String,
    callSeconds: Int,
    callStatus: String,
    number: String,
    perSecondLabel: String,
    perMinuteLabel: String,
    secondsChargedLabel: String,
    minutesChargedLabel: String,
    totalLabel: String,
    isMuted: Boolean,
    isOnHold: Boolean,
    isSpeaker: Boolean,
    batteryPct: Int,
    missedCalls: Int,
    lastDialed: String,
    scores: RazrScores,
    currentGame: RazrGame?,
    playingGameId: String?,
    onPickGame: (Int) -> Unit,
    onExitGames: () -> Unit,
) {
    when (view) {
        RazrView.STANDBY -> RazrHomeScreen(
            palette = palette,
            wallpaperRes = wallpaperRes,
            time = clockText,
            amPm = amPm,
            date = dateText,
        )

        RazrView.DIALING -> RazrDialingScreen(palette, dialBuffer, null)

        RazrView.MAIN_MENU -> RazrIconGrid(palette, RAZR_MAIN_MENU, menuIndex)

        RazrView.SETTINGS -> RazrIconGrid(palette, RAZR_SETTINGS_MENU, subIndex)

        RazrView.TOOLS -> RazrIconGrid(palette, RAZR_TOOLS_MENU, subIndex)

        RazrView.MESSAGES -> RazrMessageMenu(
            palette = palette,
            entries = RAZR_MESSAGE_ENTRIES,
            selectedIndex = subIndex,
        )

        RazrView.GAMES -> RazrGamesMenu(
            palette = palette,
            games = RAZR_GAMES,
            scores = scores,
            selectedIndex = listIndex,
        )

        RazrView.GAME -> {
            val game = currentGame
            if (game == null) {
                LaunchedEffect(Unit) { onExitGames() }
            } else {
                RazrGameHost(
                    palette = palette,
                    game = game,
                    scores = scores,
                    onExit = onExitGames,
                    onRetry = { game.reset() },
                )
            }
        }

        RazrView.INBOX -> RazrInboxList(palette, messages, listIndex)

        RazrView.APPS_LIST -> RazrListScreen(
            palette = palette,
            title = "Games & Apps",
            entries = apps,
            selectedIndex = listIndex,
            emptyText = "NO APPS FOUND",
            primary = { app: AppInfo -> app.label },
        )

        RazrView.CALLS -> RazrListScreen(
            palette = palette,
            title = "Recent Calls",
            entries = listOf(
                "Missed calls: $missedCalls",
                "Notepad: ${lastDialed.ifBlank { "-" }}",
            ),
            selectedIndex = 0,
            emptyText = "NO CALLS YET",
            primary = { it },
        )

        RazrView.RINGTONES -> RazrRingStyleScreen(
            palette = palette,
            names = RAZR_RING_STYLES.map { it.name },
            selectedIndex = settings.razrRingStyleIndex,
        )

        RazrView.THEME -> RazrThemeScreen(
            palette = palette,
            names = RazrFinish.entries.map { it.label },
            selectedIndex = settings.razrSkin.ordinal,
        )

        RazrView.IN_CALL -> RazrInCallScreen(
            palette = palette,
            number = number,
            timer = String.format(Locale.ROOT, "%02d:%02d", callSeconds / 60, callSeconds % 60),
            status = callStatus,
            perSecondLabel = perSecondLabel,
            perMinuteLabel = perMinuteLabel,
            secondsChargedLabel = secondsChargedLabel,
            minutesChargedLabel = minutesChargedLabel,
            totalLabel = totalLabel,
            muted = isMuted,
            onHold = isOnHold,
            speaker = isSpeaker,
        )

        RazrView.INCOMING -> RazrIncomingCallScreen(palette, number)

        RazrView.CALCULATOR -> RazrCalculatorPanel(
            palette = palette,
            rateText = perMinuteLabel,
            costText = totalLabel,
            callSeconds = callSeconds,
            batteryPct = batteryPct,
        )

        RazrView.ABOUT -> RazrAboutScreen(palette)

        else -> RazrHomeScreen(
            palette = palette,
            wallpaperRes = wallpaperRes,
            time = clockText,
            amPm = amPm,
            date = dateText,
        )
    }
}


private fun hasCallPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
        PackageManager.PERMISSION_GRANTED

private fun isDefaultDialer(context: Context): Boolean {
    val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    return tm.defaultDialerPackage == context.packageName
}

private fun requestDialerRole(context: Context, launch: (Intent) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val rm = context.getSystemService(RoleManager::class.java)
        if (rm.isRoleAvailable(RoleManager.ROLE_DIALER) && !rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
            launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        }
    }
}

/** Trims trailing zeros so 6.0 reads as "6" and 0.20 as "0.2". */
private fun trimNumber(v: Float): String =
    if (v == v.toInt().toFloat()) v.toInt().toString()
    else String.format(Locale.ROOT, "%.2f", v).trimEnd('0').trimEnd('.')

/** Messages sub-menu entries, mirroring the manual's Messages chapter. */
private val RAZR_MESSAGE_ENTRIES = listOf(
    RazrIcon.INBOX to "Message Inbox",
    RazrIcon.VOICEMAIL to "Voicemail",
    RazrIcon.MESSAGES to "Messaging App",
)

/** Title shown in the pale title bar for the current view. */
private fun titleFor(view: RazrView, isRinging: Boolean, currentGameTitle: String): String = when {
    isRinging -> "Incoming Call"
    view == RazrView.CALLS -> "Recent Calls"
    view == RazrView.APPS_LIST -> "Games & Apps"
    view == RazrView.INBOX -> "Message Inbox"
    view == RazrView.RINGTONES -> "Ring Styles"
    view == RazrView.THEME -> "Themes"
    view == RazrView.TOOLS -> "Tools"
    view == RazrView.IN_CALL -> "In Call"
    view == RazrView.DIALING -> "Dialing"
    view == RazrView.MESSAGES -> "Messages"
    view == RazrView.ABOUT -> "Phone Status"
    view == RazrView.CALCULATOR -> "Calculator"
    else -> "RAZR V3i"
}
