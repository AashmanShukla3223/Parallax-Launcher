package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MotoBlue = Color(0xFF00B0FF)
private val MotoCyan = Color(0xFF00E5FF)

@Composable
fun RazrStandbyScreen(
    modifier: Modifier = Modifier,
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.ROOT)
        val dateFmt = SimpleDateFormat("EEE, dd-MMM-yy", Locale.ROOT)
        while (true) {
            val now = Date()
            currentTime = timeFmt.format(now)
            currentDate = dateFmt.format(now).uppercase(Locale.ROOT)
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F1E36), Color(0xFF08101E), Color(0xFF03070D))
                )
            )
            .border(2.dp, Color(0xFF2A4269), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Motorola Watermark Logo in center
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .border(2.dp, Color(0x1A00B0FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "M",
                fontFamily = FontFamily.Serif,
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                color = Color(0x0F00B0FF)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Vintage Digital Clock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = currentTime,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = MotoCyan,
                    letterSpacing = 2.sp
                )
                Text(
                    text = currentDate,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF88AACC),
                    letterSpacing = 1.sp
                )
            }

            // Center: 4-Way D-Pad Visual Guide (Controlled via Keypad below)
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Up: Phonebook
                Box(
                    modifier = Modifier.align(Alignment.TopCenter).padding(4.dp)
                ) {
                    Text("▲ PHONEBOOK", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MotoBlue)
                }

                // Down: Recent Calls
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                ) {
                    Text("▼ RECENT CALLS", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MotoBlue)
                }

                // Left: Messages
                Box(
                    modifier = Modifier.align(Alignment.CenterStart).padding(4.dp)
                ) {
                    Text("◀ SMS", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MotoBlue)
                }

                // Right: Camera
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp)
                ) {
                    Text("CAM ▶", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MotoBlue)
                }

                // Center: Menu Button indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF003866))
                        .border(2.dp, MotoCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MENU", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            // Bottom: Classic Motorola Softkey Bar (Driven by Left/Right softkeys on Keypad)
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
                    text = "[ Messages ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MotoCyan
                )
                Text(
                    text = "[ Menu ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "[ Contacts ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MotoCyan
                )
            }
        }
    }
}
