package com.parallax.parallaxlauncher.ui.modes.razr.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.parallaxlauncher.core.notifications.Intercepted
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIcon
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconGlyph
import com.parallax.parallaxlauncher.ui.modes.razr.RazrIconTints
import com.parallax.parallaxlauncher.ui.modes.razr.RazrPalette

/**
 * The in-app alert that stands in for Android's heads-up while Mode 7 owns the
 * foreground.
 *
 * Styled like the rest of the stock UI — a blue selection bar with white bold
 * text — rather than a foreign dark widget, so it reads as part of the handset.
 * Wording follows the manual: an unread message lights the message indicator and
 * the display announces "New Message". Counts render as
 * "1 New Message Received" / "N New Messages Received", with plainer wording for
 * non-messaging apps so the copy stays truthful.
 */
@Composable
fun RazrNotificationAlert(
    palette: RazrPalette,
    alert: Intercepted?,
    onOpenMessaging: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = alert != null,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.96f),
        modifier = modifier,
    ) {
        val current = alert ?: return@AnimatedVisibility
        val isMessage = current.headline.isMessage
        val headline = when {
            isMessage && current.count == 1 -> "1 NEW MESSAGE RECEIVED"
            isMessage -> "${current.count} NEW MESSAGES RECEIVED"
            current.count == 1 -> "1 NEW NOTIFICATION"
            else -> "${current.count} NEW NOTIFICATIONS"
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow)))
                .clickable(onClick = onOpenMessaging)
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                    RazrIconGlyph(
                        if (isMessage) RazrIcon.MESSAGES else RazrIcon.HELP,
                        palette.selectInk,
                        Modifier.size(11.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = headline,
                    color = palette.selectInk,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.1).sp,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.size(1.dp))
            Text(
                text = current.headline.sender,
                color = palette.selectInk,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (current.headline.text.isNotBlank()) {
                Text(
                    text = current.headline.text,
                    color = palette.selectInk.copy(alpha = 0.85f),
                    fontSize = 7.sp,
                    maxLines = 3,
                )
            }
            Spacer(Modifier.size(1.dp))
            Text(
                text = "Tap to open",
                color = palette.selectInk.copy(alpha = 0.75f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Compact variant used on the lower shell while the flip is closed. */
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
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.horizontalGradient(listOf(palette.selectTop, palette.selectLow)))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isMessage) "${current.count} NEW MESSAGE(S)" else "${current.count} NEW ALERT(S)",
                color = palette.selectInk,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/**
 * Explanatory strip on the Messages menu so the routing rule is discoverable:
 * in-app alert here, standard Android notification anywhere else.
 */
@Composable
fun RazrRoutingHint(
    palette: RazrPalette,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(palette.fieldAlt)
            .padding(horizontal = 5.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp), contentAlignment = Alignment.Center) {
                RazrIconGlyph(RazrIcon.MESSAGES, palette.selectLow, Modifier.size(9.dp))
            }
            Spacer(Modifier.width(3.dp))
            Text(
                "In-app alert here",
                color = palette.selectLow,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            "System alert elsewhere",
            color = palette.inkDim,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}