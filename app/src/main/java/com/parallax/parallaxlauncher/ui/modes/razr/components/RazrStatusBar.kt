package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MotoBlue = Color(0xFF00B0FF)
private val MotoCyan = Color(0xFF00E5FF)
private val DarkBg = Color(0xFF0C131F)

@Composable
fun RazrStatusBar(
    modifier: Modifier = Modifier,
    carrierName: String,
    batteryPercent: Int,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBg)
            .border(1.dp, Color(0xFF1E2E4A))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: 2G Signal Bars + [G] GPRS Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Signal bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(12.dp)
                ) {
                    val heights = listOf(4.dp, 6.dp, 9.dp, 12.dp)
                    heights.forEachIndexed { index, h ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(h)
                                .background(if (index < 4) MotoCyan else Color(0xFF2C3E55))
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                // Vintage 2G / GPRS indicator badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF003866), RoundedCornerShape(2.dp))
                        .border(1.dp, MotoBlue, RoundedCornerShape(2.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "2G GPRS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MotoCyan
                    )
                }
            }

            // Center: Vintage Audio / Message Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✉", // Envelope
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White
                )
                Text(
                    text = "🔊", // Ring style
                    fontSize = 10.sp,
                )
            }

            // Right: 3-Segment Retro Battery
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, MotoCyan, RoundedCornerShape(2.dp))
                        .padding(1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier.height(8.dp)
                    ) {
                        val segments = (batteryPercent / 34).coerceIn(1, 3)
                        repeat(3) { idx ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(8.dp)
                                    .background(if (idx < segments) Color(0xFF00E676) else Color(0xFF1E3A2E))
                            )
                        }
                    }
                }
                // Battery terminal nub
                Box(
                    modifier = Modifier
                        .size(width = 1.5.dp, height = 4.dp)
                        .background(MotoCyan)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        // Center Vintage Carrier Name Banner
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = carrierName.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MotoBlue,
                letterSpacing = 1.sp
            )
        }
    }
}
