package com.parallax.parallaxlauncher.ui.modes.razr.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MenuItem(val id: Int, val title: String, val icon: String)

val SYNERGY_MENU_ITEMS = listOf(
    MenuItem(1, "Phonebook", "📖"),
    MenuItem(2, "Recent Calls", "📞"),
    MenuItem(3, "Messages", "✉"),
    MenuItem(4, "Games & Apps", "🕹"),
    MenuItem(5, "Multimedia", "🎵"),
    MenuItem(6, "WebAccess", "🌐"),
    MenuItem(7, "Office Tools", "⏰"),
    MenuItem(8, "Settings", "⚙"),
    MenuItem(9, "Camera", "📷"),
)

@Composable
fun RazrMenuGrid(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
) {
    val safeIndex = selectedIndex.coerceIn(0, SYNERGY_MENU_ITEMS.lastIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF071221))
            .border(2.dp, Color(0xFF1E3A60), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Selected Item Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF003866), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF00B0FF), RoundedCornerShape(4.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = SYNERGY_MENU_ITEMS[safeIndex].title.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00E5FF),
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // 3x3 Synergy Grid (Controlled exclusively by D-Pad below)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val item = SYNERGY_MENU_ITEMS[idx]
                        val isSelected = (idx == safeIndex)

                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF004077) else Color(0xFF0A1B30))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1B3250),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.icon,
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = item.title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF88AACC),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Softkeys: [ Select ] • [ Back ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071221), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF1E3A60), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[ Select ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            Text(
                text = "[ Back ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
