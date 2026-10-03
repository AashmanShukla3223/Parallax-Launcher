package com.parallax.parallaxlauncher.core.model

import android.content.Intent

data class AppInfo(
    val label: String,
    val packageName: String,
    val launchIntent: Intent,
)
