package com.example.app_lock.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isLocked: Boolean = false
)
