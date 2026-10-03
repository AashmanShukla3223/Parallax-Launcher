package com.parallax.parallaxlauncher.ui.modes.razr

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings as AndroidSettings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.parallax.parallaxlauncher.core.telecom.CallManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.settings.Settings
import com.parallax.parallaxlauncher.core.telecom.TonePlayer
import com.parallax.parallaxlauncher.core.telecom.VintageCarrierResolver
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrKeypad
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrMenuGrid
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrStandbyScreen
import com.parallax.parallaxlauncher.ui.modes.razr.components.RazrStatusBar
import com.parallax.parallaxlauncher.ui.modes.razr.components.SYNERGY_MENU_ITEMS
import kotlinx.coroutines.delay
import java.util.Locale

enum class RazrViewState {
    STANDBY,
    MAIN_MENU,
    APPS_LIST,
    DIALING,
    IN_CALL
}

@Composable
fun RazrV3iScreen(
    repo: AppsRepository,
    telemetry: TelemetryService,
    haptics: HapticEngine,
    settings: Settings,
) {
    val context = LocalContext.current
    val apps by repo.apps.collectAsState()
    val telemetryState by telemetry.state.collectAsState()

    val carrierName = remember { VintageCarrierResolver.resolve(context) }
    val tonePlayer = remember { TonePlayer() }
    DisposableEffect(Unit) {
        onDispose { tonePlayer.release() }
    }

    var viewState by rememberSaveable { mutableStateOf(RazrViewState.STANDBY) }
    var menuIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAppIndex by rememberSaveable { mutableIntStateOf(0) }
    var dialedBuffer by rememberSaveable { mutableStateOf("") }
    var listTitle by rememberSaveable { mutableStateOf("Games & Apps") }

    // ---- Real telephony state (fed by RazrInCallService via CallManager) ----
    val live by CallManager.call.collectAsState()
    val connectedNumber = live?.number.orEmpty()
    val isMuted = live?.muted ?: false
    val isOnHold = live?.onHold ?: false
    val isSpeaker = live?.speaker ?: false
    val isIncomingRinging = live?.state == Call.STATE_RINGING
    val callStatusText = when (live?.state) {
        Call.STATE_RINGING -> "INCOMING CALL"
        Call.STATE_DIALING, Call.STATE_CONNECTING, Call.STATE_NEW -> "CALLING..."
        Call.STATE_HOLDING -> "CALL ON HOLD"
        Call.STATE_ACTIVE -> "CONNECTED"
        else -> "CONNECTED"
    }
    var callSeconds by rememberSaveable { mutableIntStateOf(0) }
    var callVolume by rememberSaveable { mutableIntStateOf(7) } // 1..10
    var lastCallSummary by remember { mutableStateOf<String?>(null) }
    var lastDialed by rememberSaveable { mutableStateOf("") }
    var pendingNumber by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedAppIndex) {
        if (apps.isNotEmpty()) {
            listState.animateScrollToItem(selectedAppIndex.coerceIn(0, apps.lastIndex))
        }
    }

    val currencySymbol = remember(settings.callCurrencyIndex) {
        when (settings.callCurrencyIndex) {
            0 -> "₹"
            1 -> "p"
            2 -> "$"
            else -> "¢"
        }
    }

    val callCost = remember(callSeconds, settings.callTariffRate) {
        (callSeconds / 60.0) * settings.callTariffRate
    }

    fun placeRealCall(number: String) {
        val clean = number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (clean.isEmpty()) return
        lastDialed = clean
        lastCallSummary = null
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        runCatching {
            tm.placeCall(Uri.fromParts("tel", clean, null), Bundle())
        }.onFailure {
            lastCallSummary = "CALL FAILED • CHECK SIM / PERMISSIONS"
        }
        dialedBuffer = ""
        viewState = RazrViewState.IN_CALL
    }

    fun requestDialerRole(launch: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_DIALER) && !rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            }
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pendingNumber?.let { placeRealCall(it); pendingNumber = null }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res[Manifest.permission.CALL_PHONE] == true) {
            requestDialerRole { roleLauncher.launch(it) }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                pendingNumber?.let { placeRealCall(it); pendingNumber = null }
            }
        } else {
            pendingNumber = null
            lastCallSummary = "CALL PERMISSION DENIED"
        }
    }

    fun hasCallPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun isDefaultDialer(): Boolean {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return tm.defaultDialerPackage == context.packageName
    }

    fun startCall(number: String) {
        haptics.thud()
        val target = number.ifBlank { lastDialed }
        if (target.isBlank()) { tonePlayer.playRazrChirp(); return }
        if (!hasCallPermission()) {
            pendingNumber = target
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                )
            )
            return
        }
        if (!isDefaultDialer() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pendingNumber = target
            requestDialerRole { roleLauncher.launch(it) }
            // If the role dialog isn't shown (already decided), fall through to place anyway.
            val rm = context.getSystemService(RoleManager::class.java)
            if (!rm.isRoleAvailable(RoleManager.ROLE_DIALER)) { pendingNumber = null; placeRealCall(target) }
            return
        }
        placeRealCall(target)
    }

    fun endCall() {
        haptics.thud()
        if (isIncomingRinging) CallManager.reject() else CallManager.hangUp()
    }

    // Ask for permissions + Dialer role when Mode 7 opens so incoming calls work immediately.
    LaunchedEffect(Unit) {
        if (!hasCallPermission()) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                )
            )
        } else if (!isDefaultDialer()) {
            requestDialerRole { roleLauncher.launch(it) }
        }
    }

    // Talk-time clock derived from the system's real connect timestamp.
    LaunchedEffect(live?.connectTimeMillis, live?.state) {
        val l = live ?: return@LaunchedEffect
        if (l.connectTimeMillis > 0L && l.state != Call.STATE_DISCONNECTED) {
            while (true) {
                callSeconds = ((System.currentTimeMillis() - l.connectTimeMillis) / 1000L).toInt().coerceAtLeast(0)
                delay(500)
            }
        } else if (l.state == Call.STATE_RINGING || l.state == Call.STATE_DIALING || l.state == Call.STATE_CONNECTING) {
            callSeconds = 0
        }
    }

    // Drive the screen from real call state (incoming calls pop the call screen automatically).
    LaunchedEffect(live?.state) {
        val l = live
        if (l == null) return@LaunchedEffect
        if (l.state == Call.STATE_DISCONNECTED) {
            tonePlayer.playBusy()
            val dur = String.format(Locale.ROOT, "%02d:%02d", callSeconds / 60, callSeconds % 60)
            val costStr = String.format(Locale.ROOT, "%.2f", callCost)
            lastCallSummary = "ENDED • DURATION: $dur • CHARGED: $currencySymbol$costStr"
            CallManager.clearFinished()
            dialedBuffer = ""
            viewState = RazrViewState.STANDBY
        } else {
            viewState = RazrViewState.IN_CALL
        }
    }

    // Map the 1..10 earpiece volume onto the real voice-call stream.
    LaunchedEffect(callVolume) {
        runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (callVolume * max / 10).coerceAtLeast(1), 0)
        }
    }

    fun openMessages() {
        haptics.click()
        tonePlayer.playRazrChirp()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MESSAGING)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("sms:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun openContacts() {
        haptics.click()
        tonePlayer.playRazrChirp()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CONTACTS)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun openRecentCalls() {
        haptics.click()
        tonePlayer.playRazrChirp()
        val intent = Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }.onFailure {
            tonePlayer.playRazrChirp()
        }
    }

    fun openCamera() {
        haptics.thud()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun handleDialKey(char: Char) {
        haptics.click()
        if (viewState == RazrViewState.IN_CALL) {
            // In-call DTMF: sent to the far end through the real call
            tonePlayer.playDtmf(char)
            if (char != 'C') CallManager.dtmf(char)
            return
        }

        if (char == 'C') {
            tonePlayer.playRazrChirp()
            if (dialedBuffer.isNotEmpty()) {
                dialedBuffer = dialedBuffer.dropLast(1)
                if (dialedBuffer.isEmpty() && viewState == RazrViewState.DIALING) {
                    viewState = RazrViewState.STANDBY
                }
            }
        } else {
            tonePlayer.playDtmf(char)
            dialedBuffer += char
            viewState = RazrViewState.DIALING
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06090F))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Clamshell: Motorola Internal Color Screen (Strict non-touch display)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF03070E))
                .border(3.dp, Color(0xFF2C394E), RoundedCornerShape(12.dp))
        ) {
            // 1. Vintage 2G Status Bar
            RazrStatusBar(
                carrierName = carrierName,
                batteryPercent = telemetryState.batteryPct.coerceAtLeast(0)
            )

            // 2. Active Screen View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(6.dp)
            ) {
                when (viewState) {
                    RazrViewState.STANDBY -> {
                        Box(Modifier.fillMaxSize()) {
                            RazrStandbyScreen()
                            if (lastCallSummary != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xEE00264D))
                                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = lastCallSummary!!,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        }
                    }

                    RazrViewState.MAIN_MENU -> {
                        RazrMenuGrid(
                            selectedIndex = menuIndex
                        )
                    }

                    RazrViewState.APPS_LIST -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF071221), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E3A60), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF003866), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = listTitle.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = "[▲/▼ Scroll • OK Open]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF88AACC)
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(apps) { idx, app ->
                                    val isSelected = (idx == selectedAppIndex)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color(0xFF003866) else Color(0xFF0E1E34))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E3A60),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = app.label,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFFCCE0FF)
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "► [OPEN]",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00E676)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    RazrViewState.DIALING -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF071221), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E3A60), RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DIALING...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )

                            Text(
                                text = dialedBuffer,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PRESS [CALL] TO CONNECT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "[CLEAR] Erase  •  [END] Cancel",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF88AACC)
                                )
                            }
                        }
                    }

                    RazrViewState.IN_CALL -> {
                        val mm = callSeconds / 60
                        val ss = callSeconds % 60
                        val timerStr = String.format(Locale.ROOT, "%02d:%02d", mm, ss)
                        val costStr = String.format(Locale.ROOT, "%.2f", callCost)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF05101E))
                                .border(2.dp, Color(0xFF1E3A60), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Active Call Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF002E54), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = callStatusText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOnHold) Color(0xFFFFB300) else Color(0xFF00E676)
                                )
                                Text(
                                    text = "VOL $callVolume/10",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }

                            // Center Call Information
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = connectedNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = timerStr,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOnHold) Color(0xFFFFB300) else Color(0xFF00E5FF),
                                    letterSpacing = 2.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                // Real-Time Billing Accumulator
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0B1F38))
                                        .border(1.dp, Color(0xFF1C4273), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RATE: $currencySymbol${settings.callTariffRate}/min",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF88AACC)
                                    )
                                    Text(
                                        text = "COST: $currencySymbol$costStr",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00E676)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                // Live Status Badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isMuted) {
                                        Text(
                                            text = "[MIC MUTED]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF5252)
                                        )
                                    }
                                    if (isOnHold) {
                                        Text(
                                            text = "[HOLD]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB300)
                                        )
                                    }
                                    if (isSpeaker) {
                                        Text(
                                            text = "[SPEAKER ON]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                }
                            }

                            // Bottom Clamshell Softkeys Visual Hints
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF071221), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF1E3A60), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isMuted) "[ Unmute ]" else "[ Mute ]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = if (isOnHold) "[ Unhold ]" else "[ Hold ]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isSpeaker) "[ Earpiece ]" else "[ Speaker ]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Lower Clamshell: Chemically Etched Nickel Keypad with Physical D-Pad & 0 + Key
        RazrKeypad(
            modifier = Modifier.fillMaxWidth(),
            haptics = haptics,
            onUp = {
                when (viewState) {
                    RazrViewState.IN_CALL -> {
                        callVolume = (callVolume + 1).coerceAtMost(10)
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.STANDBY -> {
                        openContacts()
                    }
                    RazrViewState.MAIN_MENU -> {
                        if (menuIndex >= 3) {
                            menuIndex -= 3
                            tonePlayer.playRazrChirp()
                        }
                    }
                    RazrViewState.APPS_LIST -> {
                        if (selectedAppIndex > 0) {
                            selectedAppIndex--
                            tonePlayer.playRazrChirp()
                        }
                    }
                    RazrViewState.DIALING -> {}
                }
            },
            onDown = {
                when (viewState) {
                    RazrViewState.IN_CALL -> {
                        callVolume = (callVolume - 1).coerceAtLeast(1)
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.STANDBY -> {
                        openRecentCalls()
                    }
                    RazrViewState.MAIN_MENU -> {
                        if (menuIndex <= 5) {
                            menuIndex += 3
                            tonePlayer.playRazrChirp()
                        }
                    }
                    RazrViewState.APPS_LIST -> {
                        if (selectedAppIndex < apps.lastIndex) {
                            selectedAppIndex++
                            tonePlayer.playRazrChirp()
                        }
                    }
                    RazrViewState.DIALING -> {}
                }
            },
            onLeft = {
                when (viewState) {
                    RazrViewState.STANDBY -> {
                        openMessages()
                    }
                    RazrViewState.MAIN_MENU -> {
                        if (menuIndex % 3 > 0) {
                            menuIndex--
                            tonePlayer.playRazrChirp()
                        }
                    }
                    else -> {}
                }
            },
            onRight = {
                when (viewState) {
                    RazrViewState.STANDBY -> {
                        openCamera()
                    }
                    RazrViewState.MAIN_MENU -> {
                        if (menuIndex % 3 < 2) {
                            menuIndex++
                            tonePlayer.playRazrChirp()
                        }
                    }
                    else -> {}
                }
            },
            onCenter = {
                when (viewState) {
                    RazrViewState.IN_CALL -> {
                        CallManager.hold(!isOnHold)
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.STANDBY -> {
                        menuIndex = 0
                        viewState = RazrViewState.MAIN_MENU
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.MAIN_MENU -> {
                        val item = SYNERGY_MENU_ITEMS[menuIndex]
                        when (item.id) {
                            1 -> openContacts()
                            2 -> openRecentCalls()
                            3 -> openMessages()
                            4 -> {
                                listTitle = "Games & Apps"
                                selectedAppIndex = 0
                                viewState = RazrViewState.APPS_LIST
                                tonePlayer.playRazrChirp()
                            }
                            5 -> {
                                val gallery = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                runCatching { context.startActivity(gallery) }
                            }
                            6 -> {
                                val browser = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                runCatching { context.startActivity(browser) }
                            }
                            7 -> {
                                val clock = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                runCatching { context.startActivity(clock) }
                            }
                            8 -> {
                                val settingsIntent = Intent(AndroidSettings.ACTION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                runCatching { context.startActivity(settingsIntent) }
                            }
                            9 -> openCamera()
                            else -> {
                                listTitle = item.title
                                selectedAppIndex = 0
                                viewState = RazrViewState.APPS_LIST
                                tonePlayer.playRazrChirp()
                            }
                        }
                    }
                    RazrViewState.APPS_LIST -> {
                        apps.getOrNull(selectedAppIndex)?.let {
                            tonePlayer.playRazrChirp()
                            repo.launch(it)
                        }
                    }
                    RazrViewState.DIALING -> {
                        startCall(dialedBuffer)
                    }
                }
            },
            onSoftLeft = {
                when (viewState) {
                    RazrViewState.IN_CALL -> {
                        CallManager.mute(!isMuted)
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.STANDBY -> {
                        openMessages()
                    }
                    RazrViewState.MAIN_MENU -> {
                        val item = SYNERGY_MENU_ITEMS[menuIndex]
                        when (item.id) {
                            1 -> openContacts()
                            2 -> openRecentCalls()
                            3 -> openMessages()
                            4 -> {
                                listTitle = "Games & Apps"
                                selectedAppIndex = 0
                                viewState = RazrViewState.APPS_LIST
                                tonePlayer.playRazrChirp()
                            }
                            9 -> openCamera()
                            else -> {
                                listTitle = item.title
                                selectedAppIndex = 0
                                viewState = RazrViewState.APPS_LIST
                                tonePlayer.playRazrChirp()
                            }
                        }
                    }
                    RazrViewState.APPS_LIST -> {
                        apps.getOrNull(selectedAppIndex)?.let {
                            tonePlayer.playRazrChirp()
                            repo.launch(it)
                        }
                    }
                    RazrViewState.DIALING -> {
                        startCall(dialedBuffer)
                    }
                }
            },
            onSoftRight = {
                when (viewState) {
                    RazrViewState.IN_CALL -> {
                        CallManager.speaker(!isSpeaker)
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.STANDBY -> {
                        openContacts()
                    }
                    RazrViewState.MAIN_MENU -> {
                        viewState = RazrViewState.STANDBY
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.APPS_LIST -> {
                        viewState = RazrViewState.STANDBY
                        tonePlayer.playRazrChirp()
                    }
                    RazrViewState.DIALING -> {
                        handleDialKey('C')
                    }
                }
            },
            onCall = {
                when {
                    isIncomingRinging -> { haptics.thud(); CallManager.answer() }
                    viewState == RazrViewState.IN_CALL -> {
                        CallManager.hold(!isOnHold)
                        tonePlayer.playRazrChirp()
                    }
                    else -> startCall(dialedBuffer)
                }
            },
            onEnd = {
                if (viewState == RazrViewState.IN_CALL) {
                    endCall()
                } else {
                    tonePlayer.playBusy()
                    haptics.thud()
                    dialedBuffer = ""
                    viewState = RazrViewState.STANDBY
                }
            },
            onKey = ::handleDialKey,
            onKeyLongPress = { char ->
                handleDialKey(char)
            }
        )
    }
}
