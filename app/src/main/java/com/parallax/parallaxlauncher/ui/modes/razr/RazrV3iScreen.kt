package com.parallax.parallaxlauncher.ui.modes.razr

import android.Manifest
import android.app.KeyguardManager
import android.app.role.RoleManager
import android.content.ClipData
import android.content.ClipboardManager
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
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore
import android.provider.Settings as AndroidSettings
import android.telecom.Call
import android.telecom.TelecomManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import com.parallax.parallaxlauncher.core.telecom.SecretCodes
import com.parallax.parallaxlauncher.core.telecom.TonePlayer
import com.parallax.parallaxlauncher.core.telecom.VintageCarrierResolver
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrAboutScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverInCall
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrCoverIncomingCall
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
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrActionSheet
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrListScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrNoSimNotice
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrSecretCodeList
import com.parallax.parallaxlauncher.ui.media.ItunesNowPlaying
import com.parallax.parallaxlauncher.ui.media.NowPlayingRepository
import com.parallax.parallaxlauncher.ui.media.Transport
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

import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrUpperShell
import com.parallax.parallaxlauncher.ui.modes.razr.games.Nav
import com.parallax.parallaxlauncher.ui.modes.razr.games.RAZR_GAMES
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGame
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGameHost
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrGamesMenu
import com.parallax.parallaxlauncher.ui.modes.razr.games.RazrScores
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mode 7 ? Motorola RAZR V3i.
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

    // Contacts for the in-panel Phonebook. Loaded lazily the first time the
    // view is opened rather than at composition, so the app does not sit on a
    // content provider query on every recomposition of the whole chassis.
    var contacts by remember { mutableStateOf<List<RazrContact>>(emptyList()) }
    var contactsRequested by rememberSaveable { mutableStateOf(false) }
    var contactActionIndex by rememberSaveable { mutableIntStateOf(0) }
    var refusedNumber by remember { mutableStateOf("") }

    val palette = remember(settings.razrSkin) { RazrPalette.of(settings.razrSkin) }
    val carrierName = remember { VintageCarrierResolver.resolve(context) }
    val tonePlayer = remember { TonePlayer() }
    DisposableEffect(Unit) { onDispose { tonePlayer.release() } }

    // ---- Hardware state --------------------------------------------------------
    var flapOpen by rememberSaveable { mutableStateOf(false) }

    // The flip is a real mechanical action, so it animates about the hinge
    // line rather than popping between states.
    val flip by animateFloatAsState(
        targetValue = if (flapOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "flip",
    )

    fun setFlap(open: Boolean) {
        if (flapOpen != open) haptics.thud()
        flapOpen = open
        tonePlayer.playRazrChirp()
    }

    // System back closes the flip before it leaves the launcher.
    BackHandler(enabled = flapOpen) { setFlap(false) }
    // The 2G handset only demanded its own unlock code while the device itself
    // was locked. Mirror the system lock so we never nag when the phone is
    // already unlocked, and fall back to the code when it is.
    val keyguard = remember { context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager }
    var locked by rememberSaveable { mutableStateOf(keyguard?.isKeyguardLocked() != false) }
    

    var view by rememberSaveable { mutableStateOf(RazrView.STANDBY) }
    var menuIndex by rememberSaveable { mutableIntStateOf(0) }
    var subIndex by rememberSaveable { mutableIntStateOf(0) }
    var listIndex by rememberSaveable { mutableIntStateOf(0) }
    var dialBuffer by rememberSaveable { mutableStateOf("") }
    var lastDialed by rememberSaveable { mutableStateOf("") }
    var toast by rememberSaveable { mutableStateOf<String?>(null) }
    // Bumped on every outgoing attempt so the watchdog below re-arms per call.
    var dialAttempt by remember { mutableStateOf(0) }

    // Now Playing. One repository for the screen, polling on a scope that dies
    // with the composition, so leaving Mode 7 stops the tick rather than
    // leaving a 2 Hz loop running behind a launcher the user has swiped away.
    val mediaScope = rememberCoroutineScope()
    val nowPlaying = remember { NowPlayingRepository(context) }
    LaunchedEffect(Unit) { nowPlaying.start(mediaScope) }
    val transport by nowPlaying.state.collectAsState()

    val contactsPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        contactsRequested = true
        if (granted) {
            contacts = readPhonebook(context)
        } else {
            toast = "CONTACTS PERMISSION DENIED"
        }
    }

    fun loadPhonebook() {
        if (!hasContactsPermission(context)) {
            // Flip the flag before launching. If the sheet is dismissed without
            // ever calling back -- which is what happens once the user has
            // ticked "don't ask again" -- the list must stop saying LOADING and
            // say it needs permission instead.
            contactsRequested = true
            contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS)
            return
        }
        contacts = readPhonebook(context)
        contactsRequested = true
    }

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
    // 12-hour clock with a matching AM/PM marker ? never 24-hour text next to "PM".
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
    val inCall = live?.state == Call.STATE_ACTIVE
    // Read once: simState is stable for as long as the app is running, and
    // the strip and the dial gate must never disagree about it.
    // Taken from telemetry rather than read once. TelemetryService re-samples
    // the SIM every second, so this follows a SIM being inserted or removed and
    // an operator changing, instead of freezing on whatever was true when the
    // screen was first composed.
    val simPresent = telemetryState.simPresent

    // Answering an incoming call is placing a call, and with no SIM the radio
    // cannot carry one, so the answer is refused. An emergency call is the only
    // exception: a no-SIM handset is still expected to connect for one, and
    // that is the entire purpose of the exception. Ordinary call-backs are NOT
    // exempt -- a return call is still a call, and still needs a SIM.
    val incomingMayAnswer = remember(connectedNumber, simPresent) {
        simPresent || isEmergencyNumber(context, connectedNumber)
    }

    /**
     * Answers an incoming call, or refuses when no SIM is present to carry it.
     *
     * Every answer path goes through here -- the centre key, both soft keys and
     * the green key -- so there is no route around the refusal.
     */
    fun answerOrRefuse() {
        if (incomingMayAnswer) {
            CallManager.answer()
        } else {
            haptics.thud()
            toast = "NO SIM - CANNOT ANSWER"
        }
    }

    val currencySymbol = remember(settings.callCurrencyIndex) {
        when (settings.callCurrencyIndex) {
            0 -> "?"; 1 -> "p"; 2 -> "$"; else -> "?"
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
        // Cancel just leaves the deck locked and as it was. Nothing to report,
        // now that there is no in-app code entry to annotate.
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            locked = false
            view = RazrView.STANDBY
            tonePlayer.playRazrChirp()
        }
    }

    fun placeCall(raw: String) {
        val clean = raw.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (clean.isEmpty()) return
        lastDialed = clean
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        // Only claim the in-call screen if the request was actually accepted.
        // Setting it unconditionally meant a thrown placeCall left a dead
        // "CONNECTED" screen behind even though the toast said it had failed.
        val placed = runCatching { tm.placeCall(Uri.fromParts("tel", clean, null), Bundle()) }
            .onFailure { toast = "CALL FAILED - CHECK SIM" }
            .isSuccess
        dialBuffer = ""
        if (placed) {
            view = RazrView.IN_CALL
            dialAttempt++
        }
    }

    fun startCall(
        number: String,
        perm: ActivityResultLauncher<Array<String>>,
        role: ActivityResultLauncher<Intent>,
    ) {
        haptics.thud()
        val target = number.ifBlank { lastDialed }
        if (target.isBlank()) { tonePlayer.playRazrChirp(); return }

        // Engineering codes go to the phone app, not to placeCall. Checked before
        // the no-SIM gate because `*#06#` on a SIM-less handset is precisely the
        // thing someone needs -- it is how you find out whether the SIM is seen
        // at all. Also checked before the permission gate: these are diagnostics,
        // not calls, and demanding CALL_PHONE to read a dump would be wrong.
        SecretCodes.detect(target)?.let { code ->
            dialBuffer = ""
            when {
                // Distinguish "we won't run this" from "nowhere to run it",
                // otherwise a blocked code reports as a missing dialer.
                SecretCodes.isBlocked(code) -> {
                    tonePlayer.playBusy()
                    toast = "REFUSED: ${(SecretCodes.blockReason(code) ?: code).uppercase(Locale.ROOT)}"
                }
                SecretCodes.send(context, code) -> {
                    tonePlayer.playRazrChirp()
                    // Set view directly rather than calling goHome(): local
                    // functions cannot be referenced before their declaration,
                    // and goHome is declared well below this one.
                    view = RazrView.STANDBY
                }
                else -> {
                    // No foreign dialer to hand it to. Saying so plainly beats
                    // implying the code ran.
                    tonePlayer.playBusy()
                    toast = "NO DIALER TO RUN CODES"
                }
            }
            return
        }

        // Without a SIM the radio cannot carry an ordinary call. Refuse here
        // rather than letting placeCall fail silently, but never block an
        // emergency number.
        if (!simPresent && !isEmergencyNumber(context, target)) {
            refusedNumber = target
            view = RazrView.NO_SIM
            tonePlayer.playBusy()
            return
        }
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

    // One timer string, reused by the panel and the cover so the two faces of
    // the handset never disagree about how long the call has been running.
    val callTimerText =
        String.format(Locale.ROOT, "%02d:%02d", callSeconds / 60, callSeconds % 60)

    // Drive the panel from real call state.
    LaunchedEffect(live?.state) {
        val l = live
        if (l == null) return@LaunchedEffect
        if (l.state == Call.STATE_DISCONNECTED) {
            tonePlayer.playBusy()
            toast = "ENDED - $callTimerText - CHARGED $totalLabel"
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
            // Some teardowns never surface DISCONNECTED at all: the call record
            // simply goes away. Without this branch the flags were cleared
            // without tallying, so a genuinely missed call was silently lost
            // and the cover never said so.
            live == null && wasRinging && !everAnswered -> {
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

    // Watchdog for an outgoing call the system never handed us.
    //
    // placeCall() succeeds as soon as TelecomManager accepts the request, but the
    // call can still be refused downstream -- no service, calls disabled, no
    // default dialer. In that case RazrInCallService never attaches, so
    // CallManager.call stays null, the DISCONNECTED branch above never runs, and
    // the deck used to sit on a fabricated CONNECTED screen indefinitely. Give
    // the radio a few seconds to produce a call, then fall back to standby.
    LaunchedEffect(dialAttempt) {
        if (dialAttempt == 0) return@LaunchedEffect
        delay(8000)
        if (CallManager.call.value == null && view == RazrView.IN_CALL) {
            tonePlayer.playBusy()
            toast = "CALL FAILED - NO SERVICE"
            dialBuffer = ""
            view = RazrView.STANDBY
        }
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
            // No screen lock configured, so there is nothing to confirm. Treat the
            // deck as unlocked rather than leaving it permanently dead behind a
            // key that no longer does anything.
            locked = false
            view = RazrView.STANDBY
            tonePlayer.playRazrChirp()
            return
        }
        runCatching { credentialLauncher.launch(intent) }
            .onFailure {
                // Device refused to show it (e.g. no credential set after all).
                locked = false
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
            6 -> { view = RazrView.ITUNES }
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
            208 -> { view = RazrView.SECRET_CODES; listIndex = 0 }
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
            // The in-app lock panel and the separate 4-digit RAZR unlock code are
            // both gone. There is exactly one way in: '*' hands off to the real
            // Android credential screen, where they type the PIN they already
            // use. A second code to forget, on a phone that already has a lock
            // screen, was strictly worse than deferring to the system's.
            if (ch == '*') {
                tonePlayer.playRazrChirp()
                requestSystemUnlock()
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

    /**
     * Runs the highlighted phonebook action.
     *
     * Call puts the number on the dial line rather than placing it immediately:
     * the stock handset always shows what it is about to dial, and it keeps SEND
     * as the deliberate second step.
     *
     * Message hands off to the system composer with the recipient pre-filled,
     * which needs no SEND_SMS grant. Copy writes the clipboard, which since API
     * 28 needs no permission for the focused app.
     */
    fun runContactAction() {
        val c = contacts.getOrNull(listIndex) ?: return
        when (CONTACT_ACTIONS[contactActionIndex.coerceIn(CONTACT_ACTIONS.indices)]) {
            ContactAction.CALL -> {
                dialBuffer = c.number
                view = RazrView.DIALING
                tonePlayer.playRazrChirp()
            }
            ContactAction.MESSAGE -> {
                tonePlayer.playRazrChirp()
                context.openSmsComposer(c.number)
                goHome()
            }
            ContactAction.COPY -> {
                context.copyToClipboard(c.number)
                toast = "COPIED: ${c.number}"
                tonePlayer.playRazrChirp()
                view = RazrView.PHONEBOOK
            }
            ContactAction.BACK -> {
                view = RazrView.PHONEBOOK
                tonePlayer.playRazrChirp()
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
            // UP from the top of the list goes back to Tools, as it does from
            // every other sub-page. Braces are required: a when branch is a
            // single expression and the chirp is a second statement.
            view == RazrView.SECRET_CODES -> {
                if (listIndex > 0) {
                    listIndex -= 1
                } else {
                    view = RazrView.TOOLS
                    subIndex = RAZR_TOOLS_MENU.indexOfFirst { it.id == 208 }.coerceAtLeast(0)
                }
                tonePlayer.playRazrChirp()
            }
            view == RazrView.MESSAGES -> if (subIndex > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.THEME -> if (subIndex > 0) { subIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.GAMES -> if (listIndex < RAZR_GAMES.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.APPS_LIST -> if (listIndex > 0) { listIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.INBOX -> if (listIndex > 0) { listIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.PHONEBOOK -> if (listIndex > 0) { listIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.PHONEBOOK_ACTIONS ->
                if (contactActionIndex > 0) { contactActionIndex -= 1; tonePlayer.playRazrChirp() }
            view == RazrView.IN_CALL -> { callVolume = (callVolume + 1).coerceAtMost(10); tonePlayer.playRazrChirp() }
            // UP on the player is "previous track", matching every other player
            // ever built, and deliberately not "back" — leaving is BACK's job.
            view == RazrView.ITUNES -> { tonePlayer.playRazrChirp(); nowPlaying.previous() }
        }
    }

    fun onDown() {
        if (view == RazrView.GAME) { currentGame?.onNav(Nav.DOWN); return }
        when {
            locked -> Unit
            // The real handset puts the Phonebook on the down key from
            // standby; it is not a shortcut for the call log.
            view == RazrView.STANDBY -> {
                view = RazrView.PHONEBOOK
                listIndex = 0
                contactsRequested = false
                loadPhonebook()
                tonePlayer.playRazrChirp()
            }
            view == RazrView.MAIN_MENU ->
                if (menuIndex <= RAZR_MAIN_MENU.size - 4) { menuIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.SETTINGS ->
                if (subIndex <= RAZR_SETTINGS_MENU.size - 4) { subIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.TOOLS ->
                if (subIndex <= RAZR_TOOLS_MENU.size - 4) { subIndex += 3; tonePlayer.playRazrChirp() }
            view == RazrView.SECRET_CODES ->
                if (listIndex < SecretCodes.reference.lastIndex) {
                    listIndex += 1
                    tonePlayer.playRazrChirp()
                }
            view == RazrView.MESSAGES -> if (subIndex < 2) { subIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.THEME ->
                if (subIndex < RazrFinish.entries.lastIndex) { subIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.APPS_LIST ->
                if (listIndex < apps.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.INBOX ->
                if (listIndex < messages.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.PHONEBOOK ->
                if (listIndex < contacts.lastIndex) { listIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.PHONEBOOK_ACTIONS ->
                if (contactActionIndex < CONTACT_ACTIONS.lastIndex) { contactActionIndex += 1; tonePlayer.playRazrChirp() }
            view == RazrView.IN_CALL -> { callVolume = (callVolume - 1).coerceAtLeast(1); tonePlayer.playRazrChirp() }
            view == RazrView.ITUNES -> { tonePlayer.playRazrChirp(); nowPlaying.next() }
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
            // LEFT and RIGHT scrub rather than skip. On a panel this size there
            // is no room for both meanings on one key pair, and mid-song the
            // thing you want is to move. Track skipping lives on UP/DOWN.
            view == RazrView.ITUNES -> {
                haptics.click()
                nowPlaying.nudge(-SEEK_STEP_MS)
                toast = "-${SEEK_STEP_MS / 1000}s"
            }
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
            view == RazrView.ITUNES -> {
                haptics.click()
                nowPlaying.nudge(SEEK_STEP_MS)
                toast = "+${SEEK_STEP_MS / 1000}s"
            }
        }
    }

    fun onCenter() {
        if (locked) { tonePlayer.playRazrChirp(); return }
        haptics.thud()
        when (view) {
            RazrView.STANDBY -> { view = RazrView.MAIN_MENU; menuIndex = 0; tonePlayer.playRazrChirp() }
            RazrView.DIALING -> startCall(dialBuffer, permLauncher, roleLauncher)
            // SELECT is play/pause on the player, as it is on every player.
            RazrView.ITUNES -> {
                tonePlayer.playDtmf(' ')
                nowPlaying.togglePlayPause()
            }
        // The stock phonebook does not call straight away; SELECT opens the
        // action sheet, which is the only way Call / Message / Copy are
        // reachable on a three-soft-key panel.
        RazrView.PHONEBOOK ->
            if (contacts.isNotEmpty()) {
                contactActionIndex = 0
                view = RazrView.PHONEBOOK_ACTIONS
                tonePlayer.playRazrChirp()
            }
        RazrView.PHONEBOOK_ACTIONS -> runContactAction()
        // Nothing to confirm on the notice; the centre key just gets rid of it.
        RazrView.NO_SIM -> goHome()
            RazrView.MAIN_MENU -> selectMenuItem(RAZR_MAIN_MENU[menuIndex.coerceIn(RAZR_MAIN_MENU.indices)])
            RazrView.SETTINGS -> selectSettingsItem(RAZR_SETTINGS_MENU[subIndex.coerceIn(RAZR_SETTINGS_MENU.indices)])
            RazrView.TOOLS -> selectToolsItem(RAZR_TOOLS_MENU[subIndex.coerceIn(RAZR_TOOLS_MENU.indices)])
            // SELECT stages the code in the dial buffer rather than running it.
            // The user then presses SEND, which hands it to a real dialer -- the
            // only component permitted to dispatch these.
            RazrView.SECRET_CODES -> {
                val entry = SecretCodes.reference[listIndex.coerceIn(SecretCodes.reference.indices)]
                if (entry.blocked) {
                    // Say why rather than silently doing nothing, so someone
                    // who searched for this learns the app is refusing on
                    // purpose.
                    tonePlayer.playBusy()
                    toast = "REFUSED: ${entry.label.uppercase(Locale.ROOT)}"
                } else {
                    dialBuffer = entry.code
                    view = RazrView.DIALING
                    tonePlayer.playRazrChirp()
                    toast = "PRESS SEND TO RUN ${entry.code}"
                }
            }
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
            RazrView.INCOMING -> answerOrRefuse()
            else -> tonePlayer.playRazrChirp()
        }
    }

    fun onSoftLeft() {
        tonePlayer.playRazrChirp()
        if (locked) return
        when (view) {
            RazrView.GAME, RazrView.GAMES -> { playingGameId = null; goHome() }
            // There were two STANDBY branches and the first won, so the caption
            // said INBOX while the key closed the flip and the INBOX arm below
            // was dead code. Closing the flap is the hinge's job and BACK's, so
            // the caption is the authority here.
            RazrView.STANDBY -> { view = RazrView.INBOX; listIndex = 0 }
            RazrView.IN_CALL -> CallManager.mute(!isMuted)
            RazrView.INCOMING -> CallManager.reject()
            RazrView.ITUNES -> nowPlaying.cycleRepeat()
            // The caption here is CLEAR and the screen says "Clear erases the
            // last digit", but there was no DIALING branch, so it fell through
            // to goHome() and threw away the whole number along with the screen.
            RazrView.DIALING ->
                if (dialBuffer.isNotEmpty()) {
                    dialBuffer = dialBuffer.dropLast(1)
                    tonePlayer.playRazrChirp()
                }
            else -> goHome()
        }
    }

    fun onSoftRight() {
        tonePlayer.playRazrChirp()
        if (locked) return
        when (view) {
            RazrView.STANDBY -> openCamera()
            RazrView.IN_CALL -> CallManager.speaker(!isSpeaker)
            RazrView.INCOMING -> answerOrRefuse()
            RazrView.DIALING -> { dialBuffer = ""; goHome() }
            RazrView.ITUNES -> nowPlaying.toggleShuffle()
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
        view == RazrView.NO_SIM -> "BACK"
        // Plain captions only. The left/right soft-key slots are a fixed 52dp, so
        // "REPEAT:ALL" was silently clipped to "REPEAT" on device. State is
        // carried by the button instead: it tints selection-blue when engaged
        // and repeat-one draws a distinct glyph.
        view == RazrView.ITUNES -> "REPEAT"
        view == RazrView.PHONEBOOK_ACTIONS -> "SELECT"
        else -> "BACK"
    }
    val softCenter = when {
        locked -> "MOTOROLA"
        view == RazrView.IN_CALL -> "HOLD"
        view == RazrView.INCOMING -> "ANSWER"
        view == RazrView.STANDBY -> "MENU"
        view == RazrView.DIALING -> "SEND"
        view == RazrView.ITUNES -> if (transport.playing) "PAUSE" else "PLAY"
        else -> "SELECT"
    }
    val softRight = when {
        locked -> ""
        view == RazrView.IN_CALL -> "SPKR"
        view == RazrView.INCOMING -> "ANSWER"
        view == RazrView.DIALING -> "CANCEL"
        view == RazrView.ITUNES -> "SHUFFLE"
        view == RazrView.STANDBY -> "CLOSE FLIP"
        else -> "BACK"
    }

    // The operator a 2G handset would show, not the modern consumer brand.
    // Keyed on the raw name so a carrier change re-resolves through the 2G
    // table. A bare remember{} here pinned the operator for the lifetime of the
    // process, which is wrong when the SIM can change underneath the app.
    val operatorName = remember(telemetryState.simOperatorName) {
        RazrOperators.resolveFor2G(telemetryState.simOperatorName)
    }

    val wallpaper = RazrWallpapers.all[
        settings.razrWallpaperIndex.coerceIn(RazrWallpapers.all.indices)
    ].res

    // ---- Chassis ---------------------------------------------------------------
    //
    // Geometry is derived once, up front, from the space actually available.
    // The panel is a true 176x220 and the deck sits below at a fixed ratio; if
    // the combined height would not fit, the whole handset shrinks. Nothing here
    // competes with weight(), which is what previously let the panel stretch
    // wide and short and drop the title bar off-screen.
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0D))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        val availW = maxWidth
        val availH = maxHeight
        val shellChrome = 34.dp
        val hingeH = 14.dp

        // The V3i closed is 98 x 63 x 13.99 mm, so it is a tall, narrow handset.
        // Scaling purely to available width looks right on a phone and wrong
        // everywhere else: on a tablet it spans 700dp and reads as a toy, and in
        // landscape it collapses to an unusable sliver because width stops being
        // the constraint. So cap it at a believable handset size, and let the
        // physical proportions decide from there.
        val maxW = minOf(availW, 400.dp)
        val panelH = maxW * (220f / 176f)
        val keypadNaturalH = maxW * 1.02f
        val naturalH = shellChrome + panelH + hingeH + keypadNaturalH

        // Shrink to fit whatever room is left, and grow to fill it when there is
        // plenty. Never both: the smaller of "fits vertically" and "fills width"
        // has to win, or on a tall narrow screen it overflows and the keypad
        // falls off the bottom.
        val fitByHeight = if (availH > 0.dp) (availH / naturalH).coerceAtMost(1f) else 1f
        val fitByWidth = if (availW > 0.dp) (availW / maxW).coerceAtLeast(1f) else 1f
        val fit = minOf(fitByHeight, fitByWidth)

        // A squat screen (landscape phone, small window) cannot show the whole
        // handset at a usable size. Rather than render something too small to
        // tap, keep a floor and let the shell clip gracefully -- the deck is the
        // part that must stay reachable.
        val minW = 240.dp
        val fittedWidth = (maxW * fit).coerceIn(minW, availW)
        val keypadHeight = fittedWidth * 1.02f

        Column(
            modifier = Modifier.width(availW),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ---- Clamshell -----------------------------------------------------
            // The upper half and hinge only occupy the layout while the flip is
            // opening or is open. At rest closed they are removed outright --
            // otherwise two invisible boxes still consume height and push the
            // lower half off-screen, which is what left the closed state black.
            if (flip > 0.002f) {
                Box(
                    Modifier
                        .width(fittedWidth)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            rotationX = (1f - flip) * -104f
                            cameraDistance = 16f * density
                            alpha = flip.coerceIn(0f, 1f)
                        },
                ) {
                    RazrUpperShell(palette, panelWidth = fittedWidth) {
                            RazrPixelScreen(palette = palette, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxSize()) {
                        RazrStatusStrip(palette) {
                            RazrStatusGlyphs(
                                palette = palette,
                                operatorName = operatorName,
                                noSim = !simPresent,
                                // Real bars, not a decorative constant: with no
                                // service they read as empty, which is what
                                // makes the NO SVC caption legible.
                                noService = !telemetryState.hasService,
                                signalBars = telemetryState.signalBars.coerceIn(0, 4),
                                batteryPercent = telemetryState.batteryPct.coerceAtLeast(0),
                                charging = telemetryState.charging,
                                unreadMessages = unread,
                                ringStyleName = RAZR_RING_STYLES[
                                    settings.razrRingStyleIndex.coerceIn(RAZR_RING_STYLES.indices)
                                ].name,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        // No title bar on the home screen. The stock standby
                        // screen does not have one -- it is wallpaper, the clock
                        // and the status strip -- and here the title only ever
                        // read "RAZR V3i", which is the model name rather than
                        // anything the screen is actually about. Every other
                        // view keeps its bar, because there the caption names
                        // the screen.
                        if (view != RazrView.STANDBY) {
                            RazrTitleBar(
                                palette,
                                titleFor(view, isRinging, currentGame?.title ?: "Games"),
                            )
                        }

                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            when {
                                isRinging -> RazrIncomingCallScreen(palette, connectedNumber, canAnswer = incomingMayAnswer)
                                // No `locked` arm. While locked the deck shows whatever
                                // it was on, every key is inert, and '*' opens the system
                                // credential screen. Suppressing the panel means a
                                // false-positive isKeyguardLocked() leaves a dead but
                                // honest deck rather than a screen asking for a code
                                // that can no longer be entered.
                                else -> ActiveView(
                                    view = view,
                                    palette = palette,
                                    transport = transport,
                                    nowPlaying = nowPlaying,
                                    settings = settings,
                                    contacts = contacts,
                                    contactsRequested = contactsRequested,
                                    contactActionIndex = contactActionIndex,
                                    contactsBlocked = !hasContactsPermission(context),
                                    refusedNumber = refusedNumber,
                                    noSim = !simPresent,
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
                                        // A DISCONNECTED call still lands here on its
                                        // way off the screen, so it must never be
                                        // labelled CONNECTED.
                                        live?.state == Call.STATE_DISCONNECTED -> "CALL ENDED"
                                        // No call object at all yet means TelecomManager
                                        // has accepted the request but the radio has not
                                        // produced anything. Saying CONNECTED here is how
                                        // a dead call managed to look like a live one.
                                        live == null -> "CALLING..."
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
                }

                Box(Modifier.graphicsLayer { alpha = flip }) {
                    RazrHinge(palette, Modifier.width(fittedWidth), open = flapOpen) {
                        setFlap(!flapOpen)
                    }
                }
            }

            // ---- Lower half ----------------------------------------------------
            // On the real handset the keypad and the cover display sit on
            // opposite faces of the same slab, so they share one box and cross
            // fade as the flip turns over rather than swapping boxes.
            // Sized by whichever face is showing: the keypad declares an explicit
            // height, the cover wraps its own. A fixed height here clipped the
            // medallion and grille off the closed shell, because the cover face
            // is a little taller than the deck.
            Box(
                Modifier
                    .width(fittedWidth)
                    .wrapContentSize(),
            ) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { alpha = flip.coerceIn(0f, 1f) },
                ) {
                        RazrKeypad(
                    palette = palette,
                    modifier = Modifier.width(fittedWidth).height(keypadHeight),
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
                            isRinging -> { haptics.thud(); answerOrRefuse() }
                            view == RazrView.IN_CALL -> CallManager.hold(!isOnHold)
                            // While locked there is no in-app unlock screen to
                            // navigate to, so offer the system credential prompt
                            // directly. Previously these arms switched the view to
                            // a code entry that no longer exists.
                            locked -> { tonePlayer.playRazrChirp(); requestSystemUnlock() }
                            else -> startCall(dialBuffer, permLauncher, roleLauncher)
                        }
                    },
                    onEnd = {
                        when {
                            isRinging -> { haptics.thud(); CallManager.reject() }
                            view == RazrView.IN_CALL -> { CallManager.hangUp(); goHome() }
                            locked -> { tonePlayer.playRazrChirp(); requestSystemUnlock() }
                            else -> { tonePlayer.playBusy(); dialBuffer = ""; goHome() }
                        }
                    },
                    onGlobe = {
                        if (locked) { tonePlayer.playRazrChirp(); requestSystemUnlock() } else openBrowser()
                    },
                    onEnvelope = {
                        tonePlayer.playRazrChirp()
                        if (locked) { requestSystemUnlock() }
                        else { view = RazrView.INBOX; listIndex = 0 }
                    },
                    onVoice = {
                        tonePlayer.playRazrChirp()
                        toast = "VOICE COMMANDS"
                        goHome()
                    },
                    onKey = { handleKey(it) },
                )
                }

                Box(
                    Modifier
                        .align(Alignment.Center)
                        .wrapContentSize()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            rotationX = flip * 104f
                            cameraDistance = 16f * density
                            alpha = (1f - flip).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                        RazrCoverShell(
                    palette = palette,
                    shellWidth = fittedWidth,
                    modifier = Modifier.fillMaxWidth(),
                    onOpen = { setFlap(true) },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        when {
                        // A call outranks the lock prompt and the wallpaper: the
                        // cover has to say who is calling before you decide
                        // whether to bother opening the flip.
                        isRinging -> RazrCoverIncomingCall(palette, connectedNumber, canAnswer = incomingMayAnswer)
                        inCall -> RazrCoverInCall(
                            palette = palette,
                            number = connectedNumber,
                            timer = callTimerText,
                            muted = isMuted,
                            onHold = isOnHold,
                            speaker = isSpeaker,
                        )
                        locked -> RazrCoverLocked(
                                palette = palette,
                                onUnlock = {
                                    haptics.thud()
                                    requestSystemUnlock()
                                },
                            )

                            else -> RazrCoverPanel(
                                palette = palette,
                                operatorName = operatorName,
                                noSim = !simPresent,
                                noService = !telemetryState.hasService,
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

}
/** Every internal-panel view, driven purely by the [RazrView] state. */
@Composable
private fun ActiveView(
    view: RazrView,
    palette: RazrPalette,
    transport: Transport,
    nowPlaying: NowPlayingRepository,
    settings: Settings,
    contacts: List<RazrContact>,
    contactsRequested: Boolean,
    contactActionIndex: Int,
    contactsBlocked: Boolean,
    refusedNumber: String,
    noSim: Boolean,
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

        RazrView.SECRET_CODES -> RazrSecretCodeList(palette, listIndex)

        RazrView.ITUNES -> ItunesNowPlaying(
            transport = transport,
            onScrubFraction = { f -> nowPlaying.seekTo((f * transport.durationMs).toLong()) },
            onPlayPause = { nowPlaying.togglePlayPause() },
            onPrevious = { nowPlaying.previous() },
            onNext = { nowPlaying.next() },
            onCycleRepeat = { nowPlaying.cycleRepeat() },
            onToggleShuffle = { nowPlaying.toggleShuffle() },
        )

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

        // Reached only with a valid index: both the entry point and every
        // action guard on it, and BACK returns to the list. Rendering must not
        // assign state, so there is no recovery branch here.
        RazrView.NO_SIM -> RazrNoSimNotice(palette, refusedNumber)

        RazrView.PHONEBOOK_ACTIONS -> contacts.getOrNull(listIndex)?.let { c ->
            RazrActionSheet(
                palette = palette,
                heading = c.name,
                options = CONTACT_ACTIONS,
                selectedIndex = contactActionIndex,
                label = { a: ContactAction -> a.label },
            )
        }

        RazrView.PHONEBOOK -> RazrListScreen(
            palette = palette,
            entries = contacts,
            selectedIndex = listIndex,
            emptyText = when {
                contacts.isNotEmpty() -> ""
                !contactsRequested -> "LOADING…"
                contactsBlocked -> "CONTACTS ACCESS NEEDED"
                else -> "NO CONTACTS"
            },
            primary = { c: RazrContact -> c.name },
            // A contact stored with no display name falls back to the digits,
            // so do not print the same string twice on the row.
            secondary = { c: RazrContact -> c.number.takeIf { it != c.name } ?: "" },
        )

        RazrView.APPS_LIST -> RazrListScreen(
            palette = palette,
            entries = apps,
            selectedIndex = listIndex,
            emptyText = "NO APPS FOUND",
            primary = { app: AppInfo -> app.label },
        )

        RazrView.CALLS -> RazrListScreen(
            palette = palette,
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

/**
 * Opens the system SMS composer with the recipient pre-filled.
 *
 * Deliberately an ACTION_SENDTO handoff rather than sending via SmsManager:
 * it needs no SEND_SMS permission, and it is what the stock handset's
 * "Send Text" menu entry effectively did.
 */
private fun Context.openSmsComposer(number: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(number)}")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { startActivity(intent) }
}

/** Copies to the clipboard. No permission needed for the focused app. */
private fun Context.copyToClipboard(label: String) {
    runCatching {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, label))
    }
}

/**
 * The SIM's own list of emergency numbers, where the platform exposes one.
 *
 * On API 29+ `TelephonyManager.getEmergencyNumberList()` reports the numbers
 * the SIM and the registered network actually honour, which beats any table
 * we ship because it reflects the country the handset is really in right now.
 * Empty on older releases and on any vendor implementation that throws.
 */
private fun platformEmergencyNumbers(context: Context): Set<String> = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .emergencyNumberList.orEmpty()
            .values
            .flatten()
            .map { e: android.telephony.emergency.EmergencyNumber -> e.number }
            .filter { n: String -> n.isNotBlank() }
            .toSet()
    } else {
        emptySet()
    }
} catch (t: Throwable) {
    emptySet()
}

/**
 * Whether [raw] must be allowed to place with no SIM present.
 *
 * Checks the platform list, then the country's table, then the numbers every
 * GSM handset carries. Someone dialling 133 in Chile or 100 in India is
 * dialling the police, so our own no-SIM gate must not be what stops them.
 */
private fun isEmergencyNumber(context: Context, raw: String): Boolean {
    val iso = try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.simCountryIso?.takeIf { it.isNotBlank() } ?: tm.networkCountryIso
    } catch (t: Throwable) {
        null
    }
    return RazrEmergencyNumbers.isEmergency(raw, iso, platformEmergencyNumbers(context))
}

/**
 * One phonebook row. [number] is kept as dialled rather than formatted, because
 * that is what the dial line needs.
 */
data class RazrContact(val name: String, val number: String)

/** What the handset offers for a highlighted phonebook entry. */
private enum class ContactAction(val label: String) {
    CALL("Call"),
    MESSAGE("Send Message"),
    COPY("Copy Number"),
    BACK("Back"),
}

/**
 * How far LEFT/RIGHT nudge the playhead.
 *
 * Five seconds. Ten is too coarse to land on a line change, two is too fine to
 * cross a chorus without holding the key, and the deck has no repeat-rate
 * acceleration to fall back on.
 */
private const val SEEK_STEP_MS = 5_000L

private val CONTACT_ACTIONS = listOf(
    ContactAction.CALL,
    ContactAction.MESSAGE,
    ContactAction.COPY,
    ContactAction.BACK,
)

private fun hasContactsPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Reads the device phonebook.
 *
 * Queries only the phone rows so the projection stays cheap on a 176x220
 * screen that will never show more than the name and the number. Duplicates
 * are collapsed on the dialled digits, because a contact with a mobile, a home
 * and a work line would otherwise occupy three adjacent identical-looking rows.
 *
 * Returns an empty list rather than throwing when the provider is unavailable,
 * so the list screen can show its own empty state.
 */
private fun readPhonebook(context: Context): List<RazrContact> = try {
    val out = LinkedHashMap<String, RazrContact>()
    context.contentResolver.query(
        Phone.CONTENT_URI,
        arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER),
        null,
        null,
        "${Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
    )?.use { c ->
        val nameIdx = c.getColumnIndex(Phone.DISPLAY_NAME)
        val numIdx = c.getColumnIndex(Phone.NUMBER)
        if (nameIdx >= 0 && numIdx >= 0) {
            while (c.moveToNext()) {
                val digits = (c.getString(numIdx) ?: "").filter { it.isDigit() || it == '+' }
                if (digits.isBlank()) continue
                val name = c.getString(nameIdx)?.trim().orEmpty().ifBlank { digits }
                // Key on name+digits so one contact's several lines all survive.
                out["$name\u0000$digits"] = RazrContact(name, digits)
            }
        }
    }
    out.values.toList()
} catch (t: Throwable) {
    // SecurityException without the grant, or a vendor provider that misbehaves.
    emptyList()
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
    view == RazrView.PHONEBOOK -> "Phonebook"
    view == RazrView.PHONEBOOK_ACTIONS -> "Phonebook"
    view == RazrView.NO_SIM -> "No SIM"
    view == RazrView.APPS_LIST -> "Games & Apps"
    view == RazrView.INBOX -> "Message Inbox"
    view == RazrView.RINGTONES -> "Ring Styles"
    view == RazrView.THEME -> "Themes"
    view == RazrView.TOOLS -> "Tools"
    view == RazrView.ITUNES -> "Now Playing"
    view == RazrView.SECRET_CODES -> "Secret Codes"
    view == RazrView.IN_CALL -> "In Call"
    view == RazrView.DIALING -> "Dialing"
    view == RazrView.MESSAGES -> "Messages"
    view == RazrView.ABOUT -> "Phone Status"
    view == RazrView.CALCULATOR -> "Calculator"
    view == RazrView.GAMES -> "Games"
    view == RazrView.GAME -> currentGameTitle
    view == RazrView.MAIN_MENU -> "Main Menu"
    
    view == RazrView.NOTEPAD -> "Notepad"
    view == RazrView.DATEBOOK -> "Datebook"
    view == RazrView.VOICE_DIAL -> "Voice Dial"
    view == RazrView.PHOTO -> "My Pictures"
    view == RazrView.SETTINGS -> "Settings"
    view == RazrView.CALL_TIMES -> "Call Times"
    else -> "RAZR V3i"
}


