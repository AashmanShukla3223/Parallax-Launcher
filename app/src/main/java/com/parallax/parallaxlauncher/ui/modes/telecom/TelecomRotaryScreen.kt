package com.parallax.parallaxlauncher.ui.modes.telecom

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.telecom.AppContact
import com.parallax.parallaxlauncher.core.telecom.TelecomRepository
import com.parallax.parallaxlauncher.core.telecom.TonePlayer
import com.parallax.parallaxlauncher.ui.modes.telecom.components.NixieDisplay
import com.parallax.parallaxlauncher.ui.modes.telecom.components.RotaryDial
import com.parallax.parallaxlauncher.ui.modes.telecom.components.YellowPagesDirectory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelecomRotaryScreen(
    repo: AppsRepository,
    haptics: HapticEngine,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apps by repo.apps.collectAsState()

    val telecomRepo = remember { TelecomRepository(context) }
    val tonePlayer = remember { TonePlayer() }
    DisposableEffect(Unit) {
        onDispose { tonePlayer.release() }
    }

    val contacts = remember(apps) {
        apps.mapIndexed { index, app ->
            telecomRepo.getOrAssignContact(app, index)
        }
    }

    var dialedBuffer by remember { mutableStateOf("") }
    var matchedContact by remember { mutableStateOf<AppContact?>(null) }
    var isConnecting by remember { mutableStateOf(false) }
    var showYellowPages by remember { mutableStateOf(false) }

    fun updateMatched(buffer: String) {
        if (buffer.isEmpty()) {
            matchedContact = null
            return
        }
        // Match by extension first (e.g. "001"), or full/suffix raw digits
        matchedContact = contacts.firstOrNull { it.extension == buffer }
            ?: contacts.firstOrNull { it.rawDigits.endsWith(buffer) }
            ?: contacts.firstOrNull { it.rawDigits.startsWith(buffer) }
    }

    fun handleDigit(char: Char) {
        if (isConnecting) return
        tonePlayer.playDtmf(char)
        haptics.click()
        val newBuffer = (dialedBuffer + char).takeLast(10)
        dialedBuffer = newBuffer
        updateMatched(newBuffer)

        // If matched by full 3-digit extension or full 10-digit number, auto-trigger connect
        val fullMatch = contacts.firstOrNull { it.extension == newBuffer || it.rawDigits == newBuffer }
        if (fullMatch != null) {
            matchedContact = fullMatch
            scope.launch {
                isConnecting = true
                tonePlayer.playRingback()
                delay(850)
                repo.launch(fullMatch.app)
                isConnecting = false
                dialedBuffer = ""
                matchedContact = null
            }
        }
    }

    fun handleCall() {
        val target = matchedContact ?: return
        if (isConnecting) return
        scope.launch {
            isConnecting = true
            tonePlayer.playRingback()
            delay(850)
            repo.launch(target.app)
            isConnecting = false
            dialedBuffer = ""
            matchedContact = null
        }
    }

    fun handleClear() {
        tonePlayer.playBusy()
        haptics.thud()
        dialedBuffer = ""
        matchedContact = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF191816), Color(0xFF100F0E), Color(0xFF0A0908))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header & Nixie Call Display
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOTLINE OS • TELEPHONE EXCHANGE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E927A),
                        letterSpacing = 1.sp
                    )

                    // Toggle Directory button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF262118))
                            .border(1.dp, Color(0xFF5A4C32), RoundedCornerShape(6.dp))
                            .clickable {
                                haptics.click()
                                showYellowPages = !showYellowPages
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showYellowPages) "HIDE PHONEBOOK" else "PHONEBOOK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF2E6C3)
                        )
                    }
                }

                NixieDisplay(
                    dialedBuffer = dialedBuffer,
                    countryPrefix = telecomRepo.countryPrefix,
                    matchedContact = matchedContact,
                    isConnecting = isConnecting,
                    onCallClick = ::handleCall,
                    onClearClick = ::handleClear,
                )
            }

            // 2. Rotary Dial / Keypad Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                RotaryDial(
                    modifier = Modifier.size(310.dp),
                    haptics = haptics,
                    onDigitDialed = ::handleDigit
                )
            }

            // 3. Quick Operator Assistance / Keypad Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dial 0 for Operator (toggles phonebook)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E1C1A))
                        .border(1.dp, Color(0xFF3B3730), RoundedCornerShape(6.dp))
                        .clickable {
                            handleDigit('0')
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "DIAL 0 • OPERATOR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA89F8B)
                    )
                }

                // Directory Quick Launcher
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF282520))
                        .border(1.dp, Color(0xFF5E5440), RoundedCornerShape(6.dp))
                        .clickable {
                            haptics.click()
                            showYellowPages = !showYellowPages
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "YELLOW PAGES (${contacts.size})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9500)
                    )
                }
            }

            // 4. Yellow Pages Bottom Overlay (if toggled)
            if (showYellowPages) {
                YellowPagesDirectory(
                    modifier = Modifier.fillMaxWidth(),
                    contacts = contacts,
                    onSelectContact = { contact ->
                        haptics.click()
                        matchedContact = contact
                        dialedBuffer = contact.extension
                        showYellowPages = false
                    }
                )
            }
        }
    }
}
