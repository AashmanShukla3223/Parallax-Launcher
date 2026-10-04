package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.notifications.Intercepted
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * The in-app alert that replaces Android's heads-up while the launcher itself is
 * the foreground app in RAZR mode.
 *
 * Wording follows the V3i manual: an unread message lights the message indicator
 * and the display announces "New Message". The count is rendered as
 * "1 New Message Received" / "N New Messages Received", with a plain
 * "notification" wording for non-messaging apps so the copy stays truthful.
 */
@Composable
fun RazrNotificationAlert(
    palette: RazrPalette,
    alert: Intercepted?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = alert != null,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f),
        modifier = modifier,
    ) {
        val current = alert ?: return@AnimatedVisibility
        val isMessage = current.headline.isMessage
        val count = current.count
        val headline = when {
            isMessage && count == 1 -> "1 NEW MESSAGE RECEIVED"
            isMessage -> "$count NEW MESSAGES RECEIVED"
            count == 1 -> "1 NEW NOTIFICATION"
            else -> "$count NEW NOTIFICATIONS"
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(palette.bezel)
                .border(1.dp, palette.groove, RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Envelope glyph for messages, bell glyph otherwise.
                Text(
                    text = if (isMessage) "✉" else "◉",
                    color = palette.grooveGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.padding(horizontal = 2.dp))
                Text(
                    text = headline,
                    color = palette.grooveGlow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.padding(top = 1.dp))
            Text(
                text = current.headline.sender,
                color = palette.lcdBacklight,
                fontFamily = FontFamily.Monospace,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (current.headline.text.isNotBlank()) {
                Text(
                    text = current.headline.text,
                    color = palette.lcdBacklight.copy(alpha = 0.75f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 5.5.sp,
                    maxLines = 3,
                )
            }
        }
    }
}

/**
 * Variant used while the flip is closed, rendered inside the 96x80 cover display.
 */
@Composable
fun RazrCoverNotificationAlert(
    palette: RazrPalette,
    alert: Intercepted?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = alert != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        val current = alert ?: return@AnimatedVisibility
        val isMessage = current.headline.isMessage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(palette.ink)
                .padding(horizontal = 3.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isMessage) "${current.count} NEW MSG" else "${current.count} NEW ALERT",
                color = palette.lcdBacklight,
                fontFamily = FontFamily.Monospace,
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/**
 * Explanation strip shown on the Messages menu entry so the behaviour is
 * discoverable: in-app alert here, standard Android notification elsewhere.
 */
@Composable
fun RazrRoutingHint(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(palette.ink.copy(alpha = 0.12f))
            .padding(horizontal = 3.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "IN-APP ALERT ACTIVE",
            color = palette.accent,
            fontFamily = FontFamily.Monospace,
            fontSize = 5.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "SYSTEM ALERT ELSEWHERE",
            color = palette.inkDim,
            fontFamily = FontFamily.Monospace,
            fontSize = 5.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}