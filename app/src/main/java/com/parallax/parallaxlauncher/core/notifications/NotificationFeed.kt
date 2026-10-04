package com.parallax.parallaxlauncher.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class Headline(
    val packageName: String,
    val sender: String,
    val text: String,
    val postTime: Long,
) {
    val isMessage: Boolean get() = NotificationFeed.isMessagingPackage(packageName)
}

/**
 * A notification that the launcher swallowed because it was the foreground app.
 *
 * [count] is the running number of swallowed events so the UI can render the
 * authentic 2G wording ("1 New Message Received" / "3 New Messages Received").
 */
data class Intercepted(
    val headline: Headline,
    val count: Int,
)

/**
 * Process-wide notification bus.
 *
 * Routing rule implemented here (see [ParallaxNotificationListener]):
 *  * Launcher in the foreground + [MODE_RAZR] active  -> swallow the system
 *    notification and surface it in-app instead, so the user sees the RAZR
 *    "1 New Message Received" alert and no heads-up shadow.
 *  * Anything else (another app in front, or any other Parallax mode) -> the
 *    posting app's own Android notification is left completely untouched, so
 *    the normal system notification still shows.
 */
object NotificationFeed {
    /** Mode 7 — Motorola RAZR V3i. */
    const val MODE_RAZR = 7

    private val _items = MutableStateFlow<List<Headline>>(emptyList())
    val items: StateFlow<List<Headline>> = _items.asStateFlow()

    private val _isLauncherForeground = MutableStateFlow(false)
    val isLauncherForeground: StateFlow<Boolean> = _isLauncherForeground.asStateFlow()

    /** Active Parallax mode while the launcher is on screen; `0` when backgrounded. */
    private val _activeMode = MutableStateFlow(0)
    val activeMode: StateFlow<Int> = _activeMode.asStateFlow()

    private val _incomingNotificationEvent = MutableSharedFlow<Headline>(extraBufferCapacity = 10)
    val incomingNotificationEvent: SharedFlow<Headline> = _incomingNotificationEvent.asSharedFlow()

    /** Latest swallowed notification awaiting acknowledgement, or null. */
    private val _intercepted = MutableStateFlow<Intercepted?>(null)
    val intercepted: StateFlow<Intercepted?> = _intercepted.asStateFlow()

    /** Unread count accumulated while the launcher was in front. */
    private val _unread = MutableStateFlow(0)
    val unread: StateFlow<Int> = _unread.asStateFlow()

    /** True while the launcher should swallow notifications instead of letting Android show them. */
    val intercepting: Boolean
        get() = _isLauncherForeground.value && _activeMode.value == MODE_RAZR

    fun setLauncherForeground(foreground: Boolean) {
        _isLauncherForeground.value = foreground
        if (!foreground) {
            // Leaving the launcher: any in-app banner is stale, but the unread
            // count survives so the message indicator is still correct on return.
            _activeMode.value = 0
            _intercepted.value = null
        }
    }

    fun setActiveMode(mode: Int) {
        _activeMode.value = if (_isLauncherForeground.value) mode else 0
    }

    internal fun set(list: List<Headline>) {
        _items.value = list
    }

    /** Broadcast for surfaces that only care about "something arrived". */
    internal fun notifyPosted(headline: Headline) {
        _incomingNotificationEvent.tryEmit(headline)
    }

    /** Swallow: bump unread counters and raise the in-app banner. */
    internal fun interceptPosted(headline: Headline) {
        val next = _unread.value + 1
        _unread.value = next
        _intercepted.value = Intercepted(headline, next)
        notifyPosted(headline)
    }

    /** Dismiss the banner but keep the unread tally (message indicator stays lit). */
    fun acknowledgeIntercepted() {
        _intercepted.value = null
    }

    /** User actually looked at the messages: clear the indicator too. */
    fun markAllRead() {
        _unread.value = 0
        _intercepted.value = null
    }

    fun isMessagingPackage(packageName: String): Boolean =
        packageName.contains("messag", ignoreCase = true) ||
            packageName.contains("mms", ignoreCase = true) ||
            packageName.contains("sms", ignoreCase = true) ||
            packageName.contains("telephony", ignoreCase = true) ||
            packageName.contains("whatsapp", ignoreCase = true) ||
            packageName.contains("telegram", ignoreCase = true) ||
            packageName == "com.google.android.apps.messaging"
}

class ParallaxNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        refresh()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null && sbn.packageName != packageName && !sbn.isOngoing) {
            val extras = sbn.notification.extras
            val title = extras.getCharSequence("android.title")?.toString().orEmpty()
            val text = (extras.getCharSequence("android.text")
                ?: extras.getCharSequence("android.bigText"))?.toString().orEmpty()
            if (title.isNotBlank() || text.isNotBlank()) {
                val headline = Headline(
                    packageName = sbn.packageName,
                    sender = title.ifBlank { sbn.packageName },
                    text = text,
                    postTime = sbn.postTime,
                )
                if (NotificationFeed.intercepting) {
                    // Launcher is in front in RAZR mode: take the notification down so
                    // Android does not also drop a heads-up on top of the in-app alert.
                    runCatching { cancelNotification(sbn.key) }
                    NotificationFeed.interceptPosted(headline)
                } else {
                    // Another app is in front (or another Parallax mode): leave the
                    // posting app's normal Android notification completely alone.
                    NotificationFeed.notifyPosted(headline)
                }
            }
        }
        refresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val list = runCatching { activeNotifications }.getOrNull().orEmpty()
            .filter { it.packageName != packageName && !it.isOngoing }
            .mapNotNull {
                val extras = it.notification.extras
                val title = extras.getCharSequence("android.title")?.toString().orEmpty()
                val text = (extras.getCharSequence("android.text")
                    ?: extras.getCharSequence("android.bigText"))?.toString().orEmpty()
                if (title.isBlank() && text.isBlank()) null
                else Headline(it.packageName, title.ifBlank { it.packageName }, text, it.postTime)
            }
            .sortedByDescending { it.postTime }
        NotificationFeed.set(list)
    }
}