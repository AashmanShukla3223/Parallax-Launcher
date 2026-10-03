package com.parallax.parallaxlauncher.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Headline(val packageName: String, val sender: String, val text: String, val postTime: Long)

object NotificationFeed {
    private val _items = MutableStateFlow<List<Headline>>(emptyList())
    val items: StateFlow<List<Headline>> = _items.asStateFlow()
    internal fun set(list: List<Headline>) { _items.value = list }
}

class ParallaxNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() = refresh()
    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val list = runCatching { activeNotifications }.getOrNull().orEmpty()
            .filter { it.packageName != packageName && !it.isOngoing }
            .mapNotNull {
                val e = it.notification.extras
                val title = e.getCharSequence("android.title")?.toString().orEmpty()
                val text = (e.getCharSequence("android.text") ?: e.getCharSequence("android.bigText"))
                    ?.toString().orEmpty()
                if (title.isBlank() && text.isBlank()) null
                else Headline(it.packageName, title.ifBlank { it.packageName }, text, it.postTime)
            }
            .sortedByDescending { it.postTime }
        NotificationFeed.set(list)
    }
}
