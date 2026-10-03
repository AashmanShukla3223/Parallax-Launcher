package com.parallax.parallaxlauncher.ui.modes.industrial.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.model.AppInfo
import com.parallax.parallaxlauncher.core.telemetry.Telemetry
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AmberOledPanel(
    selected: AppInfo?,
    armed: Boolean,
    telemetry: Telemetry,
    modifier: Modifier = Modifier,
) {
    val palette = LocalParallaxPalette.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(100)
        }
    }
    val clock = remember(now / 100) {
        SimpleDateFormat("HH:mm:ss.S", Locale.US).format(Date(now))
    }

    fun oledText(size: Int, color: Color = palette.accent, weight: FontWeight = FontWeight.Normal) =
        TextStyle(
            fontFamily = palette.font,
            fontSize = size.sp,
            color = color,
            fontWeight = weight,
            letterSpacing = 1.5.sp,
        )

    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF050403), shape)
            .border(2.dp, palette.border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(clock, style = oledText(22, weight = FontWeight.Bold))
            Text(
                if (armed) "ARMED" else "SAFE",
                style = oledText(14, if (armed) Color(0xFFFF3B30) else palette.accentDim),
            )
        }
        Text(
            (selected?.label ?: "NO SIGNAL").uppercase().take(22),
            style = oledText(20, weight = FontWeight.Bold),
        )
        Text(selected?.packageName?.take(34) ?: "--", style = oledText(10, palette.accentDim))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "BAT " + (if (telemetry.batteryMv >= 0) "${telemetry.batteryMv}mV" else "--") +
                    (if (telemetry.batteryPct >= 0) " ${telemetry.batteryPct}%" else "") +
                    (if (telemetry.charging) "+" else ""),
                style = oledText(12),
            )
            Text(
                "CPU " + (if (telemetry.cpuMhz >= 0) "${telemetry.cpuMhz}MHz" else "N/A"),
                style = oledText(12),
            )
        }
        Text(
            "MEM " + (if (telemetry.ramTotalMb > 0) "${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB" else "--") +
                (if (!telemetry.tempC.isNaN()) "  ${"%.1f".format(telemetry.tempC)}C" else ""),
            style = oledText(12, palette.accentDim),
        )
    }
}
