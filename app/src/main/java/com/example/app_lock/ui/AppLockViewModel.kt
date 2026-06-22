package com.example.app_lock.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_lock.data.AppInfo
import com.example.app_lock.data.AppLockRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppLockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppLockRepository(application)
    private val packageManager = application.packageManager

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    val securityPin: StateFlow<String?> = repository.securityPin.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            repository.securityPin.firstOrNull()
            _isInitialized.value = true
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val appList: StateFlow<List<AppInfo>> = combine(
        repository.lockedPackages,
        _searchQuery
    ) { lockedPackages, query ->
        val installedApps = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<ApplicationInfo>()
        }
        installedApps
            .filter { it.packageName != application.packageName }
            .map { appInfo ->
                AppInfo(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isLocked = lockedPackages.contains(appInfo.packageName)
                )
            }
            .filter { it.name.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
            .sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleAppLock(packageName: String) {
        viewModelScope.launch {
            repository.toggleAppLock(packageName)
        }
    }

    fun setSecurityPin(pin: String) {
        viewModelScope.launch {
            repository.setSecurityPin(pin)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
