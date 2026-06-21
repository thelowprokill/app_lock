package com.example.app_lock.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.example.app_lock.service.AppLockAccessibilityService

object PermissionUtils {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, AppLockAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
