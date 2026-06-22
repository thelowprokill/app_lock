package com.example.app_lock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.app_lock.data.AppInfo
import com.example.app_lock.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppLockViewModel,
    onNavigateToSettings: () -> Unit
) {
    val appList by viewModel.appList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    
    var isServiceEnabled by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }

    // Re-check permission when the screen is focused
    LaunchedEffect(Unit) {
        isServiceEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locked Apps") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Security Settings")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (!isServiceEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Service is disabled. Locking is inactive.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(appList, key = { it.packageName }) { app ->
                    AppItem(
                        app = app,
                        onToggleLock = { viewModel.toggleAppLock(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    onToggleLock: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(app.name) },
        supportingContent = { Text(app.packageName) },
        leadingContent = {
            app.icon?.let {
                Image(
                    bitmap = it.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        trailingContent = {
            Switch(
                checked = app.isLocked,
                onCheckedChange = onToggleLock
            )
        }
    )
}
