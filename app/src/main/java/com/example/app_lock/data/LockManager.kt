package com.example.app_lock.data

/**
 * Manages the temporary unlock state for applications during a user session.
 */
object LockManager {
    private var lastUnlockedPackage: String? = null

    fun unlockPackage(packageName: String) {
        lastUnlockedPackage = packageName
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        return lastUnlockedPackage == packageName
    }

    fun updateForegroundPackage(packageName: String) {
        // If the user switches away from the locked app (and not to our own app), clear the unlock state
        if (packageName != lastUnlockedPackage && packageName != "com.example.app_lock") {
            lastUnlockedPackage = null
        }
    }
}
