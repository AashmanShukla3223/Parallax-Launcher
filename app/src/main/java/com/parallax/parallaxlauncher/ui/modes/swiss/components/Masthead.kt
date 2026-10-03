package com.parallax.parallaxlauncher.ui.modes.swiss.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.notifications.Headline
import com.parallax.parallaxlauncher.ui.theme.LocalParallaxPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Ink = Color(0xFF111111)

@Composable
fun Rule(thick: Boolean = false, color: Color = Ink) {
    Box(Modifier.fillMaxWidth().height(if (thick) 3.dp else 1.dp).background(color))
}

@Composable
fun Masthead(batteryMv: Int, headline: Headline?, wireEnabled: Boolean, onEnableWire: () -> Unit, onHeadline: (Headline) -> Unit) {
    val palette = LocalParallaxPalette.current
    val accent = palette.accent
    val date = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(Date()).uppercase()
    }
    Column(Modifier.fillMaxWidth()) {
        Rule(thick = true)
        Text(
            "THE DAILY PARALLAX",
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            style = TextStyle(
                fontFamily = palette.font, fontWeight = FontWeight.Black,
                fontSize = 38.sp, color = Ink, letterSpacing = (-1).sp,
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Rule()
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val meta = TextStyle(fontFamily = palette.font, fontSize = 10.sp, color = Ink, letterSpacing = 1.sp)
            Text(date, style = meta)
            Text(if (batteryMv >= 0) "EDITION NO. $batteryMv" else "EDITION NO. --", style = meta)
        }
        Rule()

        // Breaking-news wire ticker from notification listener
        if (headline != null) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    .clickable { onHeadline(headline) },
            ) {
                Text(
                    "WIRE // ",
                    style = TextStyle(fontFamily = palette.font, fontWeight = FontWeight.Black, fontSize = 11.sp, color = accent),
                )
                Text(
                    "${headline.source.uppercase()}: ${headline.title}",
                    style = TextStyle(fontFamily = palette.font, fontSize = 11.sp, color = Ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Rule()
        } else if (!wireEnabled) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clickable(onClick = onEnableWire),
            ) {
                Text(
                    "NOTIFICATION WIRE INACTIVE — TAP TO CONNECT AP/REUTERS FEED",
                    style = TextStyle(fontFamily = palette.font, fontSize = 9.sp, color = Color.Gray, letterSpacing = 0.5.sp),
                )
            }
            Rule()
        }
    }
}
