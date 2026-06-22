package com.example.app_lock.utils

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.example.app_lock.service.AppLockAccessibilityService

object PermissionUtils {
    /**
     * Checks if the Accessibility Service required for monitoring is enabled.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, AppLockAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            if (componentNameString.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * On Android 13+, sensitive settings like Accessibility Services are restricted for 
     * sideloaded apps, graying out the toggle in Settings. 
     * This check identifies if the "Allow restricted settings" option in App Info needs 
     * to be enabled before the service can be turned on.
     */
    fun isAccessibilityRestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return try {
            // "android:access_restricted_settings" is the internal string for OPSTR_ACCESS_RESTRICTED_SETTINGS
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    "android:access_restricted_settings",
                    context.applicationInfo.uid,
                    context.packageName
                )
            } else {
                appOpsManager.noteOpNoThrow(
                    "android:access_restricted_settings",
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            // MODE_IGNORED (1) or MODE_ERRORED (2) indicates that restricted settings are active.
            mode != AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
