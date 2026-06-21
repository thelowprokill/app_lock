package com.example.app_lock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_lock_settings")

class AppLockRepository(private val context: Context) {

    private object PreferencesKeys {
        val LOCKED_PACKAGES = stringSetPreferencesKey("locked_packages")
        val SECURITY_PIN = stringPreferencesKey("security_pin")
    }

    val lockedPackages: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LOCKED_PACKAGES] ?: emptySet()
        }

    val securityPin: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SECURITY_PIN]
        }

    suspend fun toggleAppLock(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentLocked = preferences[PreferencesKeys.LOCKED_PACKAGES] ?: emptySet()
            val newLocked = if (currentLocked.contains(packageName)) {
                currentLocked - packageName
            } else {
                currentLocked + packageName
            }
            preferences[PreferencesKeys.LOCKED_PACKAGES] = newLocked
        }
    }

    suspend fun setSecurityPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURITY_PIN] = pin
        }
    }
}
