package com.parallax.parallaxlauncher.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.parallax.parallaxlauncher.core.model.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val prefs = appContext.getSharedPreferences("launch_counts", Context.MODE_PRIVATE)
    private val _counts = MutableStateFlow(prefs.all.mapNotNull { (k, v) -> (v as? Int)?.let { k to it } }.toMap())
    val launchCounts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refresh()
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            appContext.registerReceiver(receiver, filter)
        }
        refresh()
    }

    fun refresh() {
        scope.launch { _apps.value = query() }
    }

    private fun query(): List<AppInfo> {
        val pm = appContext.packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(main, 0)
            .filter { it.activityInfo.packageName != appContext.packageName }
            .map {
                val pkg = it.activityInfo.packageName
                AppInfo(
                    label = it.loadLabel(pm).toString(),
                    packageName = pkg,
                    launchIntent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(pkg, it.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: AppInfo) {
        val n = (_counts.value[app.packageName] ?: 0) + 1
        prefs.edit().putInt(app.packageName, n).apply()
        _counts.value = _counts.value + (app.packageName to n)
        runCatching { appContext.startActivity(app.launchIntent) }
    }

    fun clearCounts() {
        prefs.edit().clear().apply()
        _counts.value = emptyMap()
    }

    fun close() {
        runCatching { appContext.unregisterReceiver(receiver) }
    }
}
