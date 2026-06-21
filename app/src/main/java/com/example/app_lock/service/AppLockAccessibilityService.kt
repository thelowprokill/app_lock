package com.example.app_lock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.app_lock.LockActivity
import com.example.app_lock.data.AppLockRepository
import com.example.app_lock.data.LockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: AppLockRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppLockRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Don't lock our own app
            if (packageName == applicationContext.packageName) return
            
            // Update LockManager with current foreground package
            LockManager.updateForegroundPackage(packageName)

            serviceScope.launch {
                val lockedPackages = repository.lockedPackages.first()
                if (lockedPackages.contains(packageName)) {
                    // Check if the package is already unlocked for this session
                    if (!LockManager.isPackageUnlocked(packageName)) {
                        Log.d("AppLockService", "Locked app detected: $packageName")
                        val intent = Intent(applicationContext, LockActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra("LOCKED_PACKAGE_NAME", packageName)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
    }
}
