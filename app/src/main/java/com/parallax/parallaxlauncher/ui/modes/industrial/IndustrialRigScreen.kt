package com.parallax.parallaxlauncher.ui.modes.industrial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.data.AppsRepository
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import com.parallax.parallaxlauncher.core.telemetry.TelemetryService
import com.parallax.parallaxlauncher.ui.modes.industrial.components.AmberOledPanel
import com.parallax.parallaxlauncher.ui.modes.industrial.components.KnurledRotaryDial
import com.parallax.parallaxlauncher.ui.modes.industrial.components.SafetyArmToggle
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette

@Composable
fun IndustrialRigScreen(
    repo: AppsRepository,
    telemetry: TelemetryService,
    haptics: HapticEngine,
    detentDeg: Float = 15f,
) {
    val palette = LocalParallaxPalette.current
    val apps by repo.apps.collectAsState()
    val tel by telemetry.state.collectAsState()
    var index by remember { mutableIntStateOf(0) }
    var armed by remember { mutableStateOf(false) }
    val selected = apps.getOrNull(index)

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AmberOledPanel(selected, armed, tel)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SafetyArmToggle(armed, onToggle = {
                armed = it
                haptics.thud()
            })
            val shape = RoundedCornerShape(8.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .background(if (armed) palette.accent else Color(0xFF26272B), shape)
                    .border(2.dp, Color(0xFF0A0A0B), shape)
                    .clickable(enabled = armed && selected != null) {
                        haptics.thud()
                        selected?.let(repo::launch)
                        armed = false
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "LAUNCH",
                    style = TextStyle(
                        fontFamily = palette.font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 4.sp,
                        color = if (armed) Color.Black else Color(0xFF55575C),
                    ),
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            KnurledRotaryDial(
                itemCount = apps.size,
                onIndexChange = {
                    if (it != index) haptics.tick()
                    index = it
                },
                modifier = Modifier.padding(8.dp),
                detentDeg = detentDeg,
            )
        }
    }
}
