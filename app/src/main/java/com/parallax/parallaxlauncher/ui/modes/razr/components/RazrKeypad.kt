package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.haptics.HapticEngine

private val EtchedMetalLight = Color(0xFF6B7078)
private val EtchedMetalDark = Color(0xFF33363A)
private val LuminescentCyan = Color(0xFF00E5FF)
private val GroovedLine = Color(0x6600E5FF)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RazrKeypad(
    modifier: Modifier = Modifier,
    haptics: HapticEngine,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    onSoftLeft: () -> Unit,
    onSoftRight: () -> Unit,
    onCall: () -> Unit,
    onEnd: () -> Unit,
    onKey: (Char) -> Unit,
    onKeyLongPress: ((Char) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(EtchedMetalLight, EtchedMetalDark, Color(0xFF1E2022))
                )
            )
            .border(2.dp, Color(0xFF888B90), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Upper D-Pad & Softkeys Deck
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Softkey
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF282A2E))
                    .border(1.dp, GroovedLine, RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.click()
                        onSoftLeft()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("―", fontSize = 18.sp, color = LuminescentCyan, fontWeight = FontWeight.Black)
            }

            // Central 4-Way D-Pad
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF4C5057), Color(0xFF1C1E20)))
                    )
                    .border(2.dp, LuminescentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Up button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clickable { haptics.click(); onUp() }
                        .padding(2.dp)
                ) {
                    Text("▲", fontSize = 10.sp, color = LuminescentCyan)
                }

                // Down button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable { haptics.click(); onDown() }
                        .padding(2.dp)
                ) {
                    Text("▼", fontSize = 10.sp, color = LuminescentCyan)
                }

                // Left button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { haptics.click(); onLeft() }
                        .padding(2.dp)
                ) {
                    Text("◀", fontSize = 10.sp, color = LuminescentCyan)
                }

                // Right button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { haptics.click(); onRight() }
                        .padding(2.dp)
                ) {
                    Text("▶", fontSize = 10.sp, color = LuminescentCyan)
                }

                // Center OK / MENU button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF003866))
                        .border(1.5.dp, LuminescentCyan, CircleShape)
                        .clickable {
                            haptics.thud()
                            onCenter()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Right Softkey
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF282A2E))
                    .border(1.dp, GroovedLine, RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.click()
                        onSoftRight()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("―", fontSize = 18.sp, color = LuminescentCyan, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(6.dp))

        // 2. Call / Clear / End Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Green Call Button
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF163E1C))
                    .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.thud()
                        onCall()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("CALL", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676))
            }

            // Center Clear Key
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF202226))
                    .border(1.dp, GroovedLine, RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.click()
                        onKey('C')
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("CLEAR", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LuminescentCyan)
            }

            // Red End / Power Button
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF421616))
                    .border(1.5.dp, Color(0xFFFF5252), RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.thud()
                        onEnd()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("END", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF5252))
            }
        }

        Spacer(Modifier.height(8.dp))

        // 3. Numeric 12-Key Pad (with prominent 0 +)
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { (digit, sub) ->
                        val isZeroPlus = (digit == "0")
                        if (isZeroPlus) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x3300B0FF))
                                    .border(1.dp, LuminescentCyan, RoundedCornerShape(4.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clickable {
                                            haptics.click()
                                            onKey('0')
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "0",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .background(Color(0x4400E5FF))
                                        .clickable {
                                            haptics.click()
                                            onKey('+')
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33000000))
                                    .border(1.dp, GroovedLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        haptics.click()
                                        onKey(digit[0])
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = digit,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (sub.isNotEmpty()) {
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = sub,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = LuminescentCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
