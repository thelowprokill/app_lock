package com.example.app_lock.data

/**
 * Manages the temporary unlock state for applications during a user session.
 */
object LockManager {
    private var lastUnlockedPackage: String? = null

    /**
     * Marks a package as temporarily unlocked.
     */
    fun unlockPackage(packageName: String) {
        lastUnlockedPackage = packageName
    }

    /**
     * Checks if a package is currently in an unlocked state.
     */
    fun isPackageUnlocked(packageName: String): Boolean {
        return lastUnlockedPackage == packageName
    }

    /**
     * Updates the current foreground package. 
     * If the user switches away from the locked app (and not to our own app), the session is reset.
     */
    fun updateForegroundPackage(packageName: String) {
        if (packageName != lastUnlockedPackage && packageName != "com.example.app_lock") {
            lastUnlockedPackage = null
        }
    }
}
