package com.example.app_lock.data

object LockManager {
    private var lastUnlockedPackage: String? = null

    fun unlockPackage(packageName: String) {
        lastUnlockedPackage = packageName
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        return lastUnlockedPackage == packageName
    }

    fun updateForegroundPackage(packageName: String) {
        if (packageName != lastUnlockedPackage && packageName != "com.example.app_lock") {
            lastUnlockedPackage = null
        }
    }
}
