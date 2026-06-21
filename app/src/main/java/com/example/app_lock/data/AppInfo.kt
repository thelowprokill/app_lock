package com.example.app_lock.data

import android.graphics.drawable.Drawable

/**
 * Data class representing information about an installed application.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isLocked: Boolean = false
)
