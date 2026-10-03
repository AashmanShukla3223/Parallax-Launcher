package com.parallax.parallaxlauncher.ui.modes.telecom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.telecom.AppContact

private val NixieAmber = Color(0xFFFF8A00)
private val NixieGlow = Color(0x66FF7700)
private val DarkGlass = Color(0xFF0F0E0D)

@Composable
fun NixieDisplay(
    modifier: Modifier = Modifier,
    dialedBuffer: String,
    countryPrefix: String,
    matchedContact: AppContact?,
    isConnecting: Boolean,
    onCallClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF22201D), Color(0xFF141312), Color(0xFF0A0A09))
                )
            )
            .border(2.dp, Color(0xFF38352F), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "BELL SYSTEM LINE 1",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9E927A),
                letterSpacing = 1.sp,
            )
            Text(
                text = if (isConnecting) "● CONNECTING..." else if (dialedBuffer.isNotEmpty()) "OFF HOOK" else "STANDBY",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isConnecting) Color(0xFF00E5FF) else if (dialedBuffer.isNotEmpty()) NixieAmber else Color(0xFF6E685C),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Main Nixie Tube Readout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkGlass)
                .border(1.dp, Color(0xFF2E2B25), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val displayText = if (dialedBuffer.isEmpty()) "$countryPrefix _ _ _ _ _" else "$countryPrefix $dialedBuffer"
            Text(
                text = displayText,
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NixieAmber,
                letterSpacing = 2.sp,
                modifier = Modifier.shadow(8.dp, spotColor = NixieAmber, ambientColor = NixieGlow)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Caller-ID / Matched App or Hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (matchedContact != null) {
                    Text(
                        text = "HOTLINE: ${matchedContact.app.label.uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                    )
                    Text(
                        text = "EXT: #${matchedContact.extension} • ${matchedContact.formattedNumber}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFA5A092),
                    )
                } else {
                    Text(
                        text = if (dialedBuffer.isEmpty()) "DIAL EXTENSION OR NUMBER" else "SEARCHING SWITCHBOARD...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF6E685C),
                    )
                }
            }

            // Dial & Hangup Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Clear / Hang up button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF8B2522))
                        .clickable(enabled = dialedBuffer.isNotEmpty()) { onClearClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CLEAR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Call / Launch button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (matchedContact != null) Color(0xFF287A38) else Color(0xFF3A443D))
                        .clickable(enabled = matchedContact != null || dialedBuffer.isNotEmpty()) { onCallClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CALL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
