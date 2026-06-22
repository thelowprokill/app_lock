package com.example.app_lock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_lock.ui.AppListScreen
import com.example.app_lock.ui.AppLockViewModel
import com.example.app_lock.ui.PinScreen
import com.example.app_lock.ui.theme.App_lockTheme
import com.example.app_lock.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    private val viewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App_lockTheme {
                val isInitialized by viewModel.isInitialized.collectAsState()
                val securityPin by viewModel.securityPin.collectAsState()
                
                if (!isInitialized) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    MainContent(viewModel, securityPin)
                }
            }
        }
    }

    @Composable
    private fun MainContent(viewModel: AppLockViewModel, securityPin: String?) {
        val navController = rememberNavController()
        var showPermissionDialog by remember { mutableStateOf(false) }
        var isRestricted by remember { mutableStateOf(false) }

        // Monitor lifecycle to check permissions when user returns from settings
        DisposableEffect(Unit) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    val enabled = PermissionUtils.isAccessibilityServiceEnabled(this@MainActivity)
                    if (!enabled) {
                        isRestricted = PermissionUtils.isAccessibilityRestricted(this@MainActivity)
                        showPermissionDialog = true
                    } else {
                        showPermissionDialog = false
                    }
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { /* Permission is mandatory for core functionality */ },
                title = { 
                    Text(
                        text = if (isRestricted) "Restricted Setting" else "Permission Required",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    if (isRestricted) {
                        Text("For your security, this setting is currently unavailable.\n\nTo enable it:\n1. Go to App Info for App Lock.\n2. Tap the ⋮ menu in the top-right corner.\n3. Select 'Allow restricted settings'.\n4. Authenticate your identity (PIN/Fingerprint).\n5. Return here and try going to Accessibility Settings again.")
                    } else {
                        Text("App Lock requires Accessibility Service permission to protect your restricted apps. Please enable it in Settings.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (isRestricted) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
                        } else {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    }) {
                        Text(if (isRestricted) "Open App Info" else "Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = if (securityPin == null) "setup_pin" else "enter_pin"
        ) {
            composable("setup_pin") {
                PinScreen(
                    isSetup = true,
                    onPinConfirmed = { pin ->
                        viewModel.setSecurityPin(pin)
                        navController.navigate("app_list") {
                            popUpTo("setup_pin") { inclusive = true }
                        }
                        true
                    }
                )
            }
            composable("enter_pin") {
                PinScreen(
                    isSetup = false,
                    onPinConfirmed = { pin ->
                        if (pin == securityPin) {
                            navController.navigate("app_list") {
                                popUpTo("enter_pin") { inclusive = true }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )
            }
            composable("app_list") {
                AppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate("setup_pin")
                    }
                )
            }
        }
    }
}
